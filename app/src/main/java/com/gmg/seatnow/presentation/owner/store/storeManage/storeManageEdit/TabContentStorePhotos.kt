package com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.AddPhotoButton
import com.gmg.seatnow.presentation.component.PhotoGridItem
import com.gmg.seatnow.presentation.theme.SubGray
import com.gmg.seatnow.presentation.theme.White

// ★ 사용자님이 주신 Step5PhotoScreen UI 구조 100% 동일 적용
@Composable
fun TabContentStorePhotos(
    viewModel: StoreEditMainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ★ [수정] 최대 5장 제한
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.onAction(StoreEditAction.AddStorePhotos(uris))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()) // 스크롤 가능하게 처리
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "음식, 실내, 외부 사진으로 가게를 소개해주세요 (최대 6장)",
            style = MaterialTheme.typography.bodyMedium,
            color = SubGray
        )
        Spacer(modifier = Modifier.height(24.dp))

        // ★ LazyGrid가 아닌 사용자님의 NonLazyPhotoGrid 로직 적용
        NonLazyPhotoGrid(
            photoList = uiState.storePhotoList,
            onAddClick = {
                // ★ [수정] 5장 미만일 때만 갤러리 오픈
                if (uiState.storePhotoList.size < 5) {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            },
            onRemove = { uriString ->
                viewModel.onAction(StoreEditAction.RemoveStorePhoto(uriString))
            },
            onSetRepresentative = { uriString ->
                viewModel.onAction(StoreEditAction.SetRepresentativePhoto(uriString))
            }
        )
        
        Spacer(modifier = Modifier.height(100.dp)) // 하단 여백
    }
}

// ★ 사용자님이 주신 코드 그대로 복사 (파라미터만 ViewModel State에 맞게 조정)
@Composable
fun NonLazyPhotoGrid(
    photoList: List<StoreImageUiModel>, // ★ 변경됨: List<String> -> List<StoreImageUiModel>
    onAddClick: () -> Unit,
    onRemove: (String) -> Unit,
    onSetRepresentative: (String) -> Unit
) {
    val columns = 3
    val totalCount = photoList.size + 1
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
                                    currentCount = photoList.size,
                                    maxCount = 5,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                // 사진 아이템
                                val photoIndex = index - 1
                                val item = photoList[photoIndex] // ★ StoreImageUiModel 객체 접근

                                // ★ 객체에서 데이터 추출
                                val uriString = item.uri
                                val isRep = item.isMain

                                PhotoGridItem(
                                    uri = Uri.parse(uriString),
                                    isRepresentative = isRep,
                                    onRemove = { onRemove(uriString) },
                                    onSetRepresentative = { onSetRepresentative(uriString) },
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
