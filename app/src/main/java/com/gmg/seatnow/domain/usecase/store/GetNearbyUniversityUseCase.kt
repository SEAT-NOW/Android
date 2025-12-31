package com.gmg.seatnow.domain.usecase.store

import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class GetNearbyUniversityUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double) = repository.getNearbyUniversity(lat, lng)
}