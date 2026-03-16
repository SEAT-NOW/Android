package com.gmg.seatnow.presentation.user.home

import com.gmg.seatnow.domain.model.Store

data class UserHomeUiState(
    val storeList: List<Store> = emptyList(),
    val isLoading: Boolean = false,
    val activeHeadCount: Int? = null,
    val searchQuery: String = "",
    val searchResults: List<Store> = emptyList(),
    val relatedUniversities: List<String> = emptyList()
)

sealed interface UserHomeAction {
    data class SetHeadCountFilter(val count: Int) : UserHomeAction
    object ClearHeadCountFilter : UserHomeAction
    data class FetchStoresInCurrentMap(
        val lat: Double,
        val lng: Double,
        val radius: Double,
        val userLat: Double? = null,
        val userLng: Double? = null
    ) : UserHomeAction

    data class OnSearchQueryChanged(
        val query: String,
        val currentLat: Double,
        val currentLng: Double,
        val userLat: Double? = null,
        val userLng: Double? = null
    ) : UserHomeAction

    data class FetchStoresByUniversity(
        val uniName: String,
        val lat: Double,
        val lng: Double,
        val radius: Double,
        val userLat: Double?,
        val userLng: Double?,
        val onResultLoaded: (Store?) -> Unit
    ) : UserHomeAction

    object ClearSearch : UserHomeAction
}

sealed interface UserHomeEvent {
}
