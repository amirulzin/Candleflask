package com.candleflask.framework.data.datasource.tiingo.streaming

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import okio.Buffer
import javax.inject.Inject

class TiingoWSMessageConverter @Inject constructor() {
  private val moshi by lazy {
    Moshi.Builder()
      .add(TiingoWSResponse.Tick.MessageArray.MessageArrayJSONAdapter())
      .build()
  }

  private val wsRequestJSONAdapter by lazy { moshi.adapter(TiingoWSRequest::class.java) }

  private val wsSubscribeJSONAdapter by lazy { moshi.adapter(TiingoWSResponse.Subscribe::class.java) }

  private val wsHeartbeatJSONAdapter by lazy { moshi.adapter(TiingoWSResponse.HeartBeat::class.java) }

  private val wsTickJSONAdapter by lazy { moshi.adapter(TiingoWSResponse.Tick::class.java) }

  fun parseWebSocketMessage(text: String): TiingoWSResponse? {
    var messageKey: String? = null
    Buffer().writeUtf8(text).use { buffer ->
      JsonReader.of(buffer).use { reader ->
        reader.beginObject()
        while (reader.hasNext()) {
          if (TiingoWSResponse.KEY_MESSAGE_TYPE == reader.nextName() && reader.peek() == JsonReader.Token.STRING) {
            when (val matchingMessageKey = reader.nextString()) {
              TiingoWSResponse.Subscribe.TYPE_SUBSCRIBE,
              TiingoWSResponse.HeartBeat.TYPE_HEARTBEAT,
              TiingoWSResponse.Tick.TYPE_DATA_ARRAY -> {
                messageKey = matchingMessageKey
                break
              }
              else -> reader.skipValue()
            }
          } else {
            reader.skipValue()
          }
        }
      }
    }

    return when (messageKey) {
      TiingoWSResponse.Subscribe.TYPE_SUBSCRIBE -> wsSubscribeJSONAdapter.fromJson(text)
      TiingoWSResponse.HeartBeat.TYPE_HEARTBEAT -> wsHeartbeatJSONAdapter.fromJson(text)
      TiingoWSResponse.Tick.TYPE_DATA_ARRAY -> wsTickJSONAdapter.fromJson(text)
      else -> null
    }
  }

  fun asJson(request: TiingoWSRequest): String {
    return wsRequestJSONAdapter.toJson(request)
  }
}