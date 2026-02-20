package com.gmg.seatnow.presentation.user.term

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.TermItem
import com.gmg.seatnow.presentation.theme.*

@Composable
fun UserTermsScreen(
    viewModel: UserTermsViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // 상세 화면이 열려있으면 뒤로가기 시 상세 화면 닫기
    BackHandler(enabled = uiState.openedTermType != null) {
        viewModel.closeDetail()
    }

    // 화면 전환 애니메이션 (리스트 <-> 상세)
    Crossfade(targetState = uiState.openedTermType, label = "UserTermsTransition") { termType ->
        if (termType != null) {
            // 상세 화면
            UserTermsDetailScreen(
                termType = termType,
                onBackClick = { viewModel.closeDetail() }
            )
        } else {
            // 약관 리스트 화면
            UserTermsListContent(
                uiState = uiState,
                onToggleAll = viewModel::toggleAll,
                onToggleTerm = viewModel::toggleTerm,
                onOpenDetail = viewModel::openDetail,
                onNavigateToBack = onNavigateToBack,
                onNavigateToMain = onNavigateToMain,
                onSaveAndNavigate = { // 뷰모델 저장 로직 호출용
                    // ViewModel에서 저장 로직은 onNavigateToMain 호출 전 NavGraph에서 처리하거나 여기서 처리
                    // 여기서는 단순히 메인 이동 콜백만 호출 (NavGraph에서 처리하도록 되어있음)
                    onNavigateToMain()
                }
            )
        }
    }
}

@Composable
fun UserTermsListContent(
    uiState: UserTermsUiState,
    onToggleAll: (Boolean) -> Unit,
    onToggleTerm: (UserTermType) -> Unit,
    onOpenDetail: (UserTermType) -> Unit,
    onNavigateToBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    onSaveAndNavigate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .systemBarsPadding()
    ) {
        // 1. 상단 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "약관 동의",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = SubBlack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onNavigateToBack() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 전체 동의
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleAll(!uiState.isAllChecked) } // 전체 영역 클릭 가능
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = uiState.isAllChecked,
                onCheckedChange = { onToggleAll(it) },
                colors = CheckboxDefaults.colors(
                    checkedColor = PointRed,
                    uncheckedColor = SubLightGray,
                    checkmarkColor = White
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "서비스 이용약관 모두 동의",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp),
            thickness = 1.dp,
            color = SubPaleGray
        )

        // 3. 약관 리스트
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            TermItem(
                title = UserTermType.AGE.title,
                isChecked = uiState.isAgeChecked,
                showArrow = false,
                onToggle = { onToggleTerm(UserTermType.AGE) }
            )
            TermItem(
                title = UserTermType.SERVICE.title,
                isChecked = uiState.isServiceChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.SERVICE) },
                onDetailClick = { onOpenDetail(UserTermType.SERVICE) } // 화살표 클릭 시 상세 이동
            )
            TermItem(
                title = UserTermType.PRIVACY_COLLECT.title,
                isChecked = uiState.isPrivacyCollectChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.PRIVACY_COLLECT) },
                onDetailClick = { onOpenDetail(UserTermType.PRIVACY_COLLECT) }
            )
            TermItem(
                title = UserTermType.PRIVACY_PROVIDE.title,
                isChecked = uiState.isPrivacyProvideChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.PRIVACY_PROVIDE) },
                onDetailClick = { onOpenDetail(UserTermType.PRIVACY_PROVIDE) }
            )
            TermItem(
                title = UserTermType.LOCATION.title,
                isChecked = uiState.isLocationChecked,
                showArrow = true,
                onToggle = { onToggleTerm(UserTermType.LOCATION) },
                onDetailClick = { onOpenDetail(UserTermType.LOCATION) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. 하단 버튼
        Button(
            onClick = onSaveAndNavigate,
            enabled = uiState.isNextEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .height(52.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PointRed,
                disabledContainerColor = SubLightGray,
                contentColor = White,
                disabledContentColor = White
            )
        ) {
            Text(
                text = "동의하고 시작하기",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

// 상세 약관 화면
@Composable
fun UserTermsDetailScreen(
    termType: UserTermType,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = termType.title.replace("[필수]", "").trim(),
                onBackClick = onBackClick,
                topMargin = 15.dp
            )
        },
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            val content = when(termType) {
                UserTermType.SERVICE -> getServiceTermsMock()
                UserTermType.PRIVACY_COLLECT -> getPrivacyCollectMock()
                UserTermType.PRIVACY_PROVIDE -> getPrivacyProvideMock()
                UserTermType.LOCATION -> getLocationTermsMock() // 위치기반 추가
                else -> "내용이 없습니다."
            }

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = SubBlack,
                lineHeight = 24.sp
            )
        }
    }
}

// --- Mock Data Generators (Step1BasicScreen에서 쓴 것과 동일 + 위치기반 추가) ---

fun getServiceTermsMock() = """
제1조 (목적)
본 약관은 시트나우(SEAT NOW)가 제공하는 모바일 애플리케이션 및 관련 서비스(이하 “서비스”)의 이용과 관련하여 시트나우와 이용자 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.

제2조 (용어의 정의)
“서비스”란 시트나우가 제공하는 주점·술집 등의 좌석 현황, 혼잡도, 위치, 영업 정보 등 정보 제공 서비스를 의미합니다.
“이용자”란 본 약관에 동의하고 서비스를 이용하는 회원 및 비회원을 말합니다.
“회원”이란 시트나우가 정한 절차에 따라 계정을 생성하고 서비스를 이용하는 자를 말합니다.
“제휴매장”이란 시트나우와 협력하여 매장 정보 또는 좌석 관련 정보를 제공하는 외부 사업자를 말합니다.

제3조 (약관의 효력 및 변경)
본 약관은 서비스 화면에 게시하거나 기타의 방법으로 이용자에게 공지함으로써 효력이 발생합니다.
시트나우는 관련 법령을 위반하지 않는 범위에서 약관을 개정할 수 있습니다.
약관을 개정하는 경우 적용일자 및 개정 사유를 명시하여 서비스 내 공지사항을 통해 사전에 공지합니다.
이용자가 개정 약관에 동의하지 않을 경우 서비스 이용을 중단할 수 있으며, 개정 약관 시행 이후에도 서비스를 계속 이용하는 경우 개정 약관에 동의한 것으로 간주합니다.

제4조 (서비스의 제공)
시트나우는 다음과 같은 서비스를 제공합니다.
주점·술집 좌석 현황 및 혼잡도 정보 제공
매장 위치, 영업시간, 연락처 등 기본 정보 제공
기타 시트나우가 정하는 부가 서비스
서비스는 연중무휴 제공을 원칙으로 하나, 시스템 점검 또는 운영상 필요에 따라 일시적으로 중단될 수 있습니다.

제5조 (서비스 정보의 한계 및 면책)
시트나우가 제공하는 좌석 현황 및 혼잡도 정보는 제휴매장 또는 이용자가 제공한 정보를 기반으로 하며, 실제 현장 상황과 차이가 발생할 수 있습니다.
시트나우는 좌석의 실제 이용 가능 여부, 입장 가능성, 대기 시간 등을 보장하지 않습니다.
이용자가 서비스를 참고하여 매장을 방문함으로써 발생한 불이익에 대하여, 시트나우는 고의 또는 중과실이 없는 한 책임을 지지 않습니다.

제6조 (서비스의 중단)
시트나우는 다음 각 호의 사유가 발생한 경우 서비스 제공을 일시적으로 중단할 수 있습니다.
시스템 점검, 보수 또는 교체
통신 장애
기타 불가항력적 사유
시트나우는 고의 또는 중과실이 없는 한 서비스 중단으로 인하여 발생한 손해에 대해 책임을 지지 않습니다.

제7조 (회원가입 및 관리)
이용자는 시트나우가 정한 절차에 따라 회원가입을 신청할 수 있습니다.
다음 각 호에 해당하는 경우 회원가입이 제한되거나 서비스 이용이 제한될 수 있습니다.
허위 정보 기재
타인의 정보 도용
서비스 운영을 방해하는 행위

제8조 (이용자의 의무)
이용자는 다음 행위를 하여서는 안 됩니다.
허위 정보의 등록 또는 타인의 정보 도용
서비스의 정상적인 운영을 방해하는 행위
법령 또는 공서양속에 반하는 행위
시트나우 또는 제3자의 지적재산권을 침해하는 행위

제9조 (개인정보 보호)
시트나우는 이용자의 개인정보를 관련 법령 및 개인정보 처리방침에 따라 보호합니다.
개인정보의 수집·이용·보관·파기에 관한 사항은 별도의 개인정보 처리방침 및 개인정보 수집·이용 동의에 따릅니다.

제10조 (지적재산권)
서비스 및 서비스 내 콘텐츠에 대한 저작권은 시트나우에 귀속됩니다.
이용자는 사전 동의 없이 서비스 내 정보를 복제, 배포, 상업적으로 이용할 수 없습니다.

제11조 (분쟁 해결)
시트나우는 이용자가 제기하는 의견 및 불만 사항을 성실히 처리합니다.
서비스 이용과 관련하여 발생한 분쟁은 상호 협의를 통해 해결함을 원칙으로 합니다.

제12조 (준거법 및 관할)
본 약관은 대한민국 법률을 준거법으로 합니다.
서비스 이용과 관련하여 발생한 분쟁에 관한 소송은 민사소송법상 관할 법원에 제기합니다.

부칙
본 약관은 2026년 01월 17일부터 시행합니다.
""".trimIndent()

fun getPrivacyCollectMock() = """
시트나우(이하 “본 서비스”)는 서비스 제공을 위하여 아래와 같이 개인정보를 수집·이용합니다.

1. 수집 항목
① 공통 수집 항목 (이용자 및 사장님 공통)
이메일
휴대폰번호
서비스 이용 기록
접속 로그

② 위치 기반 서비스 이용 시
위치정보

③ 사장님 회원의 경우 추가 수집 항목
대표자명
사업자등록번호
매장 주소
매장 연락처
사업자등록증 파일(선택)
※ 본 서비스는 민감정보(사상·신념, 건강정보 등) 및 고유식별정보를 수집하지 않습니다.

2. 이용 목적
회원 식별 및 서비스 제공
주변 술집·음식점 정보 및 좌석 현황 제공
사장님 매장 정보 등록 및 관리
고객 문의 대응 및 민원 처리
서비스 이용 통계 분석 및 품질 개선
부정 이용 방지 및 서비스 안정성 확보

3. 보유 및 이용 기간
원칙적으로 회원 탈퇴 시까지 보유·이용합니다.
단, 관련 법령에 따라 보관이 필요한 경우 해당 법령에서 정한 기간 동안 보관합니다.

4. 동의 거부 권리 및 불이익 안내
이용자는 개인정보 수집·이용에 대한 동의를 거부할 수 있습니다.
다만, 필수 개인정보 수집·이용에 동의하지 않을 경우 서비스 이용이 제한될 수 있습니다.
""".trimIndent()

fun getPrivacyProvideMock() = """
시트나우(이하 “본 서비스”)는 이용자의 개인정보를 매우 중요하게 생각하며, 「개인정보 보호법」, 「정보통신망 이용촉진 및 정보보호 등에 관한 법률」, 「위치정보의 보호 및 이용 등에 관한 법률」 등 관련 법령을 준수합니다.
본 개인정보처리방침은 이용자가 본 서비스를 이용함에 있어 제공하는 개인정보가 어떠한 목적과 방식으로 처리되는지, 그리고 개인정보 보호를 위하여 어떠한 조치가 이루어지고 있는지를 안내하기 위하여 마련되었습니다.

제1조 (수집하는 개인정보 항목 및 수집 방법)
1. 수집하는 개인정보 항목
본 서비스는 서비스 제공을 위해 아래와 같은 개인정보를 수집·이용합니다.

[수집 목적 / 수집 항목 / 보유 및 이용기간]
- 사장님 이메일 회원가입, 회원 식별, 서비스 이용(매장 관리 서비스 제공) / 이메일, 휴대폰번호, 대표자명, 사업자등록번호, 주소, 가게 연락처, 사업자등록증 파일(선택) / 회원 탈퇴 시까지
- 타사 계정(카카오)를 이용한 사용자 회원가입, 회원 식별, 서비스 이용 / 타사(카카오)로부터 제공받는 정보: 이메일, 카카오 계정 식별자, 휴대폰번호 / 회원 탈퇴 시까지
- 좌석 탐색 및 서비스 제공 / 서비스 이용 기록, 접속 로그 / 이용 목적 달성 후 즉시 파기
- 위치 기반 정보 제공 / 개인위치정보 / 이용 목적 달성 후 즉시 파기
- 고객 문의 및 민원 처리 / 이메일, 문의 내용 / 처리 완료 후 3년
※ 본 서비스는 민감정보(사상·신념, 건강정보 등) 및 고유식별정보를 수집하지 않습니다.

2. 개인정보 수집 방법
회원가입 및 서비스 이용 과정에서 이용자가 직접 입력
모바일 기기 접근 권한 승인
서비스 이용 과정에서 자동 생성되는 정보 수집

제2조 (개인정보의 이용 목적)
본 서비스는 수집한 개인정보를 다음 목적을 위해 이용합니다.
회원 식별 및 서비스 제공
주변 술집·음식점 좌석 정보 제공
위치 기반 서비스 제공
서비스 이용 통계 분석 및 품질 개선
고객 문의 대응 및 민원 처리
부정 이용 방지 및 서비스 안정성 확보

제3조 (개인정보의 보유 및 이용기간)
① 원칙적으로 개인정보는 수집·이용 목적 달성 시 지체 없이 파기합니다.
② 단, 관계 법령에 따라 보관이 필요한 경우 아래와 같이 보관합니다.
- 서비스 이용 기록 (통신비밀보호법) : 3개월
- 소비자 불만 및 분쟁 기록 (전자상거래법) : 3년
- 접속 로그 (통신비밀보호법) : 3개월

제4조 (개인정보의 제3자 제공)
본 서비스는 원칙적으로 이용자의 개인정보를 제3자에게 제공하지 않습니다.
다만, 아래의 경우에 한하여 제공될 수 있습니다.
이용자가 사전에 동의한 경우
법령에 따라 제공이 요구되는 경우
이용자의 생명·신체 안전에 긴급한 위험이 있는 경우

제5조 (개인정보 처리의 위탁)
본 서비스는 원활한 서비스 제공을 위하여 일부 업무를 외부 업체에 위탁할 수 있습니다.
위탁 시, 개인정보 보호 관련 법령에 따라 안전하게 관리·감독합니다.
(현재 위탁 중인 사항이 없는 경우, 추후 발생 시 본 방침을 통해 공개합니다.)

제6조 (개인정보의 파기 절차 및 방법)
1. 파기 절차
수집 목적 달성 후 별도 저장 공간으로 이동 후 일정 기간 보관
보관 기간 종료 시 파기
2. 파기 방법
전자적 파일: 복구 불가능한 기술적 방법으로 삭제
출력물: 분쇄 또는 소각

제7조 (이용자의 권리와 행사 방법)
이용자는 언제든지 다음 권리를 행사할 수 있습니다.
개인정보 열람 요청
개인정보 정정·삭제 요청
개인정보 처리 정지 요청
회원 탈퇴 요청
요청은 서비스 내 설정 또는 고객 문의를 통해 가능합니다.

제8조 (개인정보의 안전성 확보 조치)
본 서비스는 개인정보 보호를 위해 다음과 같은 조치를 취하고 있습니다.
개인정보 암호화
접근 권한 최소화
보안 시스템 운영
정기적인 점검 및 관리

제9조 (개인위치정보의 처리)
본 서비스는 위치정보법에 따라 개인위치정보를 처리합니다.
1. 처리 목적: 현 위치 기반 주변 술집·음식점 좌석 정보 제공
2. 보유 기간: 위치 기반 서비스 제공 이용 목적 달성 후 즉시 파기
3. 제3자 제공: 이용자의 사전 동의 없이 개인위치정보를 제3자에게 제공하지 않습니다.

제10조 (개인정보 보호 책임자)
개인정보 보호 관련 문의는 아래 이메일을 통해 접수할 수 있습니다.
이메일: seatnow2026@gmail.com

제11조 (개인정보처리방침의 변경)
본 개인정보처리방침은 법령 또는 서비스 정책 변경에 따라 수정될 수 있으며, 변경 시 서비스 내 공지사항을 통해 사전 안내합니다.

부칙
본 개인정보처리방침은 2026년 01월 17일부터 시행됩니다.
""".trimIndent()

fun getLocationTermsMock() = """
시트나우(이하 “본 서비스”)는 「위치정보의 보호 및 이용 등에 관한 법률」 등 관련 법령을 준수하며, 이용자의 개인위치정보를 안전하게 보호하기 위하여 본 위치정보 이용약관을 마련합니다.

본 약관은 본 서비스가 제공하는 위치 기반 서비스 이용과 관련하여 이용자와 본 서비스 간의 권리·의무 및 기타 필요한 사항을 규정합니다.

## 제1조 (목적)

본 약관은 본 서비스가 제공하는 위치 기반 서비스와 관련하여 개인위치정보의 수집·이용 및 보호에 관한 사항을 규정함을 목적으로 합니다.

## 제2조 (용어의 정의)

본 약관에서 사용하는 용어의 의미는 다음과 같습니다.

1. **개인위치정보**란 특정 개인의 위치를 알 수 있는 정보로서 위치정보법에서 정의하는 정보를 말합니다.
2. **이용자**란 본 약관에 동의하고 본 서비스의 위치 기반 서비스를 이용하는 자를 말합니다.
3. **위치 기반 서비스**란 개인위치정보를 이용하여 제공하는 주변 술집·음식점 정보 및 좌석 현황 안내 서비스를 말합니다.

## 제3조 (위치정보의 수집 및 이용 목적)

본 서비스는 다음 목적을 위하여 개인위치정보를 수집·이용합니다.

1. 이용자의 현재 위치를 기준으로 주변 술집·음식점 정보 제공
2. 위치 기반 좌석 탐색 기능 제공
3. 서비스 품질 개선 및 기능 고도화

## 제4조 (개인위치정보의 수집 방법)

본 서비스는 다음 방법으로 개인위치정보를 수집합니다.

1. 모바일 단말기의 위치정보 수집 기능을 통한 수집
2. 이용자가 위치정보 이용에 동의한 경우에 한하여 수집

## 제5조 (개인위치정보의 보유 및 이용 기간)

본 서비스는 개인위치정보를 위치 기반 서비스 제공 목적 달성 후 즉시 파기합니다.

단, 관련 법령에 따라 보관이 필요한 경우에는 해당 법령에서 정한 기간 동안 보관합니다.

## 제6조 (개인위치정보의 제3자 제공)

본 서비스는 이용자의 개인위치정보를 제3자에게 제공하지 않습니다.

다만, 다음 각 호의 경우에는 예외로 합니다.

1. 이용자가 사전에 동의한 경우
2. 법령에 따라 제공이 요구되는 경우
3. 이용자의 생명 또는 신체의 안전을 위하여 긴급히 필요한 경우

## 제7조 (개인위치정보의 파기 절차 및 방법)

1. **파기 절차**
    
    개인위치정보는 이용 목적 달성 즉시 파기합니다.
    
2. **파기 방법**
    
    전자적 파일 형태의 정보는 복구 불가능한 방법으로 삭제합니다.
    

## 제8조 (이용자의 권리)

이용자는 언제든지 개인위치정보의 수집·이용에 대한 동의를 철회할 수 있습니다.

이용자는 개인위치정보에 대해 다음 권리를 행사할 수 있습니다.

1. 개인위치정보 수집·이용에 대한 동의 철회
2. 개인위치정보의 열람 또는 고지 요구

권리 행사는 서비스 내 설정 또는 고객 문의를 통해 가능합니다.

## 제9조 (법정대리인의 권리)

14세 미만 이용자의 경우, 개인위치정보 수집·이용은 법정대리인의 동의를 받아야 합니다.

법정대리인은 14세 미만 이용자의 개인위치정보 처리와 관련하여 이용자 본인과 동일한 권리를 가집니다.

## 제10조 (위치정보 관리 책임자)

본 서비스는 개인위치정보 보호를 위하여 다음과 같이 위치정보 관리 책임자를 지정합니다.

- 위치정보 관리 책임자: 시트나우 운영자
- 이메일 문의: [seatnow2026@gmail.com](mailto:seatnow2026@gmail.com)

## 제11조 (약관의 변경)

본 약관은 법령 또는 서비스 정책 변경에 따라 수정될 수 있습니다.

약관이 변경되는 경우, 본 서비스는 사전에 서비스 내 공지를 통해 안내합니다.

### 부칙

본 위치정보 이용약관은 2026년 01월 17일부터 시행됩니다.""".trimIndent()