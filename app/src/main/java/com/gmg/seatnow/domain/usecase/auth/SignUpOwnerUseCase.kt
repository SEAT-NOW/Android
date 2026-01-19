package com.gmg.seatnow.domain.usecase.auth

import android.net.Uri
import com.gmg.seatnow.data.model.request.OwnerSignUpRequestDTO
import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpOwnerUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        requestDto: OwnerSignUpRequestDTO,
        licenseUri: Uri?,
        storeImageUris: List<Uri>
    ): Result<Unit> {
        return repository.signUpOwner(requestDto, licenseUri, storeImageUris)
    }
}