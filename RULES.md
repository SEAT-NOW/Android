# 🏗️ SeatNow Android Architecture & Code Convention Rules

## 📌 0. Core Philosophy (AI Agent 지침)
본 프로젝트는 **Clean Architecture**와 **MVI (Model-View-Intent) 패턴**을 엄격하게 준수한다.
이 문서(RULES.md)를 읽는 AI 어시스턴트는 코드 생성, 리팩토링, 코드 리뷰 시 **반드시 아래의 규칙을 최우선으로 적용**해야 한다.

---

## 🚫 Rule 1. 도메인 계층 격리 (Repository 직접 참조 절대 금지)
Presentation Layer(ViewModel)는 Data Layer(Repository)의 존재를 알아서는 안 된다. 모든 데이터 접근은 반드시 Domain Layer의 **UseCase**를 거쳐야 한다.

* **[DON'T]** ViewModel에 Repository 직접 주입 금지
    ```kotlin
    // ❌ 절대 금지
    class MyViewModel @Inject constructor(private val repository: SeatRepository) 
    ```
* **[DO]** UseCase를 통한 접근
    ```kotlin
    // 🟢 필수 적용
    class MyViewModel @Inject constructor(private val getSeatConfigUseCase: GetSeatConfigurationUseCase)
    ```

## 🧠 Rule 2. 비즈니스 로직의 도메인 캡슐화 (로직 중복 제거)
계산, 검증, 데이터 가공 등의 순수 비즈니스 로직은 ViewModel 내부에 작성하지 않는다. 반드시 `Domain/UseCase` 패키지에 별도의 UseCase 클래스로 분리하여 주입받아 사용한다.

* **[DON'T]** ViewModel 내부에 계산/검증 함수 작성 금지
    ```kotlin
    // ❌ 절대 금지 (ViewModel 내부에 로직 존재)
    private fun calculateTotalSeats(tables: List<Table>): Int { ... }
    ```
* **[DO]** UseCase 재사용
    ```kotlin
    // 🟢 필수 적용
    private val calculateSeatCountUseCase: CalculateSeatCountUseCase
    val total = calculateSeatCountUseCase(tables)
    ```

## 📜 Rule 3. MVI Contract 분리 (ViewModel 종속성 제거)
UI 상태(`UiState`), 이벤트(`Event`), 사용자 액션(`Action`)은 **반드시 `*Contract.kt` 라는 별도의 파일로 분리**해야 한다. ViewModel 내부에 중첩(Nested) 클래스로 선언하는 것을 엄격히 금지한다.

* **[DON'T]** ViewModel 내부에 State 선언 금지
    ```kotlin
    // ❌ 절대 금지
    class MyViewModel : ViewModel() {
        data class MyUiState(...) 
        sealed interface MyAction { ... }
    }
    ```
* **[DO]** 별도의 Contract 파일 (`MyPageContract.kt`) 생성
    ```kotlin
    // 🟢 필수 적용 (MyPageContract.kt)
    data class MyPageUiState(...)
    sealed interface MyPageAction { ... }
    sealed interface MyPageEvent { ... }
    ```

## 🧺 Rule 4. UI 상태 다이어트 (바구니/Grouping 패턴)
`UiState` 데이터 클래스 내부에 수십 개의 변수를 평면적(Flat)으로 나열하지 않는다. 반드시 연관된 상태들을 성격에 맞는 도메인별 Data Class(바구니)로 그룹화하여 계층 구조를 만든다.

* **[DON'T]** 변수 무한 나열 금지
    ```kotlin
    // ❌ 절대 금지
    data class MyPageUiState(
        val email: String = "",
        val phone: String = "",
        val storeName: String = "",
        val spaceList: List<SpaceItem> = emptyList()
    )
    ```
* **[DO]** 상태 그룹화 (바구니 패턴)
    ```kotlin
    // 🟢 필수 적용
    data class MyPageUiState(
        val isLoading: Boolean = false, // 최상위 공통 상태
        val account: AccountState = AccountState(),
        val storeInfo: StoreInfoState = StoreInfoState(),
        val seatConfig: SeatConfigState = SeatConfigState()
    )
    ```

## 🎨 Rule 5. UI 화면의 독립성 확보 (Stateless Compose UI)
Jetpack Compose 기반의 UI 화면(`@Composable`) 컴포넌트는 ViewModel 객체 자체를 매개변수로 받아서는 안 된다. 오직 상태(`UiState`)와 액션 콜백(`(Action) -> Unit`)만 주입받아 완벽한 Stateless 상태를 유지해야 한다. 이를 통해 Preview 생성을 보장한다.

* **[DON'T]** Composable에 ViewModel 주입 금지
    ```kotlin
    // ❌ 절대 금지
    @Composable
    fun EditScreen(viewModel: MyViewModel = hiltViewModel()) { ... }
    ```
* **[DO]** State와 Action Lambda만 전달 (진정한 단방향 데이터 흐름)
    ```kotlin
    // 🟢 필수 적용
    @Composable
    fun EditScreen(
        uiState: MyPageUiState,
        onAction: (MyPageAction) -> Unit,
        onBackClick: () -> Unit
    ) { ... }
    ```

## 🛑 Rule 6. 기능 보존의 원칙 (로직 임의 변경 금지)
이 프로젝트에서의 AI의 역할은 철저하게 Clean Architecture와 MVI 패턴을 준수하는 "구조적 리팩토링"에 한정된다.
로직을 최적화한다는 명목으로 기존 비즈니스 로직(결과값 도출 방식, 분기 처리 등)의 흐름을 임의로 수정하거나 생략하거나 새로운 기능을 추가하는 행위를 절대 금지한다.
클래스 분리, 상태(State) 이동, 의존성 방향 조절 등 아키텍처 관점의 수정이 완료된 후에도 기존의 기능은 이전과 100% 동일하게 동작해야 한다.

* **[DON'T]** 리팩토링 중 기존에 없던 예외 처리 임의 추가, 로직 결과값 변경, 새로운 기능(기능 개선 포함) 구현
* **[DO]** 기존 레거시 코드의 기조를 완벽히 유지하며 '정해진 아키텍처 컨벤션으로의 구조적 이관'에만 집중

---
**[AI Agent Action Item]**
새로운 화면(Screen)이나 기능을 추가하라는 프롬프트를 받으면, 코드를 작성하기 전에 반드시 다음 순서를 따른다:
1. `*Contract.kt` 파일을 생성하여 Grouping된 State와 Action을 정의한다.
2. `ViewModel`을 작성하되, Repository를 직접 주입받지 않고 필요한 `UseCase`를 도메인 계층에 정의한다.
3. `Compose UI`를 작성할 때 ViewModel을 배제하고 Contract의 State와 Action만으로 UI를 구성한다.
