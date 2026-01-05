package com.gmg.seatnow.presentation.owner.signup.steps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.component.SpaceItemCard
import com.gmg.seatnow.presentation.component.TableItemCard
import com.gmg.seatnow.domain.model.SpaceItem

// ★ [복구 1] Annotation 추가
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Step3StoreScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    // 스크롤 충돌 방지를 위해 Column 사용
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 100.dp)
    ) {
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
                        offset = IntOffset(x = 650, y = 70), // 위치는 UI에 맞게 미세조정 (기존 코드 참고)
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

        // 공간 리스트 (forEach 사용)
        uiState.spaceList.forEach { item ->
            key(item.id) {
                if (item.isEditing) {
                    SignUpTextFieldWithButton(
                        value = item.editInput,
                        onValueChange = { onAction(SignUpAction.UpdateSpaceItemInput(item.id, it)) },
                        placeholder = "ex) 전체 / 1,2층 테라스..",
                        buttonText = "완료",
                        isButtonEnabled = true,
                        errorText = item.inputError,
                        onButtonClick = { onAction(SignUpAction.SaveSpaceItem(item.id)) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                } else {
                    val isSelected = uiState.selectedSpaceId == item.id
                    val isSpaceDeleteEnabled = uiState.spaceList.size > 1

                    SpaceItemCard(
                        name = item.name,
                        seatCount = item.seatCount,
                        isSelected = isSelected,
                        onItemClick = { onAction(SignUpAction.SelectSpace(item.id)) },
                        onEditClick = { onAction(SignUpAction.EditSpace(item.id)) },
                        onDeleteClick = { if (isSpaceDeleteEnabled) onAction(SignUpAction.RemoveSpace(item.id)) },
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
                onClick = { onAction(SignUpAction.AddSpaceItemRow) },
                isEnabled = !isAnySpaceEditing
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
        HorizontalDivider(thickness = 1.dp, color = SubLightGray)
        Spacer(modifier = Modifier.height(40.dp))

        // --- 2. 테이블 구성 섹션 ---
        val selectedSpace = uiState.spaceList.find { it.id == uiState.selectedSpaceId }
        val isTableEditable = selectedSpace?.isEditing == true

        val displayTableList = selectedSpace?.tableList ?: emptyList()
        val totalSeats = displayTableList.sumOf { (it.personCount.toIntOrNull() ?: 0) * (it.tableCount.toIntOrNull() ?: 0) }

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(text = "테이블 구성", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = SubBlack)
            Text(text = "총 ${totalSeats}석", style = MaterialTheme.typography.labelMedium, color = SubGray)
        }

        Column {
            displayTableList.forEach { tableItem ->
                key(tableItem.id) {
                    val isDeleteEnabled = displayTableList.size > 1
                    TableItemCard(
                        nValue = tableItem.personCount,
                        mValue = tableItem.tableCount,
                        onNChange = { onAction(SignUpAction.UpdateTableItemN(tableItem.id, it)) },
                        onMChange = { onAction(SignUpAction.UpdateTableItemM(tableItem.id, it)) },
                        onDeleteClick = { if (isDeleteEnabled) onAction(SignUpAction.RemoveTableItemRow(tableItem.id)) },
                        isDeleteEnabled = isDeleteEnabled,
                        isEnabled = isTableEditable
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            SeatNowRedPlusButton(
                onClick = { onAction(SignUpAction.AddTableItemRow) },
                isEnabled = isTableEditable // ★ 버튼도 비활성화 처리
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun PreviewStep3StoreScreen() {
    SeatNowTheme {
        Step3StoreScreen(
            uiState = OwnerSignUpUiState(
                spaceList = listOf(
                    SpaceItem(id = 1, name = "1층 홀", seatCount = 20),
                    SpaceItem(id = 2, name = "2층 테라스", seatCount = 12),
                    SpaceItem(id = 3, name = "별관 (수정 모드 테스트)", seatCount = 8, isEditing = true, editInput = "수정 중인 이름")
                )
            ),
            onAction = {}
        )
    }
}