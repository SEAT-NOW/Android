package com.gmg.seatnow.presentation.owner.signup.steps

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.platform.LocalContext
import com.gmg.seatnow.presentation.component.BusinessNumberVisualTransformation
import com.gmg.seatnow.presentation.component.NumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2BusinessInfoScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    // ★ 파일 선택기 (이미지 전용)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = getFileNameFromUri(context, uri)
            onAction(SignUpAction.UploadLicenseImage(uri, fileName))
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
            var textFieldWidth by remember { mutableStateOf(0) }
            val density = LocalDensity.current // 픽셀을 dp로 변환하기 위해 필요

            Row(
                modifier = Modifier.fillMaxWidth()
                    .onSizeChanged { size ->
                        textFieldWidth = size.width
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {


                SeatNowTextField(
                    value = uiState.storeName,
                    onValueChange = { onAction(SignUpAction.UpdateStoreName(it)) },
                    placeholder = "상호명"
                )
            }
            val dropDownWidthRatio = 0.96f

            // 드롭다운 메뉴 (검색 결과)
            DropdownMenu(
                expanded = uiState.isStoreSearchDropdownExpanded && uiState.storeSearchResults.isNotEmpty(),
                onDismissRequest = { /* 닫힘 처리 */ },
                modifier = Modifier
                    .width(with(density) {(textFieldWidth * dropDownWidthRatio).toDp()})
                    .background(White),
                offset = DpOffset(
                    x = with(density) { (textFieldWidth * (1 - dropDownWidthRatio) / 2).toDp() },
                    y = 0.dp
                ),
                properties = PopupProperties(focusable = false)
            ) {
                uiState.storeSearchResults.forEachIndexed { index, result ->
                    DropdownMenuItem(
                        text = { Text(text = result, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            onAction(SignUpAction.SelectStoreName(result))
                            focusManager.clearFocus()
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )

                    if (index < uiState.storeSearchResults.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp), // 양옆 여백 (이미지처럼 살짝 띄우려면)
                            thickness = 1.dp,        // 선 두께
                            color = SubLightGray     // 색상 (기존 테마 색상 활용)
                        )
                    }
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
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 텍스트 필드 (모양 담당, 입력 불가, 읽기 전용)
            SeatNowTextField(
                value = uiState.licenseFileName ?: "", // 파일명 표시, 없으면 빈값
                onValueChange = {},
                placeholder = "사업자등록증 파일 선택",
                isEnabled = true, // 활성화된 색상(흰색 배경)을 유지하기 위해 true
                readOnly = true   // 키보드 안 올라오게 설정
            )

            // 2. 우측 아이콘 (링크 모양)
            Icon(
                painter = painterResource(id = R.drawable.ic_link),
                contentDescription = "파일 첨부",
                tint = SubLightGray, // 색상 맞춤
                modifier = Modifier
                    .align(Alignment.CenterEnd) // 오른쪽 중앙 정렬
                    .padding(end = 16.dp)       // 우측 여백
                    .size(20.dp)                // 아이콘 크기 조절
            )

            // 3. 전체 클릭 영역 (투명 버튼 역할)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp)) // 텍스트 필드 모양대로 클리핑
                    .clickable {
                        // 갤러리 열기 (이미지만)
                        imagePickerLauncher.launch("image/*")
                    }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))


    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor.use {
            if (it != null && it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    result = it.getString(index)
                }
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unknown_file"
}

@Preview(showBackground = true, name = "Step 2 Only", heightDp = 800)
@Composable
fun PreviewStep2BusinessInfoScreen() {
    SeatNowTheme {
        Step2BusinessInfoScreen(
            uiState = OwnerSignUpUiState(
                storeName = "메가커피", // 입력된 텍스트 예시
                storeSearchResults = listOf("메가커피 건대점", "메가커피 세종대점", "메가커피 어린이대공원점"),
                isStoreSearchDropdownExpanded = true,
            ),
            onAction = {}
        )
    }
}