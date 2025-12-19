package com.gmg.seatnow.presentation.owner.signup.steps

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.BusinessNumberVisualTransformation
import com.gmg.seatnow.presentation.component.NumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.R

@Composable
fun Step2BusinessInfoScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    val focusManager = LocalFocusManager.current

    // ★ 파일 선택기 (이미지 전용)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onAction(SignUpAction.UploadLicenseImage(uri))
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        // 1. 대표자명 (일반 텍스트)
        SeatNowTextField(
            value = uiState.repName,
            onValueChange = { onAction(SignUpAction.UpdateRepName(it)) },
            placeholder = "대표자명",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 사업자 등록번호 (숫자패드, 10자 제한, 확인 버튼)
        // 확인 완료 시 TextField 및 버튼 비활성화 (isEnabled 로직 활용)
        SignUpTextFieldWithButton(
            value = uiState.businessNumber,
            onValueChange = { onAction(SignUpAction.UpdateBusinessNum(it)) },
            placeholder = "사업자등록번호",
            buttonText = if (uiState.isBusinessNumVerified) "인증완료" else "확인",
            errorText = uiState.businessNumberError,
            isEnabled = !uiState.isBusinessNumVerified, // 인증되면 입력 불가
            isButtonEnabled = !uiState.isBusinessNumVerified && uiState.businessNumber.length == 10,
            keyboardType = KeyboardType.NumberPassword, // 숫자만
            visualTransformation = BusinessNumberVisualTransformation(), // 000-00-00000 포맷
            onButtonClick = {
                focusManager.clearFocus()
                onAction(SignUpAction.VerifyBusinessNum)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 상호명 (검색 Dropdown 포함)
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 검색 버튼이 포함된 형태를 원하신다면 SignUpTextFieldWithButton을 개조하거나 Row로 구성
                // 요구사항 4: "기본적으로 text입력이 가능하게 열어두고... 검색 버튼" (이미지 참조 시 TextField 내부에 버튼이 있는 형태)
                // 여기서는 SeatNowTextField 옆에 버튼을 두거나, SignUpTextFieldWithButton을 활용합니다.
                // 편의상 SignUpTextFieldWithButton을 재활용하되 버튼 텍스트를 "검색"으로 둡니다.

                SignUpTextFieldWithButton(
                    value = uiState.storeName,
                    onValueChange = { onAction(SignUpAction.UpdateStoreName(it)) },
                    placeholder = "상호명",
                    buttonText = "검색",
                    onButtonClick = {
                        // 디바운싱에 의해 자동 검색되지만, 버튼 클릭 시 즉시 검색 등의 처리 가능
                        // 현재는 입력 시 자동 검색 흐름
                    }
                )
            }

            // 드롭다운 메뉴 (검색 결과)
            DropdownMenu(
                expanded = uiState.isStoreSearchDropdownExpanded && uiState.storeSearchResults.isNotEmpty(),
                onDismissRequest = { /* 닫힘 처리 */ },
                modifier = Modifier
                    .fillMaxWidth(0.8f) // 부모 너비에 맞춤 (Padding 고려)
                    .background(White),
                properties = PopupProperties(focusable = false)
            ) {
                uiState.storeSearchResults.forEach { result ->
                    DropdownMenuItem(
                        text = { Text(text = result, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onAction(SignUpAction.SelectStoreName(result))
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. 주소 (입력 불가, 클릭 시 API 호출)
        Box(modifier = Modifier.fillMaxWidth()) {
            SeatNowTextField(
                value = uiState.mainAddress,
                onValueChange = {}, // ReadOnly
                placeholder = "주소",
                isEnabled = true,
                readOnly = true
            )
            // 클릭 이벤트를 뺏기지 않도록 투명 박스로 덮음 (TextField 자체 enabled=false 하면 스타일이 변하므로)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        focusManager.clearFocus()
                        onAction(SignUpAction.OnAddressClick)
                    }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. 우편번호 (주소 아래에 추가, 입력 불가)
        SeatNowTextField(
            value = uiState.zipCode,
            onValueChange = {},
            placeholder = "우편번호",
            isEnabled = uiState.zipCode.isEmpty(),
            readOnly = true,
            )

        Spacer(modifier = Modifier.height(20.dp))

        // 6. 주변 대학명 (입력 불가, 자동 채움)
        SeatNowTextField(
            value = uiState.nearbyUniv,
            onValueChange = {},
            placeholder = "주변 대학명",
            isEnabled = uiState.nearbyUniv.isEmpty(),
            readOnly = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 7. 가게 연락처 (선택 사항, 복잡한 하이픈 로직)
        SeatNowTextField(
            value = uiState.storeContact,
            onValueChange = { onAction(SignUpAction.UpdateStoreContact(it)) },
            placeholder = "가게 연락처('-' 제외)",
            keyboardType = KeyboardType.Number,
            visualTransformation = NumberVisualTransformation() // 커스텀 로직 적용
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 8. 사업자등록증 파일 선택 (버튼만 활성화)
        // 커스텀 UI: 텍스트필드처럼 보이지만 우측에 아이콘이 있고 전체가 클릭되는 형태
        FileSelectionField(
            fileName = uiState.licenseFileName,
            onClick = { /* 추후 파일 피커 연동 */ }
        )

        Spacer(modifier = Modifier.height(10.dp))


    }
}

// 파일 선택용 커스텀 컴포저블 (TextField 모양 흉내)
@Composable
fun FileSelectionField(
    fileName: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .border(1.dp, SubLightGray, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = fileName ?: "사업자등록증 파일 선택",
            color = if (fileName == null) SubLightGray else SubBlack,
            style = MaterialTheme.typography.bodyMedium
        )
        Icon(
            // Attachment 아이콘 대신 rotate된 링크 아이콘 등을 사용 (임시로 Check 사용하거나 리소스 필요)
            // 여기서는 기본 아이콘 사용
            painter = painterResource(id = R.drawable.ic_link),
            contentDescription = null,
            tint = SubLightGray,
            modifier = Modifier.rotate(-45f)
        )
    }
}

@Preview(showBackground = true, name = "Step 2 Only", heightDp = 800)
@Composable
fun PreviewStep2BusinessInfoScreen() {
    SeatNowTheme {
        Step2BusinessInfoScreen(
            uiState = OwnerSignUpUiState(),
            onAction = {}
        )
    }
}