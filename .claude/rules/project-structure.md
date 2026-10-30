---
description: 프로젝트 디렉토리 구조. 새 파일을 생성하거나 패키지 위치를 결정할 때 참조
paths:
  - "src/main/java/**/*.java"
---

# 프로젝트 구조

루트 패키지는 프로젝트 생성 시 정한 값을 따른다 (`{root}`). 도메인 단위로 나눈다.

```
src/main/java/{root}/
├── global/        # 공통 설정, 예외, 응답 포맷, 유틸
├── command/       # EMS 명령 로그 (수신 이력, 조회)
├── detection/     # 규칙 기반 이상 탐지, 탐지 이벤트 발행
├── event/         # 탐지 이벤트 (대시보드 조회용)
└── mock/          # 목 데이터 적재 (시연용)
```

## 도메인 패키지 내부 구조

```
{domain}/
├── controller/
├── service/
├── repository/
├── domain/        # Entity
└── dto/
    ├── request/
    └── response/
```

- 필요할 때만 `listener/`, `rule/` 같은 하위 패키지를 추가한다.
- 탐지 규칙은 규칙 하나당 클래스 하나로 둔다 (`detection/rule/`). 규칙 추가가 시연 포인트다.
