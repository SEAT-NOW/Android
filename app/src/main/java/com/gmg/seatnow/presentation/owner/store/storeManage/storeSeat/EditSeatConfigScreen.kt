package com.gmg.seatnow.presentation.owner.store.mypage.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.gmg.seatnow.presentation.component.SeatNowRedPlusButton
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.component.SpaceItemCard
import com.gmg.seatnow.presentation.component.TableItemCard
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageAction
import com.gmg.seatnow.presentation.owner.store.mypage.MyPageViewModel
import com.gmg.seatnow.presentation.theme.*

@Composable
fun EditSeatConfigScreen(
    uiState: MyPageViewModel.MyPageUiState,
    onAction: (MyPageAction) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "좌석 정보 구성 수정",
                onBackClick = onBackClick
            )
        },
        containerColor = White,
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            // 하단 [변경] 버튼
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White) // 배경색 지정 (컨텐츠 비침 방지)
                    .navigationBarsPadding() // 시스템 네비게이션 바 높이만큼 패딩
                    .padding(24.dp)
                    .imePadding()
            ) {
                Button(
                    onClick = { onAction(MyPageAction.OnSaveSeatConfigClick) },
                    enabled = uiState.isSeatConfigValid && !uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PointRed,
                        disabledContainerColor = SubLightGray,
                        contentColor = White,
                        disabledContentColor = White
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "변경",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        // Step 3 UI 구조 그대로 적용 (Column + Scroll)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()) // 스크롤 가능하게 변경
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. 공간 구성 헤더 (도움말 팝업 포함)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Text(
                    text = "공간 구성",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )

                Spacer(modifier = Modifier.width(4.dp))

                var showTooltip by remember { mutableStateOf(false) }

                Box {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "도움말",
                        tint = SubGray,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showTooltip = !showTooltip }
                    )

                    if (showTooltip) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(x = 50, y = 70), // 위치 조정
                            onDismissRequest = { showTooltip = false }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(260.dp)
                                    .shadow(elevation = 4.dp, shape = RoundedCornerShape(8.dp)),
                                shape = RoundedCornerShape(8.dp),
                                color = SubPaleGray
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "공간별(예: 1층, 테라스)로 항목을 추가하면 좌석을 \n" +
                                                "편리하게 관리할 수 있습니다. 미입력 시 '전체' \n" +
                                                "하나의 공간으로 구성되며, 언제든 수정 가능합니다.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SubDarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. 공간 리스트
            uiState.spaceList.forEach { item ->
                key(item.id) {
                    if (item.isEditing) {
                        SignUpTextFieldWithButton(
                            value = item.editInput,
                            onValueChange = { onAction(MyPageAction.UpdateSpaceItemInput(item.id, it)) },
                            placeholder = "ex) 전체 / 1,2층 테라스..",
                            buttonText = "완료",
                            isButtonEnabled = true,
                            errorText = item.inputError,
                            onButtonClick = { onAction(MyPageAction.SaveSpaceItem(item.id)) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    } else {
                        val isSelected = uiState.selectedSpaceId == item.id
                        val isSpaceDeleteEnabled = uiState.spaceList.size > 1

                        SpaceItemCard(
                            name = item.name,
                            seatCount = item.seatCount, // Int
                            isSelected = isSelected,
                            onItemClick = { onAction(MyPageAction.SelectSpace(item.id)) },
                            onEditClick = { onAction(MyPageAction.EditSpace(item.id)) },
                            onDeleteClick = { if (isSpaceDeleteEnabled) onAction(MyPageAction.RemoveSpace(item.id)) },
                            isDeleteEnabled = isSpaceDeleteEnabled
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            // 공간 추가 버튼 (+)
            val isAnySpaceEditing = uiState.spaceList.any { it.isEditing }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SeatNowRedPlusButton(
                    onClick = { onAction(MyPageAction.AddSpaceItemRow) },
                    isEnabled = !isAnySpaceEditing
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
            HorizontalDivider(thickness = 1.dp, color = SubLightGray)
            Spacer(modifier = Modifier.height(40.dp))

            // --- 3. 테이블 구성 섹션 ---
            val selectedSpace = uiState.spaceList.find { it.id == uiState.selectedSpaceId }
            // ★ Step 3 로직: 공간이 수정 중일 때만 테이블도 수정 가능하게 (isEnabled = isTableEditable)
            val isTableEditable = selectedSpace?.isEditing == true

            val displayTableList = selectedSpace?.tableList ?: emptyList()
            // 총 좌석 수 계산
            val totalSeats = displayTableList.sumOf {
                (it.personCount.toIntOrNull() ?: 0) * (it.tableCount.toIntOrNull() ?: 0)
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "테이블 구성",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = SubBlack
                )
                Text(
                    text = "총 ${totalSeats}석",
                    style = MaterialTheme.typography.labelMedium,
                    color = SubGray
                )
            }

            Column {
                displayTableList.forEach { tableItem ->
                    key(tableItem.id) {
                        val isDeleteEnabled = displayTableList.size > 1
                        TableItemCard(
                            nValue = tableItem.personCount,
                            mValue = tableItem.tableCount,
                            onNChange = { onAction(MyPageAction.UpdateTableItemN(tableItem.id, it)) },
                            onMChange = { onAction(MyPageAction.UpdateTableItemM(tableItem.id, it)) },
                            onDeleteClick = { if (isDeleteEnabled) onAction(MyPageAction.RemoveTableItemRow(tableItem.id)) },
                            isDeleteEnabled = isDeleteEnabled,
                            isEnabled = isTableEditable // ★ Step 3 로직 일치
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SeatNowRedPlusButton(
                    onClick = { onAction(MyPageAction.AddTableItemRow) },
                    isEnabled = isTableEditable // ★ 버튼 비활성화 로직 일치
                )
            }

            // 하단 버튼이 가리지 않도록 여백 추가
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditSeatConfigScreen() {
    SeatNowTheme {
        EditSeatConfigScreen(
            uiState = MyPageViewModel.MyPageUiState(
                isSeatConfigValid = true
            ),
            onAction = {},
            onBackClick = {}
        )
    }
}