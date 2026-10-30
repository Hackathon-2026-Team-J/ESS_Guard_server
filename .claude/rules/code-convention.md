---
description: Java 소스 코드를 작성하거나 수정할 때 공통으로 적용되는 컨벤션
paths:
  - "src/main/java/**/*.java"
---

# Code Convention

## 레이어 구조

Controller → Service → Repository 순서를 따른다. 역방향 의존을 만들지 마라.

## 예외 처리

- 공통 예외 클래스 하나와 에러코드 enum 하나로 처리한다 (`throw new RestApiException(XXX)`)
- 에러코드는 static import로 쓴다 (`CustomErrorCode.XXX` 표기 금지)

## 객체 생성

- Entity와 Response DTO는 정적 팩토리 메서드(`create()`, `of()`, `from()`)로만 생성한다
- 생성자는 private + `@Builder(access = AccessLevel.PRIVATE)`로 감춘다. record는 팩토리만 호출한다

## 상수

- 비즈니스 의미가 있는 값(임계값, 허용 범위, 규칙 이름)은 `private static final` 상수로 선언한다
- 탐지 규칙의 임계값은 매직넘버로 두지 말고 규칙 클래스 상단 상수로 둔다

## 포맷팅

- 메서드와 생성자 파라미터가 2개 이상이면 파라미터마다 줄바꿈하고, 닫는 괄호는 단독 줄에 둔다
- 주입 필드는 Service → Repository → 그 밖의 컴포넌트 → 프레임워크 객체 순으로 묶고, 종류가 바뀌는 곳에만 빈 줄을 넣는다
- 메서드 본문에서 논리 단계가 바뀌면 빈 줄로 구분한다
- private 메서드는 그것을 사용하는 메서드 바로 아래에, 호출되는 순서대로 둔다

## 네이밍

- 패키지는 도메인 단위, 클래스는 PascalCase, 메서드는 camelCase + CRUD 동사
- 조회용 Map 변수는 `{키}To{값}` 형태 (`commandIdToEvents`)

## 주석

- 설명 주석을 달지 마라. 이름과 구조로 드러내라
