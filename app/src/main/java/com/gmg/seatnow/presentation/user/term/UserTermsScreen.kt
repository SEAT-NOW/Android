package com.gmg.seatnow.presentation.user.term

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.TermItem
import com.gmg.seatnow.presentation.theme.*

@Composable
fun UserTermsScreen(
    onNavigateToBack: () -> Unit, // 뒤로가기 (로그인 화면으로)
    onNavigateToMain: () -> Unit  // 동의 완료 후 메인으로
) {
    // 약관 상태 관리
    var isAllChecked by remember { mutableStateOf(false) }
    var isAgeChecked by remember { mutableStateOf(false) }
    var isServiceChecked by remember { mutableStateOf(false) }
    var isPrivacyCollectChecked by remember { mutableStateOf(false) }
    var isPrivacyProvideChecked by remember { mutableStateOf(false) }
    var isLocationChecked by remember { mutableStateOf(false) } // ★ 위치기반 필수

    // 전체 동의 로직
    fun toggleAll(checked: Boolean) {
        isAllChecked = checked
        isAgeChecked = checked
        isServiceChecked = checked
        isPrivacyCollectChecked = checked
        isPrivacyProvideChecked = checked
        isLocationChecked = checked
    }

    // 개별 토글 시 전체 동의 상태 업데이트
    fun updateAllState() {
        isAllChecked = isAgeChecked && isServiceChecked && isPrivacyCollectChecked && isPrivacyProvideChecked && isLocationChecked
    }

    // 필수 항목이 다 체크되었는지 확인 (여기선 모두 필수로 가정)
    val isNextEnabled = isAgeChecked && isServiceChecked && isPrivacyCollectChecked && isPrivacyProvideChecked && isLocationChecked

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White) // 배경색
            .systemBarsPadding()
    ) {
        // 1. 상단 바 (타이틀 + 닫기 버튼)
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

        // 2. 전체 동의 체크박스
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable { toggleAll(!isAllChecked) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAllChecked,
                onCheckedChange = { toggleAll(it) },
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

        // 3. 약관 리스트 (padding 추가)
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            TermItem(
                title = "[필수] 만 14세 이상",
                isChecked = isAgeChecked,
                showArrow = false,
                onToggle = { isAgeChecked = !isAgeChecked; updateAllState() }
            )
            TermItem(
                title = "[필수] 이용약관 동의",
                isChecked = isServiceChecked,
                showArrow = true,
                onToggle = { isServiceChecked = !isServiceChecked; updateAllState() }
            )
            TermItem(
                title = "[필수] 개인정보 수집이용 동의",
                isChecked = isPrivacyCollectChecked,
                showArrow = true,
                onToggle = { isPrivacyCollectChecked = !isPrivacyCollectChecked; updateAllState() }
            )
            TermItem(
                title = "[필수] 개인정보 처리방침 동의",
                isChecked = isPrivacyProvideChecked,
                showArrow = true,
                onToggle = { isPrivacyProvideChecked = !isPrivacyProvideChecked; updateAllState() }
            )
            // ★ 위치 기반 필수 항목 추가
            TermItem(
                title = "[필수] 위치기반 서비스 이용약관 동의",
                isChecked = isLocationChecked,
                showArrow = true,
                onToggle = { isLocationChecked = !isLocationChecked; updateAllState() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. 하단 버튼
        Button(
            onClick = onNavigateToMain,
            enabled = isNextEnabled,
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