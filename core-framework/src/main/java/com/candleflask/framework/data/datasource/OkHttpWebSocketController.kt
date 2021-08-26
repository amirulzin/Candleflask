package com.candleflask.framework.data.datasource

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Provider

class OkHttpWebSocketController @Inject constructor(private val httpClientProvider: Provider<OkHttpClient>) {
  companion object {
    private const val WS_CLOSURE_CODE = 1_000 //RFC 6455
  }

  private val httpClient by lazy { httpClientProvider.get() }

  private val mainSocketRef: AtomicReference<WebSocket?> = AtomicReference()

  fun optionallyReconnect(
    initializationRequest: Request,
    force: Boolean = false,
    listener: WebSocketListener
  ) {
    if (mainSocketRef.get() != null && force) {
      disconnect()
    }
    if (mainSocketRef.get() == null) {
      val webSocket = httpClient.newWebSocket(initializationRequest, listener)
      mainSocketRef.set(webSocket)
    }
  }

  fun disconnect() {
    try {
      mainSocketRef.get()?.close(WS_CLOSURE_CODE, reason = null)
    } finally {
      mainSocketRef.set(null)
    }
  }

  fun sendMessage(message: String) {
    mainSocketRef.get()?.send(message)
  }

  fun isSocketConnected(): Boolean = mainSocketRef.get() != null
}