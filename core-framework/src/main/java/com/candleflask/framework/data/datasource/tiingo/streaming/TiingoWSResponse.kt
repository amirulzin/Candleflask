package com.candleflask.framework.data.datasource.tiingo.streaming

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.ToJson

sealed class TiingoWSResponse(open val messageType: String) {
  companion object {
    const val KEY_MESSAGE_TYPE = "messageType"
  }

  @JsonClass(generateAdapter = true)
  data class Subscribe(
    val response: ResponseStatus,
    val data: SubscriptionId?,
    override val messageType: String = TYPE_SUBSCRIBE
  ) : TiingoWSResponse(messageType) {
    companion object {
      const val TYPE_SUBSCRIBE = "I"
    }

    @JsonClass(generateAdapter = true)
    data class SubscriptionId(val subscriptionId: Long)
  }

  @JsonClass(generateAdapter = true)
  data class HeartBeat(
    val response: ResponseStatus,
    override val messageType: String = TYPE_HEARTBEAT
  ) : TiingoWSResponse(messageType) {
    companion object {
      const val TYPE_HEARTBEAT = "H"
    }
  }


  @JsonClass(generateAdapter = true)
  data class Tick(
    val data: MessageArray,
    val service: String = "iex",
    override val messageType: String = TYPE_DATA_ARRAY,
  ) : TiingoWSResponse(messageType) {
    companion object {
      const val TYPE_DATA_ARRAY = "A"

      @JvmStatic
      fun from(jsonReader: JsonReader): Tick? {
        jsonReader.beginObject()
        if (jsonReader.nextName() == "data") {
          with(jsonReader) {
            createMessageArray()?.let { array ->
              return Tick(array)
            }
          }
        }

        return null
      }

      private fun JsonReader.createMessageArray(): MessageArray? {
        if (peek() == JsonReader.Token.BEGIN_ARRAY) {

          beginArray()
          val messageArray = MessageArray(
            code = nextString(),
            quoteTimeISO8601 = nextString(),
            epochNanos = nextLong(),
            ticker = nextString(),
            bidSize = readNullable(::nextInt),
            bidPrice = readNullable(::nextString),
            midPrice = readNullable(::nextString),
            askPrice = readNullable(::nextString),
            askSize = readNullable(::nextInt),
            lastPrice = readNullable(::nextString),
            lastSize = readNullable(::nextInt),
            halted = readNullable(::nextInt),
            afterHours = readNullable(::nextInt),
            sweepOrder = readNullable(::nextInt),
            oddLot = readNullable(::nextInt),
            isRule661 = readNullable(::nextInt)
          )
          endArray()
          return messageArray
        }
        return null
      }

      private inline fun <T> JsonReader.readNullable(crossinline ifNonNull: () -> T): T? {
        return if (peek() != JsonReader.Token.NULL) {
          ifNonNull()
        } else {
          nextNull()
        }
      }
    }

    data class MessageArray(
      val code: String, //"T" for last trade message | "Q" for top of book | "B" for trade break
      val quoteTimeISO8601: String,
      val epochNanos: Long,
      val ticker: String,
      val bidSize: Int?,
      val bidPrice: String?,
      val midPrice: String?,
      val askPrice: String?,
      val askSize: Int?,
      val lastPrice: String?, //Only for "T" or "B"
      val lastSize: Int?, //Only for "T" or "B"
      val halted: Int?, //0 = not halted, 1 = halted
      val afterHours: Int?, //0 = market hours, 1 = after hours,
      val sweepOrder: Int?, //0 = non ISO order, 1 = inter-market sweep order (ISO)
      val oddLot: Int?, //Only for "T" 0 = trade a round or mixed lot, 1 = odd lot
      val isRule661: Int?, //Only for "T", 0 if the trade is subject to Rule NMS 611, 1 otherwise
    ) {

      class MessageArrayJSONAdapter {
        @FromJson
        fun fromJson(jsonReader: JsonReader): MessageArray? {
          return jsonReader.createMessageArray()
        }

        @ToJson
        fun toJson(@Suppress("UNUSED_PARAMETER") messageArray: MessageArray): String? {
          throw IllegalStateException("${MessageArray::class.simpleName} can't be used for outbound message")
        }
      }
    }
  }

  @JsonClass(generateAdapter = true)
  data class ResponseStatus(val code: Int, val message: String)
}