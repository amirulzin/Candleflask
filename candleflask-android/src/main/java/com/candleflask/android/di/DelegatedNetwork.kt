package com.candleflask.android.di

import android.app.Application
import common.android.network.isNetworkConnected
import javax.inject.Inject


fun interface DelegatedNetwork {
  fun isNetworkConnected(): Boolean
}

class AndroidDelegatedNetwork @Inject constructor(private val application: Application) : DelegatedNetwork {
  override fun isNetworkConnected(): Boolean {
    return application.isNetworkConnected()
  }
}