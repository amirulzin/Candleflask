package com.candleflask.framework.features.securitytoken

interface EncryptedTokenRepository {
  fun updateToken(token: String?)

  fun retrieveToken(): String?

  fun isTokenExist(): Boolean = !retrieveToken().isNullOrBlank()
}