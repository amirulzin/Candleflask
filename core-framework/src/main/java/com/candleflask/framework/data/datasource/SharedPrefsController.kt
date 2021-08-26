package com.candleflask.framework.data.datasource

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import javax.inject.Inject

class SharedPrefsController @Inject constructor(private val application: Application) {
  companion object {
    private const val PREF_FILENAME = "USER_PREFS"
  }

  val prefs: SharedPreferences by lazy {
    application.getSharedPreferences(PREF_FILENAME, Context.MODE_PRIVATE)
  }
}