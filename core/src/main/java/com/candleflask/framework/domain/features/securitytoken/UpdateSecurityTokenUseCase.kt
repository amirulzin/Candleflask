package com.candleflask.framework.domain.features.securitytoken

import javax.inject.Inject

class UpdateSecurityTokenUseCase @Inject constructor(private val encryptedTokenRepository: EncryptedTokenRepository) {

  fun updateToken(token: String?) {
    encryptedTokenRepository.updateToken(token)
  }

  fun isTokenAvailable(): Boolean {
    return encryptedTokenRepository.isTokenExist()
  }

  fun retrieveToken(): String? {
    return encryptedTokenRepository.retrieveToken()
  }
}