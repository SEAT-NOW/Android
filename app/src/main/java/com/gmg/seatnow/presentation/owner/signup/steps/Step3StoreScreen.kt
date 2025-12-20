package com.gmg.seatnow.presentation.owner.signup.steps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gmg.seatnow.presentation.component.CircularNumberField
import com.gmg.seatnow.presentation.component.SeatNowRedPlusButton
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.theme.*
import com.gmg.seatnow.R
import com.gmg.seatnow.presentation.component.SignUpTextFieldWithButton
import com.gmg.seatnow.presentation.component.SpaceItemCard
import com.gmg.seatnow.presentation.owner.SpaceItem

@Composable
fun Step3StoreScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        // --- 1. 공간 구성 섹션 ---
        Text(
            text = "공간 구성",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SubBlack
        )
        Spacer(modifier = Modifier.height(12.dp))

        // 공간 입력 필드
        SeatNowTextField(
            value = uiState.spaceInput,
            onValueChange = { onAction(SignUpAction.UpdateSpaceInput(it)) },
            placeholder = "ex) 전체 / 1,2층 테라스..",
            errorText = uiState.spaceInputError
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 공간 추가 버튼
        SeatNowRedPlusButton(
            onClick = { onAction(SignUpAction.AddSpace) },
            modifier = Modifier.align(Alignment.CenterHorizontally) // 가운데 정렬
        )
        Spacer(modifier = Modifier.height(20.dp))

        uiState.spaceList.forEach { item ->
            if (item.isEditing) {
                // [수정 모드]: SignUpTextFieldWithButton 사용 (완료 버튼 포함)
                SignUpTextFieldWithButton(
                    value = item.editInput,
                    onValueChange = { onAction(SignUpAction.UpdateEditInput(item.id, it)) },
                    placeholder = "공간 이름 수정",
                    buttonText = "완료",
                    isButtonEnabled = item.editInput.isNotBlank(),
                    onButtonClick = { onAction(SignUpAction.SaveSpace(item.id)) }
                )
            } else {
                // [보기 모드]: SpaceItemCard 사용 (수정/삭제 버튼 포함)
                SpaceItemCard(
                    name = item.name,
                    seatCount = item.seatCount, // 현재는 0석
                    onEditClick = { onAction(SignUpAction.EditSpace(item.id)) },
                    onDeleteClick = { onAction(SignUpAction.RemoveSpace(item.id)) }
                )
            }
            Spacer(modifier = Modifier.height(12.dp)) // 아이템 간 간격
        }

        Spacer(modifier = Modifier.height(40.dp))
        HorizontalDivider(thickness = 1.dp, color = SubLightGray)
        Spacer(modifier = Modifier.height(40.dp))


        // --- 2. 테이블 구성 섹션 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "테이블 구성",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
            // 총 좌석 수 계산 표시
            Text(
                text = "총 = SUM(N*M)석",
                style = MaterialTheme.typography.labelSmall,
                color = SubGray
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 테이블 입력 Row
        TableRowItem(
            nValue = uiState.tablePersonCount,
            mValue = uiState.tableCount,
            onNChange = { onAction(SignUpAction.UpdateTablePersonCount(it)) },
            onMChange = { onAction(SignUpAction.UpdateTableCount(it)) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 테이블 추가 버튼
        SeatNowRedPlusButton(
            onClick = { /* TODO: 테이블 추가 로직 */ },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

// 테이블 입력 행 (화면 전용 컴포넌트로 유지하거나 필요시 공통으로 이동)
@Composable
private fun TableRowItem(
    nValue: String,
    mValue: String,
    onNChange: (String) -> Unit,
    onMChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // N (인원 수)
        CircularNumberField(
            value = nValue,
            onValueChange = onNChange,
            placeholder = "N"
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "인 테이블", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
        Spacer(modifier = Modifier.width(16.dp))

        // X 아이콘 (곱하기)
        Icon(
            painter = painterResource(id = R.drawable.ic_table_multiply), // X 아이콘
            contentDescription = "multiply",
            tint = PointRed,
            modifier = Modifier.size(14.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // M (개수)
        CircularNumberField(
            value = mValue,
            onValueChange = onMChange,
            placeholder = "M"
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "개", style = MaterialTheme.typography.bodyMedium, color = SubBlack)
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
fun PreviewStep3StoreScreen() {
    SeatNowTheme {
        Step3StoreScreen(
            uiState = OwnerSignUpUiState(
                spaceInput = "입력 중인 텍스트...", // 입력창 테스트용
                spaceList = listOf(
                    SpaceItem(id = 1, name = "1층 홀", seatCount = 20),
                    SpaceItem(id = 2, name = "2층 테라스", seatCount = 12),
                    SpaceItem(id = 3, name = "별관 (수정 모드 테스트)", seatCount = 8, isEditing = true, editInput = "수정 중인 이름"))),
            onAction = {}
        )
    }
}