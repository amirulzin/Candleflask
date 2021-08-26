package com.candleflask.framework.data.datasource

interface EncryptedDataSource {
  fun retrieveString(key: String): String?

  fun storeString(key: String, data: String?)
}