package com.candleflask.framework.domain.features.securitytoken

import javax.inject.Inject

class RetrieveSecurityTokenUseCase @Inject constructor(private val encryptedTokenRepository: EncryptedTokenRepository) {

  suspend fun execute(): String? {
    return encryptedTokenRepository.retrieveToken()
  }
}