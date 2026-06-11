# 리팩토링 계획

---

## Phase 1 — Gradle 프로젝트 세팅

### 1-1. Gradle Wrapper 생성
- [x] `gradle/wrapper/gradle-wrapper.properties` 생성 (Gradle 9.3.0)
- [x] `gradle/wrapper/gradle-wrapper.jar` 생성
- [x] `gradlew`, `gradlew.bat` 생성

### 1-2. 빌드 스크립트 작성
- [x] `settings.gradle` 생성 (`rootProject.name = 'assemblyCar'`)
- [x] `build.gradle` 생성
  - [x] `java` 플러그인, `application` 플러그인
  - [x] `sourceCompatibility = 17`
  - [x] `mainClass = 'Assemble'`
  - [x] JUnit 5 의존성 (`junit-jupiter`)
  - [x] JaCoCo 플러그인

### 1-3. 디렉토리 구조 재편
- [x] `src/main/java/` 생성
- [x] `src/test/java/` 생성
- [x] `java/Assemble.java` → `src/main/java/Assemble.java` 이동
- [x] `java/` 폴더 제거

### 1-4. .gitignore 업데이트
- [x] `.gradle/` 추가
- [x] `build/` 추가

### 1-5. 빌드 검증
- [x] `./gradlew compileJava` — 컴파일 확인
- [x] `./gradlew run` — 실행 확인

---

## Phase 2 — Unit Test 작성 (리팩토링 전 안전망)

> 기존 `Assemble.java` 코드 기준.
> ⚠️ 현재 테스트(`AssembleTest.java`)는 `stack`, `isValidCheck()` 등 구현 세부사항에 직접 결합 — **리팩토링 내성 없음**.
> Phase 5 완료 후 `CompatibilityCheckerTest.java`(행동 기반)로 교체 예정. 현재는 커버리지 확보용 임시 안전망.

### 2-1. 테스트 클래스 생성
- [x] `src/test/java/AssembleTest.java` 생성
- [x] `@BeforeEach` 에서 `stack` 초기화 방법 결정
  - `stack`, `isValidCheck()`, `runProducedCar()` → `package-private` 으로 변경

### 2-2. 호환성 실패 케이스 테스트 (isValidCheck)
- [x] `sedan_with_continental_brake_fails`
- [x] `suv_with_toyota_engine_fails`
- [x] `truck_with_wia_engine_fails`
- [x] `truck_with_mando_brake_fails`
- [x] `bosch_brake_requires_bosch_steering_fails`

### 2-3. 정상 케이스 테스트
- [x] `valid_combination_passes`
- [x] `broken_engine_does_not_run`

### 2-4. 테스트 실행 및 Coverage 측정 (1차)
- [x] `./gradlew test` — 7개 전체 통과
- [x] `./gradlew jacocoTestReport` 실행
- [x] 1차 coverage: Instructions 11%, Branches 16% — 너무 낮음

### 2-5. Coverage 보강 (isValidRange, testProducedCar, runProducedCar 추가)
- [x] `isValidRange()` → `package-private` 변경
- [x] `testProducedCar()` → `package-private` 변경
- [x] `isValidRange()` 테스트 — 각 step별 유효/무효 범위
  - [x] CarType: 유효(1~3), 무효(0, 4)
  - [x] Engine: 유효(0~4), 무효(5)
  - [x] BrakeSystem: 유효(0~3), 무효(4)
  - [x] SteeringSystem: 유효(0~2), 무효(3)
  - [x] RunTest: 유효(0~2), 무효(3)
- [x] `testProducedCar()` 테스트 — 5개 FAIL + 1개 PASS
- [x] `runProducedCar()` 성공/실패/고장 경로 출력 확인
- [x] `./gradlew test` 전체 통과 (25개)
- [x] coverage 재측정: Instructions 47%, Branches 51%

---

## Phase 3 — enum 정의

### 3-1. CarType enum
- [x] `src/main/java/model/CarType.java`
  - 값: `SEDAN`, `SUV`, `TRUCK`
  - 표시 이름 필드 (`displayName`)

### 3-2. Engine enum
- [x] `src/main/java/model/Engine.java`
  - 값: `GM`, `TOYOTA`, `WIA`, `BROKEN`
  - `isBroken()` 메서드

### 3-3. BrakeSystem enum
- [x] `src/main/java/model/BrakeSystem.java`
  - 값: `MANDO`, `CONTINENTAL`, `BOSCH`

### 3-4. SteeringSystem enum
- [x] `src/main/java/model/SteeringSystem.java`
  - 값: `BOSCH`, `MOBIS`
- [x] `Assemble.java` int 상수 블록 제거, enum + displayName 사용으로 교체
- [x] `./gradlew test` 25개 전체 통과

---

## Phase 4 — Car 도메인 모델

### 4-1. Car 클래스
- [x] `src/main/java/model/Car.java`
  - 필드: `CarType`, `Engine`, `BrakeSystem`, `SteeringSystem`
  - getter / setter
  - `isFullyConfigured()` — 4개 부품 모두 선택 여부
  - `reset()` — 처음 화면 복귀 시 상태 초기화

### 4-2. 기존 `int[] stack` 제거 검증
- [x] `Assemble.java` 에서 `stack` → `Car car` 객체로 교체
- [x] `AssembleTest.java` — `stack` 접근 → `car` 객체 기준으로 수정
- [x] `./gradlew test` 25개 전체 통과

---

## Phase 5 — 호환성 규칙 분리

### 5-1. CompatibilityRule 인터페이스
- [x] `src/main/java/rule/CompatibilityRule.java`
  - `@FunctionalInterface`
  - `Optional<String> check(Car car)` — 위반 시 메시지 반환

### 5-2. CompatibilityChecker 클래스
- [x] `src/main/java/rule/CompatibilityChecker.java`
  - `RULES` — 규칙 5개 `List<CompatibilityRule>` 정의
  - `List<String> validate(Car car)` — 위반 메시지 목록 반환
  - `boolean isValid(Car car)` — 전체 통과 여부
- [x] 기존 `isValidCheck()` 제거
- [x] 기존 `testProducedCar()` 중복 규칙 제거 → `CompatibilityChecker` 사용
- [x] `InterruptedException` → `Thread.currentThread().interrupt()` 복원

### 5-3. CompatibilityChecker 테스트 작성 (행동 기반 — 리팩토링 내성 있음)
- [x] `src/test/java/rule/CompatibilityCheckerTest.java`
  - `Car` 객체 직접 생성 — `Assemble` 내부 무관
  - 5개 FAIL + 4개 PASS + 3개 메시지 검증 (총 12개)
- [x] 기존 `AssembleTest.java` 제거
- [x] `./gradlew test` 전체 통과

---

## Phase 6 — UI / Service 분리

### 6-1. ConsoleUI 클래스
- [x] `src/main/java/ui/ConsoleUI.java`
  - `displayMenu`, `displayMenuWithBack` — 공통 메뉴 출력 (show*Menu 4개 통합)
  - `readLine()` — "INPUT > " 포함 입력 처리
  - `printCarBanner()`, `clearScreen()`, `print()`, `printf()`

### 6-2. CarAssembler 서비스
- [x] `src/main/java/service/CarAssembler.java`
  - `run()` — 전체 조립 흐름 제어
  - `ConsoleUI` 생성자 주입
  - `delay()` protected — 테스트 시 override 가능

### 6-3. Assemble.java 진입점 정리
- [x] 3줄로 축소 (`new CarAssembler(new ConsoleUI(new Scanner(System.in))).run()`)

### 6-4. CarAssemblerTest 추가
- [x] `src/test/java/service/CarAssemblerTest.java` (StubUI, 12개 테스트 — BasicFlow/RunPath/TestPath/BackNavigation 4 Nested)
- [x] `src/test/java/model/CarTest.java` 추가 (3개 테스트 — isFullyConfigured/reset)
- [x] `./gradlew test` 전체 통과
- [x] coverage: Instructions 81%, Branches 80%

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
