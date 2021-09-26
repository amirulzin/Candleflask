package common.android.strictmode

import android.annotation.TargetApi
import android.os.Build
import android.os.StrictMode

object StrictModeDefaults {

  /**
   * StrictMode defaults that avoid [StrictMode.VmPolicy.Builder.detectUntaggedSockets]
   * since its incorrectly warns default OkHttp behaviors
   */
  @JvmStatic
  @TargetApi(Build.VERSION_CODES.S)
  fun enableDefaultsWithoutSocketTagging() {
    val threadPolicy = StrictMode.ThreadPolicy.Builder()
      .detectAll()
      .penaltyLog()
      .build()

    StrictMode.setThreadPolicy(threadPolicy)

    val vmPolicy = StrictMode.VmPolicy.Builder().apply {
      val targetSdk = Build.VERSION.SDK_INT
      if (targetSdk >= Build.VERSION_CODES.HONEYCOMB) {
        detectActivityLeaks()
        detectLeakedClosableObjects()
      }
      if (targetSdk >= Build.VERSION_CODES.JELLY_BEAN) {
        detectLeakedRegistrationObjects()
      }
      if (targetSdk >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
        detectFileUriExposure()
      }
      if (targetSdk >= Build.VERSION_CODES.M) {
        detectCleartextNetwork()
      }
      if (targetSdk >= Build.VERSION_CODES.O) {
        detectContentUriWithoutPermission()
        // detectUntaggedSockets()
      }
      if (targetSdk >= Build.VERSION_CODES.Q) {
        detectCredentialProtectedWhileLocked()
      }

      if (targetSdk >= Build.VERSION_CODES.S) {
        detectIncorrectContextUse()
      }
    }.penaltyLog()
      .build()

    StrictMode.setVmPolicy(vmPolicy)
  }
}