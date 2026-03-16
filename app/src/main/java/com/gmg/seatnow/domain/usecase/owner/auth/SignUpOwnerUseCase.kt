package com.gmg.seatnow.domain.usecase.owner.auth

import android.net.Uri
import com.gmg.seatnow.domain.model.OwnerSignUpInfo
import com.gmg.seatnow.domain.repository.AuthRepository
import javax.inject.Inject

class SignUpOwnerUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        info: OwnerSignUpInfo,
        licenseUri: Uri?,
        storeImageUris: List<Uri>,
        representativeUri: Uri?
    ): Result<Unit> {
        val sortedImages = if (representativeUri != null && storeImageUris.contains(representativeUri)) {
            listOf(representativeUri) + storeImageUris.filter { it != representativeUri }
        } else {
            storeImageUris
        }

        return repository.signUpOwner(info, licenseUri, sortedImages)
    }
}