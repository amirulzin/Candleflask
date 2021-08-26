package com.candleflask.framework.data.datasource

import android.app.Application
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject

class EncryptedPrefsController @Inject constructor(private val application: Application) {
  companion object {
    private const val PREF_FILENAME = "ENCRYPTED_USER_PREFS"
  }

  val prefs: SharedPreferences by lazy {
    EncryptedSharedPreferences.create(
      application,
      PREF_FILENAME,
      MasterKey(application),
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
  }
}