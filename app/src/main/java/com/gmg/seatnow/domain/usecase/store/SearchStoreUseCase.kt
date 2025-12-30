package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.AuthRepository
import com.gmg.seatnow.domain.model.StoreSearchResult
import javax.inject.Inject

class SearchStoreUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(query: String): Result<List<StoreSearchResult>> {
        return repository.searchStore(query)
    }
}