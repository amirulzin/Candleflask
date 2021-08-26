package com.candleflask.android

import android.app.Application
import common.android.strictmode.StrictModeDefaults
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CandleFlaskApp : Application() {
  override fun onCreate() {
    super.onCreate()
    if (BuildConfig.DEBUG) {
      StrictModeDefaults.enableDefaultsWithoutSocketTagging()
    }
  }
}