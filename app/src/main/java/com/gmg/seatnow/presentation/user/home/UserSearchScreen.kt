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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
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
    onUniversityClick: (String) -> Unit,
    viewModel: UserHomeViewModel,
    currentLat: Double,
    currentLng: Double,
    userLat: Double?,
    userLng: Double?
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val relatedUniversities by viewModel.relatedUniversities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    UserSearchContent(
        query = query,
        searchResults = searchResults,
        relatedUniversities = relatedUniversities,
        isLoading = isLoading,
        onBackClick = onBackClick,
        onQueryChange = { newQuery ->
            viewModel.onSearchQueryChanged(newQuery, currentLat, currentLng, userLat, userLng)
        },
        onSearchAction = {
            viewModel.onSearchQueryChanged(query, currentLat, currentLng, userLat, userLng)
        },
        onClearQuery = {
            viewModel.onSearchQueryChanged("", currentLat, currentLng, userLat, userLng)
        },
        onStoreClick = onStoreClick,
        onUniversityClick = onUniversityClick
    )
}

// [2] Stateless: UI 구현
@Composable
fun UserSearchContent(
    query: String,
    searchResults: List<Store>,
    relatedUniversities: List<String>,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSearchAction: () -> Unit,
    onClearQuery: () -> Unit,
    onStoreClick: (Store) -> Unit,
    onUniversityClick: (String) -> Unit
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .background(White, RoundedCornerShape(8.dp))
                    .border(1.dp, SubLightGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "뒤로가기",
                        tint = SubBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .focusRequester(focusRequester),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = SubBlack),
                    singleLine = true,
                    cursorBrush = SolidColor(SubGray),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            focusManager.clearFocus()
                            onSearchAction()
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart,
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

                if (query.isNotEmpty()) {
                    Icon(
                        painter = painterResource(id = R.drawable.btn_search_cancel),
                        contentDescription = "삭제",
                        tint = SubGray,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onClearQuery() }
                    )
                } else {
                    Spacer(modifier = Modifier.width(20.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))
            }
        }

        // [결과 리스트 영역]
        Box(modifier = Modifier.fillMaxSize()) {

            // 1. 검색 결과가 없을 때 (검색어 O, 로딩 X, 결과 0개)
            if (searchResults.isEmpty() && query.isNotEmpty() && query.isNotEmpty() && !isLoading) {
                Column(
                    // ★ [핵심] 정중앙(0,0)보다 y축으로 -0.3f만큼 위로 올림
                    modifier = Modifier.align(BiasAlignment(0f, -0.75f)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "검색 결과가 없습니다.",
                        style = MaterialTheme.typography.titleMedium,
                        color = SubDarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "장소, 지역, 대학명이 정확히 입력되었는지 확인해 주세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SubGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // 2. 검색 결과가 있을 때
            else {
                LazyColumn(contentPadding = PaddingValues(bottom = 20.dp)) {
                    // ★ [Step 1] 관련 대학 리스트 (최상단)
                    if (relatedUniversities.isNotEmpty()) {
                        items(relatedUniversities) { university ->
                            RelatedUniversityItem(
                                name = university,
                                onClick = {
                                    focusManager.clearFocus()
                                    onUniversityClick(university)
                                }
                            )
                            HorizontalDivider(color = SubPaleGray, thickness = 1.dp)
                        }

                        // ★ [Step 2] 영역 구분선 (2dp) - 대학 리스트가 있을 때만 표시
                        item {
                            HorizontalDivider(color = SubLightGray, thickness = 2.dp)
                        }
                    }

                    // ★ [Step 3] 실질적인 검색 결과 (Store) 리스트
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

            // 3. 로딩 중일 때
            if (isLoading && query.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PointRed)
                }
            }
        }
    }
}

@Composable
fun RelatedUniversityItem(
    name: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 돋보기 아이콘 등 적절한 아이콘 사용 (기존 리소스 활용 또는 기본 아이콘)
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = SubGray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
            color = SubBlack,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

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