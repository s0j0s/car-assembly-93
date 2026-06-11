# 리팩토링 계획

---

## Phase 1 — Gradle 프로젝트 세팅

### 1-1. Gradle Wrapper 생성
- [ ] `gradle/wrapper/gradle-wrapper.properties` 생성 (Gradle 8.x)
- [ ] `gradle/wrapper/gradle-wrapper.jar` 생성
- [ ] `gradlew`, `gradlew.bat` 생성

### 1-2. 빌드 스크립트 작성
- [ ] `settings.gradle` 생성 (`rootProject.name = 'assemblyCar'`)
- [ ] `build.gradle` 생성
  - [ ] `java` 플러그인, `application` 플러그인
  - [ ] `sourceCompatibility = 17`
  - [ ] `mainClass = 'Assemble'`
  - [ ] JUnit 5 의존성 (`junit-jupiter`)
  - [ ] JaCoCo 플러그인

### 1-3. 디렉토리 구조 재편
- [ ] `src/main/java/` 생성
- [ ] `src/test/java/` 생성
- [ ] `java/Assemble.java` → `src/main/java/Assemble.java` 이동
- [ ] `java/` 폴더 제거

### 1-4. .gitignore 업데이트
- [ ] `.gradle/` 추가
- [ ] `build/` 추가

### 1-5. 빌드 검증
- [ ] `./gradlew compileJava` — 컴파일 확인
- [ ] `./gradlew run` — 실행 확인

---

## Phase 2 — Unit Test 작성 (리팩토링 전 안전망)

> 기존 `Assemble.java` 코드 기준. 리팩토링 후 테스트가 깨지면 안전망 역할.

### 2-1. 테스트 클래스 생성
- [ ] `src/test/java/AssembleTest.java` 생성
- [ ] `@BeforeEach` 에서 `stack` 초기화 방법 결정
  - `Assemble.stack` 접근 방식 검토 (현재 `private static`)
  - 테스트 가능하도록 접근 제어자 `package-private` 으로 변경 여부 결정

### 2-2. 호환성 실패 케이스 테스트
- [ ] `sedan_with_continental_brake_fails`
  - CarType=SEDAN, BrakeSystem=CONTINENTAL → `isValidCheck()` false
- [ ] `suv_with_toyota_engine_fails`
  - CarType=SUV, Engine=TOYOTA → `isValidCheck()` false
- [ ] `truck_with_wia_engine_fails`
  - CarType=TRUCK, Engine=WIA → `isValidCheck()` false
- [ ] `truck_with_mando_brake_fails`
  - CarType=TRUCK, BrakeSystem=MANDO → `isValidCheck()` false
- [ ] `bosch_brake_requires_bosch_steering_fails`
  - BrakeSystem=BOSCH, SteeringSystem=MOBIS → `isValidCheck()` false

### 2-3. 정상 케이스 테스트
- [ ] `valid_combination_passes`
  - CarType=SEDAN, Engine=GM, BrakeSystem=MANDO, SteeringSystem=BOSCH → `isValidCheck()` true
- [ ] `broken_engine_does_not_run`
  - Engine=4 (BROKEN) → `runProducedCar()` 출력에 "고장" 포함 확인

### 2-4. 테스트 실행 및 Coverage 측정
- [ ] `./gradlew test` — 전체 통과 확인
- [ ] `./gradlew jacocoTestReport` 실행
- [ ] `build/reports/jacoco/test/html/index.html` coverage 확인
- [ ] baseline coverage 수치 `CLAUDE.md`에 기록

---

## Phase 3 — enum 정의

### 3-1. CarType enum
- [ ] `src/main/java/model/CarType.java`
  - 값: `SEDAN`, `SUV`, `TRUCK`
  - 표시 이름 필드 (`displayName`)

### 3-2. Engine enum
- [ ] `src/main/java/model/Engine.java`
  - 값: `GM`, `TOYOTA`, `WIA`, `BROKEN`
  - `isBroken()` 메서드

### 3-3. BrakeSystem enum
- [ ] `src/main/java/model/BrakeSystem.java`
  - 값: `MANDO`, `CONTINENTAL`, `BOSCH`

### 3-4. SteeringSystem enum
- [ ] `src/main/java/model/SteeringSystem.java`
  - 값: `BOSCH`, `MOBIS`

---

## Phase 4 — Car 도메인 모델

### 4-1. Car 클래스
- [ ] `src/main/java/model/Car.java`
  - 필드: `CarType`, `Engine`, `BrakeSystem`, `SteeringSystem`
  - getter / setter
  - `isFullyConfigured()` — 4개 부품 모두 선택 여부

### 4-2. 기존 `int[] stack` 제거 검증
- [ ] `Assemble.java` 에서 `stack` → `Car` 객체로 교체
- [ ] `./gradlew compileJava` 통과 확인

---

## Phase 5 — 호환성 규칙 분리

### 5-1. CompatibilityRule 인터페이스
- [ ] `src/main/java/rule/CompatibilityRule.java`
  - `@FunctionalInterface`
  - `Optional<String> check(Car car)` — 위반 시 메시지 반환

### 5-2. CompatibilityChecker 클래스
- [ ] `src/main/java/rule/CompatibilityChecker.java`
  - `RULES` — 규칙 5개 `List<CompatibilityRule>` 정의
  - `List<String> validate(Car car)` — 위반 메시지 목록 반환
  - `boolean isValid(Car car)` — 전체 통과 여부
- [ ] 기존 `isValidCheck()` 제거
- [ ] 기존 `testProducedCar()` 중복 규칙 제거 → `CompatibilityChecker` 사용

### 5-3. CompatibilityChecker 테스트 작성
- [ ] `src/test/java/rule/CompatibilityCheckerTest.java`
  - Phase 2 테스트 케이스 동일하게 이전
  - 각 규칙별 독립 테스트
- [ ] `./gradlew test` 통과 확인

---

## Phase 6 — UI / Service 분리

### 6-1. ConsoleUI 클래스
- [ ] `src/main/java/ui/ConsoleUI.java`
  - `displayMenu(String title, String[] options)` — 공통 메뉴 출력
  - `readInput(Scanner sc)` — 입력 처리
  - 기존 `show*Menu()` 4개 메서드 통합

### 6-2. CarAssembler 서비스
- [ ] `src/main/java/service/CarAssembler.java`
  - `run()` — 전체 조립 흐름 제어
  - `ConsoleUI` 생성자 주입 (테스트 용이성)
  - 기존 `main()` 89줄 로직 이전

### 6-3. Assemble.java 진입점 정리
- [ ] `main()` — `new CarAssembler(new ConsoleUI()).run()` 만 남기기
- [ ] `InterruptedException` → `Thread.currentThread().interrupt()` 복원

### 6-4. 전체 동작 검증
- [ ] `./gradlew run` — 실제 실행 확인

---

## Phase 7 — 최종 검증 및 Commit

### 7-1. 테스트 및 Coverage 최종 확인
- [ ] `./gradlew test` — 전체 통과 확인
- [ ] `./gradlew jacocoTestReport` — coverage 측정
- [ ] Phase 2 baseline 대비 coverage 향상 확인

### 7-2. 코드 스멜 체크리스트
- [ ] God Class 해소 (4개 클래스 분리)
- [ ] Primitive Obsession 해소 (enum + Car)
- [ ] Data Clump 해소 (Car 객체)
- [ ] Long Method 해소 (main → CarAssembler.run())
- [ ] Switch Statements 해소 (enum 메서드 위임)
- [ ] Duplicate Code 해소 (메뉴, 이름 매핑, 규칙)
- [ ] Divergent Change 해소 (CompatibilityChecker)
- [ ] Magic Numbers 해소 (Engine.BROKEN)
- [ ] Dead Code 해소 (InterruptedException)

### 7-3. 빌드 산출물 확인
- [ ] `build/`, `.gradle/` `.gitignore` 반영 여부 확인
- [ ] `src/test/java/` 테스트 파일 커밋 포함 확인

### 7-4. Commit & Push
- [ ] `git add` 스테이징
- [ ] commit 메시지 작성
- [ ] `git push`
