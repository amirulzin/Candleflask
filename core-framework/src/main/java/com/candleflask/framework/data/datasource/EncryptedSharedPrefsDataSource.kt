package com.candleflask.framework.data.datasource

import androidx.annotation.WorkerThread
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import javax.inject.Inject

/**
 * Based on Jetpack [EncryptedSharedPreferences]
 *
 * For Android SDK < 23 and >=18, a [Keystore based implementation](https://gist.github.com/Diederikjh/36ae22d5fde9d8f671a70b5d8cada90e)
 * can be used instead
 */
class EncryptedSharedPrefsDataSource @Inject constructor(
  private val encryptedPrefsController: EncryptedPrefsController
) : EncryptedDataSource {
  @WorkerThread
  override fun retrieveString(key: String): String? {
    return encryptedPrefsController.prefs.getString(key, null)
  }

  @WorkerThread
  override fun storeString(key: String, data: String?) {
    encryptedPrefsController.prefs.edit {
      putString(key, data)
    }
  }
}