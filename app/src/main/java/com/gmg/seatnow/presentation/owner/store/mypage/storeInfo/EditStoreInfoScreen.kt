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

            // 1. 대표자명
            UserInfoRow(
                title = "대표자명",
                value = uiState.representativeName.ifEmpty { "불러오기.." }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 2. 사업자 등록번호 (포매팅 적용)
            UserInfoRow(
                title = "사업자 등록번호",
                value = formatBusinessNumber(uiState.businessNumber).ifEmpty { "불러오기.." }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 3. 상호명
            UserInfoRow(
                title = "상호명",
                value = uiState.storeName.ifEmpty { "불러오기.." }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 4. 주소 (Custom Layout)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "주소",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = uiState.storeAddress.ifEmpty { "불러오기.." },
                    style = MaterialTheme.typography.bodySmall,
                    color = SubDarkGray,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(2.dp))
                HorizontalDivider(thickness = 1.dp, color = SubLightGray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. 주변 대학명
            UserInfoRow(
                title = "주변 대학명",
                value = uiState.universityName.ifEmpty { "불러오기.." }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 6. 사업자 등록증 파일
            UserInfoRow(
                title = "사업자 등록증 파일",
                value = uiState.licenseFileName.ifEmpty { "불러오기.." }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // 7. 가게 연락처 (수정 가능 - 클릭 영역)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onEditContactClick)
            ) {
                UserInfoRow(
                    title = "가게 연락처",
                    value = formatPhoneNumber(uiState.storeContact).ifEmpty { "불러오기.." },
                    showArrow = true
                )
            }
        }
    }
}

// ================= PREVIEW =================

@Preview(showBackground = true)
@Composable
fun PreviewEditStoreInfoScreen() {
    val mockState = MyPageViewModel.MyPageUiState(
        representativeName = "안태훈",
        businessNumber = "1234567890",
        storeName = "맛있는 술집 신촌본점",
        storeAddress = "서울특별시 서대문구 연세로 12길 34, 1층 (창천동)",
        universityName = "연세대학교",
        licenseFileName = "business_license.jpg",
        storeContact = "0212345678"
    )

    EditStoreInfoScreen(
        uiState = mockState,
        onBackClick = {},
        onEditContactClick = {}
    )
}