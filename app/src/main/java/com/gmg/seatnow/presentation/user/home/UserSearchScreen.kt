package com.gmg.seatnow.presentation.user.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.Store
import com.gmg.seatnow.domain.model.StoreStatus
import com.gmg.seatnow.presentation.theme.*

// [1] Stateful: ViewModel 연결
@Composable
fun UserSearchScreen(
    onBackClick: () -> Unit,
    onStoreClick: (Store) -> Unit,
    viewModel: UserHomeViewModel,
    currentLat: Double,
    currentLng: Double,
    userLat: Double?,   // ★ [추가] 내 위치 (거리 계산용)
    userLng: Double?    // ★ [추가] 내 위치 (거리 계산용)
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    UserSearchContent(
        query = query,
        searchResults = searchResults,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onQueryChange = { newQuery ->
            // ★ 수정된 함수 호출 (내 위치 포함)
            viewModel.onSearchQueryChanged(newQuery, currentLat, currentLng, userLat, userLng)
        },
        onSearchAction = {
            viewModel.onSearchQueryChanged(query, currentLat, currentLng, userLat, userLng)
        },
        onClearQuery = {
            viewModel.onSearchQueryChanged("", currentLat, currentLng, userLat, userLng)
        },
        onStoreClick = onStoreClick
    )
}

// [2] Stateless: UI 구현 (요청사항 반영)
@Composable
fun UserSearchContent(
    query: String,
    searchResults: List<Store>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchAction: () -> Unit,
    onClearQuery: () -> Unit,
    onStoreClick: (Store) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .statusBarsPadding()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
    ) {
        // [검색바 영역]
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ★ 1. 통합 검색창 박스 (흰색 배경 + 회색 테두리)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp) // 높이 약간 키움
                    .background(White, RoundedCornerShape(8.dp))
                    .border(1.dp, SubLightGray, RoundedCornerShape(8.dp)) // ★ 테두리 회색
                    .padding(horizontal = 8.dp), // 내부 여백
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ★ 2. 뒤로가기 버튼 (검색창 내부로 이동)
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "뒤로가기",
                        tint = SubBlack, // 혹은 SubGray
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // ★ 3. 입력 필드
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight() // ★ 높이 꽉 채움 (정렬을 위해 필수)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = SubBlack,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(SubGray),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearchAction()
                        }
                    ),
                    // ★ decorationBox를 이용한 완벽한 수직 중앙 정렬
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart, // 내용물(텍스트/힌트)을 왼쪽 중앙에 배치
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "장소, 지역, 대학명 검색",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SubGray
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // ★ 4. 우측 아이콘 처리
                if (query.isNotEmpty()) {
                    // ★ 지정해주신 X 버튼 코드 적용
                    Icon(
                        painter = painterResource(id = R.drawable.btn_search_cancel),
                        contentDescription = "삭제",
                        tint = SubGray,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onClearQuery() }
                    )
                } else {
                    // ★ 돋보기 버튼 제거 (공백)
                    Spacer(modifier = Modifier.width(20.dp))
                }

                // 우측 끝 여백
                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // [검색 결과 리스트]
        Box(modifier = Modifier.fillMaxSize()) {
            if (searchResults.isEmpty() && query.isNotEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 결과가 없습니다.", style = MaterialTheme.typography.bodyMedium, color = SubGray)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
                    items(searchResults) { store ->
                        SearchStoreListItem(
                            store = store,
                            onItemClick = {
                                focusManager.clearFocus()
                                onStoreClick(store)
                            }
                        )
                        HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                    }
                }
            }

            if (isLoading && query.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PointRed)
                }
            }
        }
    }
}

// [리스트 아이템]
@Composable
fun SearchStoreListItem(
    store: Store,
    onItemClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = SubGray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = store.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = SubBlack,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = store.neighborhood.ifBlank { "주소 정보 없음" },
                style = MaterialTheme.typography.bodySmall,
                color = SubGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = store.distance,
            style = MaterialTheme.typography.bodySmall,
            color = SubGray
        )
    }
}

@Preview(showBackground = true, name = "검색 화면 (빈 값)")
@Composable
fun PreviewUserSearchContentEmpty() {
    SeatNowTheme {
        UserSearchContent(
            query = "",
            searchResults = emptyList(),
            isLoading = false,
            onBackClick = {},
            onQueryChange = {},
            onSearchAction = {},
            onClearQuery = {},
            onStoreClick = {}
        )
    }
}