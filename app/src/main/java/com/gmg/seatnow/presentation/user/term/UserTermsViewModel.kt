package com.gmg.seatnow.presentation.user.term

import androidx.lifecycle.ViewModel
import com.gmg.seatnow.data.local.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserTermsViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    // 어떤 모드로 들어왔는지에 따라 다르게 저장
    fun saveTermsAgreement(isGuest: Boolean) {
        if (isGuest) {
            authManager.setGuestTermsAgreed(true)
        } else {
            authManager.setKakaoTermsAgreed(true)
        }
    }
}