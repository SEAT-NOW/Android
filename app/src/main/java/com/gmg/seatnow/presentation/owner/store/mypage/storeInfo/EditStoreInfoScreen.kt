package com.gmg.seatnow.presentation.owner.store.mypage.storeInfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.formatBusinessNumber
import com.gmg.seatnow.presentation.component.formatPhoneNumber
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel
import com.gmg.seatnow.presentation.theme.SubBlack
import com.gmg.seatnow.presentation.theme.SubDarkGray
import com.gmg.seatnow.presentation.theme.SubLightGray
import com.gmg.seatnow.presentation.theme.White
import com.gmg.seatnow.presentation.user.mypage.UserInfoRow

@Composable
fun EditStoreInfoScreen(
    uiState: MyPageViewModel.MyPageUiState,
    onBackClick: () -> Unit,
    onEditContactClick: () -> Unit
) {
    // 일반 필드용 Helper (무조건 내용이 있어야 하는 경우)
    fun String.orLoading() = this.ifEmpty { "불러오기.." }

    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "가게 정보 수정",
                onBackClick = onBackClick
            )
        },
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            UserInfoRow(
                title = "대표자명",
                value = uiState.representativeName.orLoading()
            )
            Spacer(modifier = Modifier.height(24.dp))

            UserInfoRow(
                title = "사업자 등록번호",
                value = formatBusinessNumber(uiState.businessNumber).orLoading()
            )
            Spacer(modifier = Modifier.height(24.dp))

            UserInfoRow(
                title = "상호명",
                value = uiState.storeName.orLoading()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 주소
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "주소",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.storeAddress.orLoading(),
                    style = MaterialTheme.typography.bodySmall,
                    color = SubDarkGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))
                HorizontalDivider(thickness = 1.dp, color = SubLightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            UserInfoRow(
                title = "주변 대학명",
                value = uiState.universityName.orLoading()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 6. 사업자 등록증 파일 (없을 수도 있음)
            // ★ 수정: 로딩 전이면 "불러오기..", 로딩 완료 후면 값이 없어도 공백으로 표시
            UserInfoRow(
                title = "사업자 등록증 파일",
                value = if (!uiState.isStoreLoaded) "불러오기.." else uiState.licenseFileName
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 7. 가게 연락처 (없을 수도 있음)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditContactClick)
            ) {
                // ★ 수정: 로딩 전이면 "불러오기..", 로딩 완료 후면 포매팅된 값(없으면 공백) 표시
                UserInfoRow(
                    title = "가게 연락처",
                    value = if (!uiState.isStoreLoaded) "불러오기.." else formatPhoneNumber(uiState.storeContact),
                    showArrow = true
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditStoreInfoScreen() {
    val mockState = MyPageViewModel.MyPageUiState(
        isStoreLoaded = true, // 로딩 완료 상태 가정
        representativeName = "안태훈",
        businessNumber = "1234567890",
        storeName = "맛있는 술집 신촌본점",
        storeAddress = "서울특별시 서대문구 연세로 12길 34, 1층 (창천동)",
        universityName = "연세대학교",
        licenseFileName = "", // 빈 값 테스트
        storeContact = ""     // 빈 값 테스트
    )

    EditStoreInfoScreen(
        uiState = mockState,
        onBackClick = {},
        onEditContactClick = {}
    )
}