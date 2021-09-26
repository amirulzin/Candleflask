package com.candleflask.framework.data.datasource

import okhttp3.Request
import okhttp3.WebSocketListener

interface WebSocketController {
  fun optionallyReconnect(initializationRequest: Request, force: Boolean = false, listener: WebSocketListener)

  fun disconnect()

  fun sendMessage(message: String)

  fun isSocketConnected(): Boolean
}