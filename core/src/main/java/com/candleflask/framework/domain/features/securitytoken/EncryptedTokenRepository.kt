package com.candleflask.framework.domain.features.securitytoken

interface EncryptedTokenRepository {
  fun updateToken(token: String?)

  fun retrieveToken(): String?

  fun isTokenExist(): Boolean = !retrieveToken().isNullOrBlank()
}