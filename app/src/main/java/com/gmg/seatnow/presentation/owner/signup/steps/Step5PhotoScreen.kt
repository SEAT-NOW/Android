package com.gmg.seatnow.presentation.owner.signup.steps

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.AddPhotoButton
import com.gmg.seatnow.presentation.component.PhotoGridItem
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.theme.*

@Composable
fun Step5PhotoScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    // ★ [수정] 최대 5장 제한
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAction(SignUpAction.AddStorePhotos(uris))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "가게 사진 등록",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "음식, 실내, 외부 사진으로 가게를 소개해주세요 (선택)",
            style = MaterialTheme.typography.bodySmall,
            color = SubGray
        )
        Spacer(modifier = Modifier.height(24.dp))

        NonLazyPhotoGrid(
            uiState = uiState,
            onAddClick = {
                // ★ [수정] 5장 미만일 때만 갤러리 오픈
                if (uiState.storePhotoList.size < 5) {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            },
            onAction = onAction
        )
    }
}

@Composable
fun NonLazyPhotoGrid(
    uiState: OwnerSignUpUiState,
    onAddClick: () -> Unit,
    onAction: (SignUpAction) -> Unit
) {
    val columns = 3
    val totalCount = uiState.storePhotoList.size + 1
    val rows = (totalCount + columns - 1) / columns

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (c in 0 until columns) {
                    val index = r * columns + c

                    if (index < totalCount) {
                        // 3등분
                        Box(modifier = Modifier.weight(1f)) {
                            if (index == 0) {
                                // 추가 버튼
                                AddPhotoButton(
                                    onClick = onAddClick,
                                    currentCount = uiState.storePhotoList.size,
                                    maxCount = 5, // ★ [수정] 5로 표시
                                    modifier = Modifier.fillMaxWidth() // 비율은 내부에서 처리
                                )
                            } else {
                                // 사진 아이템
                                val photoIndex = index - 1
                                val uri = uiState.storePhotoList[photoIndex]
                                val isRep = (uri == uiState.representativePhotoUri)

                                PhotoGridItem(
                                    uri = uri,
                                    isRepresentative = isRep,
                                    onRemove = { onAction(SignUpAction.RemoveStorePhoto(uri)) },
                                    onSetRepresentative = { onAction(SignUpAction.SetRepresentativePhoto(uri)) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        // 빈 공간 채우기
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun PreviewStep5() {
    SeatNowTheme {
        Step5PhotoScreen(
            uiState = OwnerSignUpUiState(),
            onAction = {}
        )
    }
}