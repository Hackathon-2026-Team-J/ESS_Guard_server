# ESS Guard Server

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?logo=docker&logoColor=white)

EMS 제어 명령과 ESS 측정값을 운전 계획과 대조해 이상 징후를 탐지하는 규칙 기반 보안 모니터링 백엔드입니다.

명령을 보낸 계정이 정상이어도, **계획과 어긋난 명령**이나 **물리적으로 설명되지 않는 SOC 변화**는 잡아냅니다. 해커톤 시연용 MVP로, 실제 장비 대신 목 데이터를 재생해 탐지 흐름을 보여 줍니다.

## 주요 기능

- **규칙 기반 탐지**: 명령 단계 3개, 측정값 단계 1개, 총 4개 룰
- **시간순 재생**: 명령과 측정값을 하나의 시간축으로 합쳐 1초에 1건씩 처리
- **실시간 전송**: 탐지 결과를 SSE로 대시보드에 바로 전달
- **조회 API**: 탐지 이벤트 목록과 룰, 심각도별 통계 제공

## 탐지 룰

부호 기준은 `+` 방전, `-` 충전, `0` 대기입니다. 모든 시간 판단은 실제 시계가 아니라 데이터 시각을 따릅니다.

| ID | 룰 | 심각도 | 탐지 조건 |
|---|---|---|---|
| R1 | 방향 불일치 | HIGH | 명령 방향이 계획 방향과 다름 |
| R2 | 출력 편차 | MEDIUM, HIGH | 계획과 방향은 같지만 편차가 20% 초과이고 2 kW 이상 (50% 초과면 HIGH) |
| R3 | 급격한 방향 전환 | HIGH | 5분 내 충방전 전환 4회 이상, 또는 직전 방향을 60초도 유지하지 않고 전환 |
| R4 | 물리 정합성 | HIGH | 직전 출력으로 계산한 SOC 변화와 실제 변화의 차이가 15% 초과이고 0.5%p 이상 |

- R3는 계획 방향이 바뀌는 시각 ±30초 안의 전환을 제외합니다.
- R4는 같은 장치의 직전 2분 안 명령에 R1~R3 탐지가 있으면 이벤트를 만들지 않습니다.
- 장치 용량은 모두 500 kWh로 계산합니다.

## API

| 메서드 | 경로 | 설명 |
|---|---|---|
| POST | `/api/replay/start` | 재생 시작 |
| POST | `/api/replay/stop` | 재생 정지, 처리 위치는 유지 |
| POST | `/api/replay/reset` | 재생 정지, 탐지 이벤트 삭제, 처리 상태 초기화 |
| GET | `/api/events` | 탐지 이벤트 목록 (최신순) |
| GET | `/api/stats` | 처리 현황과 룰, 심각도별 탐지 건수 |
| GET | `/api/stream` | 실시간 탐지 SSE 스트림 (`detection`, `reset` 이벤트) |

R4 이벤트는 측정값 기반이라 `commandLogId`, `clientId` 등 명령 정보 필드가 `null`입니다. 자세한 응답 형식은 Swagger UI를 참고하세요.

## 목 데이터

`src/main/resources/data/`의 CSV를 사용합니다. 파일이 없으면 경고 로그만 남기고 건너뜁니다.

| 파일 | 내용 | 행 수 |
|---|---|---|
| `ems_commands.csv` | EMS 명령 | 141 |
| `dispatch_plan.csv` | 장치별 운전 계획 | 5 |
| `telemetry.csv` | 30초 간격 출력, SOC 측정값 | 240 |

## 기술 스택

| 구분 | 사용 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5, Spring Data JPA |
| Database | H2 (로컬), MySQL 8.4 (운영) |
| API 문서 | springdoc-openapi |
| 배포 | Docker Compose, GitHub Actions, AWS EC2 |

## 프로젝트 구조

```text
src/main/java/kr/sentinel/
├── command/     # EMS 명령 로그, CSV 적재, 재생
├── demo/        # 재생 제어 API
├── detection/   # 룰 엔진, 탐지 룰
├── event/       # 탐지 이벤트 저장, 조회 API
├── global/      # 공통 설정
├── plan/        # 운전 계획
├── stream/      # SSE 스트림
└── telemetry/   # 측정값
```
