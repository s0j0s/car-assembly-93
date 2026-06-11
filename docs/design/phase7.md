# Phase 7 — 최종 검증 설계

## 목표
리팩토링 완료 후 품질 지표 최종 확인 및 프로젝트 마무리.

---

## 7-1. 테스트 및 Coverage 최종 확인

### 실행 명령
```bash
./gradlew test jacocoTestReport
```

### 확인 항목
| 항목 | 기준 | 현재 |
|------|------|------|
| 전체 테스트 통과 | 0 failures | 27개 (12+12+3) |
| Instructions coverage | ≥ 80% | 81% |
| Branches coverage | ≥ 75% | 80% |

### Phase 2 baseline 대비
| 지표 | Phase 2 (1차) | Phase 2 (보강) | Phase 7 (현재) |
|------|--------------|---------------|---------------|
| Instructions | 11% | 47% | 81% |
| Branches | 16% | 51% | 80% |

---

## 7-2. 코드 스멜 체크리스트

CLAUDE.md의 17개 코드 스멜 → 리팩토링 후 해소 현황.

| # | 스멜 | 해소 방법 | 상태 |
|---|------|-----------|------|
| 1 | God Class | `model/`, `rule/`, `service/`, `ui/` 4개 패키지로 분리 | ✅ |
| 2 | Primitive Obsession | `int[]` → enum 4개 + `Car` 객체 | ✅ |
| 3 | Data Clump | `stack[0..3]` → `Car` 단일 도메인 모델 | ✅ |
| 4 | Long Method (main 89줄) | `main()` 3줄 → `CarAssembler.run()` 위임 | ✅ |
| 5 | Switch Statements (메뉴) | `ConsoleUI.displayMenu/displayMenuWithBack` 통합 | ✅ |
| 6 | Switch Statements (선택) | enum index 직접 매핑으로 축소 | ✅ |
| 7 | Switch Statements (범위 검증) | `isValidRange()` — enum length 활용으로 축소 | ✅ |
| 8 | Feature Envy (select*() 4개) | `Car` setter 직접 호출 | ✅ |
| 9 | Duplicate Code (메뉴 출력 4개) | `ConsoleUI.displayMenu/displayMenuWithBack` 공통화 | ✅ |
| 10 | Duplicate Code (int→이름 ternary) | `enum.displayName` 필드 직접 참조 | ✅ |
| 11 | Duplicate Code (이름 매핑 재반복) | `namesOf()` 제네릭 헬퍼로 단일화 | ✅ |
| 12 | Duplicate Code (호환성 규칙 중복) | `CompatibilityChecker` 단일 정의 | ✅ |
| 13 | Divergent Change | `CompatibilityChecker.RULES` 한 곳만 수정 | ✅ |
| 14 | Inappropriate Intimacy | `stack[idx]` 제거 → `Car` getter/setter | ✅ |
| 15 | Magic Numbers (부품 상수 int) | enum 상수로 교체 | ✅ |
| 16 | Magic Numbers (`4` = 고장 엔진) | `Engine.BROKEN` | ✅ |
| 17 | Dead Code (InterruptedException 묵살) | `Thread.currentThread().interrupt()` 복원 | ✅ |

---

## 7-3. 빌드 산출물 확인

### .gitignore 반영 여부
```
.gradle/
build/
*.class
```
→ `build/`, `.gradle/` 추적 제외 확인.

### 커밋 포함 확인
```
src/main/java/
  Assemble.java
  model/{CarType,Engine,BrakeSystem,SteeringSystem,Car}.java
  rule/{CompatibilityRule,CompatibilityChecker}.java
  service/CarAssembler.java
  ui/ConsoleUI.java
src/test/java/
  rule/CompatibilityCheckerTest.java
  service/CarAssemblerTest.java
  model/CarTest.java
build.gradle
settings.gradle
gradle/wrapper/
gradlew, gradlew.bat
docs/PLAN.md
docs/design/phase*.md
CLAUDE.md
```

---

## 7-4. 최종 Commit & Push

### 커밋 전략
Phase 7은 테스트/빌드 결과 확인 및 문서 업데이트가 주 내용 → **별도 커밋 불필요**.  
PLAN.md Phase 7 체크리스트 완료 표시 후 단일 커밋.

### 커밋 메시지
```
docs: Phase 7 완료 — 코드 스멜 17개 해소, 커버리지 81%/80% 확인
```

---

## 최종 구조 비교

| 항목 | Before | After |
|------|--------|-------|
| 파일 수 | 1 (`Assemble.java`) | 10 (main) + 3 (test) |
| 테스트 | 0 | 27개 |
| 빌드 도구 | plain javac | Gradle 9.3.0 |
| Coverage | 0% | Instructions 81%, Branches 80% |
| 타입 안전성 | int[] | enum + Car |
| 호환성 규칙 위치 | 2곳 중복 | 1곳 (CompatibilityChecker) |
