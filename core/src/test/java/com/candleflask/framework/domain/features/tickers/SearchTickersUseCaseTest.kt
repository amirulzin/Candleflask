package com.candleflask.framework.domain.features.tickers

import com.candleflask.framework.domain.entities.ticker.TickerModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.*

class SearchTickersUseCaseTest {
  @Test
  fun `when search ticker success, exactly 1 result`() {
    val mockRepository: TickerRepository = mock {
      onBlocking { search(any()) } doReturn TickerRepository.OperationResult.Success(listOf(TickerModel("AMD")))
    }

    val searchInput = "AMD"
    val result = runBlocking {
      SearchTickersUseCase(mockRepository).execute(searchInput)
    }
    val successResult = result as? TickerRepository.OperationResult.Success<List<TickerModel>>
    require(successResult != null)
    assertEquals(1, successResult.output.size)

    argumentCaptor<String> {
      verifyBlocking(mockRepository) {
        search(capture())
        assertEquals(searchInput, firstValue)
      }
    }
  }

  @Test
  fun `when search invalid ticker, return empty list`() {
    val mockRepository: TickerRepository = mock {
      onBlocking { search(any()) } doReturn TickerRepository.OperationResult.Success(emptyList())
    }

    val searchInput = "InvalidTicker"
    val result = runBlocking {
      SearchTickersUseCase(mockRepository).execute(searchInput)
    }
    val successResult = result as? TickerRepository.OperationResult.Success<List<TickerModel>>
    require(successResult != null)
    assertEquals(0, successResult.output.size)

    argumentCaptor<String> {
      verifyBlocking(mockRepository) {
        search(capture())
        assertEquals(searchInput, firstValue)
      }
    }
  }

  @Test
  fun `when search with invalid token, return invalid token error`() {
    val mockRepository: TickerRepository = mock {
      onBlocking { search(any()) } doReturn TickerRepository.OperationResult.InvalidTokenError()
    }

    val searchInput = "InvalidTicker"
    val result = runBlocking {
      SearchTickersUseCase(mockRepository).execute(searchInput)
    }
    assertEquals(TickerRepository.OperationResult.InvalidTokenError::class, result::class)

    argumentCaptor<String> {
      verifyBlocking(mockRepository) {
        search(capture())
        assertEquals(searchInput, firstValue)
      }
    }
  }
}