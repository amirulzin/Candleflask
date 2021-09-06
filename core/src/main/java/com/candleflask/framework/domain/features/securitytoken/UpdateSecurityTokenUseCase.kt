package com.candleflask.framework.domain.features.securitytoken

import javax.inject.Inject

class UpdateSecurityTokenUseCase @Inject constructor(private val encryptedTokenRepository: EncryptedTokenRepository) {

  suspend fun execute(token: String?) {
    encryptedTokenRepository.updateToken(token)
  }
}