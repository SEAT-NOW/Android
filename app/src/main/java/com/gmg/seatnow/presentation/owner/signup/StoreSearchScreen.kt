package com.gmg.seatnow.presentation.owner.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.domain.model.StoreSearchResult
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.OwnerSignUpUiState
import com.gmg.seatnow.presentation.owner.signup.OwnerSignUpViewModel.SignUpAction
import com.gmg.seatnow.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreSearchScreen(
    uiState: OwnerSignUpUiState,
    onAction: (SignUpAction) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            // [TopBar] 닫기 버튼이 있는 형태
            CenterAlignedTopAppBar(
                title = { Text("상호명 검색", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(SignUpAction.CloseStoreSearch) }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "닫기")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = White)
            )
        },
        containerColor = White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // [검색창]
            SeatNowTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    onAction(SignUpAction.SearchStoreQuery(it)) // 실시간 검색 요청
                },
                placeholder = "상호명을 입력해주세요 (예: 용용선생)",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // [검색 결과 리스트]
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.storeSearchResults) { store ->
                    StoreResultItem(store = store, onClick = { onAction(SignUpAction.SelectStore(store)) })
                    HorizontalDivider(color = SubLightGray, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun StoreResultItem(
    store: StoreSearchResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 마커 아이콘 (이미지 참고)
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = SubGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = store.placeName,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                color = SubBlack
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = store.addressName,
                style = MaterialTheme.typography.bodySmall,
                color = SubGray
            )
        }
    }
}