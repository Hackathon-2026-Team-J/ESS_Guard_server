# ESS Guard Server

ESS(에너지저장장치) EMS 제어 명령의 이상 징후를 탐지하는 백엔드. 해커톤 MVP이므로 핵심 흐름만 구현하고, 시연 가능한 범위를 넘기지 않는다.

## MVP 범위

- DB에 EMS 명령 로그 목 데이터를 적재한다
- 규칙 기반으로 이상 명령을 탐지하고 이벤트를 발행한다
- 프론트 대시보드가 조회할 API를 제공한다 (탐지 이벤트 목록, 명령 로그 목록)
- 범위 밖: 실제 Modbus 연동, 실시간 프록시, 머신러닝 탐지, 복잡한 인증

## 기술 스택

- Java 21 / Spring Boot 3.5 / Gradle
- Spring Data JPA
- DB: (TODO: 팀 확정 후 기입. 시연 속도가 우선이면 H2로 시작)
- springdoc-openapi (프론트 협업용 Swagger UI)

## 빌드 & 실행

```bash
./gradlew build
./gradlew test
./gradlew bootRun
```

## 상호작용 규칙

- 사용자의 관찰과 의문 표현(`~한 부분이 있네?`, `이거 왜 이렇게 했어?`)은 수정 지시가 아니다.
  해당 코드를 직접 확인한 뒤 문제인지 아닌지에 대한 판단과 근거를 먼저 제시하라. 수정 여부는 사용자가 정한다.
- 수정에 착수하는 조건은 명시적 지시(`고쳐줘`, `수정해`, `반영해`, `바꿔줘`)뿐이다.
- 사용자의 지적이 사실과 다르면 동의하지 말고 근거를 들어 다른 판단을 말하라.
- 해커톤이라 시간이 짧다. 요청하지 않은 리팩토링, 추상화, 확장 포인트는 만들지 마라.

## 규칙 참조

`.claude/rules/` - paths 매칭 파일 작업 시 자동 로드

- 패키지 배치 → `project-structure.md`
- 코드 컨벤션 → `code-convention.md`

`.claude/spec/` - 필요할 때만 참조 (자동 로드 아님)

- Git 작업 (커밋, 브랜치, PR) → `git-convention.md`

## 스킬 / 에이전트

- `commit-push` - 변경 사항 확인 후 컨벤션에 맞춰 커밋, 푸시
- `security-reviewer` (agent) - 보안 관점 코드 리뷰
