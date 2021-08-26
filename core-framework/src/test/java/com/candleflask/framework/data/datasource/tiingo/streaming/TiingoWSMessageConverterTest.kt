package com.candleflask.framework.data.datasource.tiingo.streaming

import com.squareup.moshi.Moshi
import org.junit.Assert.assertTrue
import org.junit.Test

class TiingoWSMessageConverterTest {

  @Test
  fun `parse to tick`() {
    val json =
      """
        {
          "data":[
            "T",
            "2021-08-23T13:55:25.037716298-04:00",
            1629741325037716298,
            "nvda",
            null,
            null,
            null,
            null,
            null,
            217.93,
            15,
            null,
            0,
            0,
            1,
            0
          ],
          "service":"iex",
          "messageType":"A"
        }
      """.trimIndent()

    with(TiingoWSMessageConverter()) {
      assertTrue(parseWebSocketMessage(json) is TiingoWSResponse.Tick)
    }
  }

  @Test
  fun parseWebSocketMessage() {
    val initMessage =
      """{"response": {"code": 200, "message": "Success"}, "data": {"subscriptionId": 2638658}, "messageType": "I"}"""
    val heartbeatMessage =
      """{"response": {"code": 200, "message": "HeartBeat"}, "messageType": "H"}"""
    val updateMessage =
      """{"data": ["T", "2021-08-23T14:15:45.893556030-04:00", 1629742545893556030, "nvda", null, null, null, null, null, 218.47, 50, null, 0, 1, 1, 0], "service": "iex", "messageType": "A"}"""

    with(TiingoWSMessageConverter()) {
      assertTrue(parseWebSocketMessage(initMessage) is TiingoWSResponse.Subscribe)
      assertTrue(parseWebSocketMessage(heartbeatMessage) is TiingoWSResponse.HeartBeat)
      assertTrue(parseWebSocketMessage(updateMessage) is TiingoWSResponse.Tick)
    }
  }

  @Test
  fun toJson() {
    with(TiingoWSMessageConverter()) {
      val request = TiingoWSRequest("subscribe", "test_keys")
      val json = asJson(request)
      assertTrue(json.isNotBlank())
      val moshi = Moshi.Builder().build()
      val adapter = moshi.adapter(TiingoWSRequest::class.java)

      assertTrue(adapter.fromJson(json) is TiingoWSRequest)
    }
  }
}