package common.android.network

import android.Manifest
import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresPermission

@RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
@TargetApi(Build.VERSION_CODES.M)
fun Application.isNetworkConnected(): Boolean {
  (applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)?.let { cm ->
    val activeNetwork = cm.activeNetwork ?: return false
    return cm.getNetworkCapabilities(activeNetwork)
      ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
      ?: false
  }
  return false
}