package com.candleflask.framework.domain.features.tickers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class IsStreamConnectedUseCaseTest {
  @Test
  fun `when isStreamConnected emits, observed stateflow matches the emission list`() {
    val mutableState = MutableStateFlow(false)
    val mockRepository: TickerRepository = mock {
      on { isStreamConnected() } doReturn mutableState
    }

    val emissionList = mutableListOf<Boolean>()
    runBlocking {
      val collectionJob = launch(Dispatchers.Unconfined) {
        IsStreamConnectedUseCase(mockRepository).stateFlow.collectLatest {
          emissionList.add(it)
        }
      }

      launch {
        mutableState.value = true
        mutableState.value = false
        collectionJob.cancel()
      }
    }

    assertEquals(3, emissionList.size)
    assertEquals(listOf(false, true, false), emissionList)

    verify(mockRepository).isStreamConnected()
  }
}