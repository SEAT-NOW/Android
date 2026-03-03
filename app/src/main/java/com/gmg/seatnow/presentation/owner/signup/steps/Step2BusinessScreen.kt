package com.gmg.seatnow.presentation.owner.signup.steps

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.gmg.seatnow.presentation.component.BusinessNumberVisualTransformation
import com.gmg.seatnow.presentation.component.NumberVisualTransformation
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.SignUpAction
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step2BusinessScreen(
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
            value = uiState.business.repName,
            onValueChange = { input ->
                if (input.all { char -> char.isLetter() || char.isWhitespace() }) {
                    onAction(SignUpAction.UpdateRepName(input))
                }
            },
            placeholder = "대표자명(특수문자/숫자 입력 불가)",
            keyboardType = KeyboardType.Text
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 2. 사업자 등록번호 (숫자패드, 10자 제한, 확인 버튼)
        SignUpTextFieldWithButton(
            value = uiState.business.businessNumber,
            onValueChange = { onAction(SignUpAction.UpdateBusinessNum(it)) },
            placeholder = "사업자등록번호('-' 제외)",
            buttonText = if (uiState.business.isBusinessNumVerified) "인증완료" else "확인",
            errorText = uiState.business.businessNumberError,
            isEnabled = !uiState.business.isBusinessNumVerified,
            isButtonEnabled = !uiState.business.isBusinessNumVerified && uiState.business.businessNumber.length == 10,
            keyboardType = KeyboardType.NumberPassword,
            visualTransformation = BusinessNumberVisualTransformation(), // 000-00-00000 포맷
            onButtonClick = {
                focusManager.clearFocus()
                onAction(SignUpAction.VerifyBusinessNum)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 3. 상호명 (검색 Dropdown 포함)
        Box(modifier = Modifier.fillMaxWidth()) {
            SignUpTextFieldWithButton(
                value = uiState.business.storeName,
                onValueChange = {},
                placeholder = "상호명",
                buttonText = "검색",
                isEnabled = false,
                isButtonEnabled = true,
                onButtonClick = {
                    onAction(SignUpAction.OpenStoreSearch)
                }
            )

            // [핵심] 투명한 클릭 영역 오버레이
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onAction(SignUpAction.OpenStoreSearch)
                    }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. 주소 (입력 불가, 클릭 시 API 호출)
        SeatNowTextField(
            value = uiState.business.mainAddress,
            onValueChange = { onAction(SignUpAction.UpdateMainAddress(it)) },
            placeholder = "주소 (상호명 검색 시 자동 입력)",
            isEnabled = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 5. 주변 대학명 (입력 불가, 자동 채움)
        SeatNowTextField(
            value = uiState.business.nearbyUniv,
            onValueChange = {},
            placeholder = "주변 대학명 (자동 입력)",
            isEnabled = uiState.business.isNearbyUnivEnabled,
            readOnly = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 6. 가게 연락처 (선택 사항, 복잡한 하이픈 로직)
        SeatNowTextField(
            value = uiState.business.storeContact,
            onValueChange = { onAction(SignUpAction.UpdateStoreContact(it)) },
            placeholder = "가게 연락처('-' 제외)",
            keyboardType = KeyboardType.Number,
            visualTransformation = NumberVisualTransformation()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 7. 사업자등록증 파일 선택
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            SeatNowTextField(
                value = uiState.business.licenseFileName ?: "", // 파일명 표시
                onValueChange = {},
                placeholder = "사업자등록증 파일 선택",
                isEnabled = true,
                readOnly = true
            )

            Icon(
                painter = painterResource(id = R.drawable.ic_link),
                contentDescription = "파일 첨부",
                tint = SubLightGray,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(20.dp)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
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
        Step2BusinessScreen(
            uiState = OwnerSignUpUiState(),
            onAction = {}
        )
    }
}