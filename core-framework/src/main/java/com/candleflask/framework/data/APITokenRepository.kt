package com.candleflask.framework.data

import com.candleflask.framework.data.datasource.EncryptedDataSource
import com.candleflask.framework.domain.features.securitytoken.EncryptedTokenRepository
import javax.inject.Inject

class APITokenRepository @Inject constructor(
  private val encryptedDataSource: EncryptedDataSource
) : EncryptedTokenRepository {
  private companion object {
    private const val KEY_TOKEN = "TOKEN"
  }

  override fun updateToken(token: String?) {
    encryptedDataSource.storeString(KEY_TOKEN, token)
  }

  override fun retrieveToken(): String? {
    return encryptedDataSource.retrieveString(KEY_TOKEN)
  }
}