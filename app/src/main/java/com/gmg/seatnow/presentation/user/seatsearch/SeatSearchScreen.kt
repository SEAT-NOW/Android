package com.gmg.seatnow.presentation.user.seatsearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.theme.White
import com.gmg.seatnow.presentation.user.seatsearch.components.SearchInputContent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SeatSearchScreen(
    onSearchConfirmed: (Int) -> Unit,
    viewModel: SeatSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(true) {
        viewModel.event.collectLatest { event ->
            when (event) {
                is SeatSearchEvent.OnSearchConfirmed -> {
                    onSearchConfirmed(event.count)
                }
            }
        }
    }

    // 배경 클릭 시 포커스 해제
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusManager.clearFocus() }
    ) {
        SearchInputContent(
            headCount = uiState.headCount,
            onCountChange = { viewModel.onAction(SeatSearchAction.OnHeadCountChanged(it)) },
            onAdjust = { viewModel.onAction(SeatSearchAction.OnAdjustHeadCount(it)) },
            onFocusClear = { viewModel.onAction(SeatSearchAction.OnHeadCountFinalize) },
            onSearchClick = { viewModel.onAction(SeatSearchAction.OnSearchClick) }
        )
    }
}