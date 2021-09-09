package com.candleflask.android.ui.searchticker

import com.candleflask.android.di.DelegatedDispatchers
import com.candleflask.android.di.UIDelegatedStateFlow
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.domain.features.tickers.SearchTickersUseCase
import com.candleflask.framework.domain.features.tickers.TickerRepository
import com.candleflask.framework.domain.features.tickers.TickerRepository.OperationResult
import com.candleflask.framework.domain.features.tickers.UpdateSubscribedTickersUseCase
import common.android.ui.UIResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class SearchTickerViewModelTest {
  private lateinit var vm: SearchTickerViewModel
  private lateinit var tickerRepositoryMock: TickerRepository

  private val searchInput = "NVDA"
  private val validTickerModel = TickerModel("NVDA")
  private val invalidSearchInput = "INVALID_TICKER"

  @ExperimentalCoroutinesApi
  @Before
  fun setUp() {
    tickerRepositoryMock = mock {
      onBlocking { search(searchInput) } doReturn OperationResult.Success(listOf(validTickerModel))
      onBlocking { search(invalidSearchInput) } doReturn OperationResult.Success(emptyList())
    }
    vm = SearchTickerViewModel(
      delegatedDispatchers = DelegatedDispatchers(TestCoroutineDispatcher()),
      searchTickersUseCase = SearchTickersUseCase(tickerRepositoryMock),
      updateSubscribedTickersUseCase = UpdateSubscribedTickersUseCase(tickerRepositoryMock),
      _searchResultStateFlow = UIDelegatedStateFlow(),
      delegatedNetwork = { true }
    )
  }

  @Test
  fun `when search valid Ticker, returns valid result`() {
    vm.searchTicker(searchInput)
    assertEquals(
      vm.searchResultStateFlow.value,
      UIResource.Success(listOf(UISearchItem(Ticker(validTickerModel.symbolNormalized))))
    )

    argumentCaptor<String> {
      verifyBlocking(tickerRepositoryMock) {
        search(capture())
      }
      assertEquals(searchInput, firstValue)
    }
  }

  @Test
  fun `when search invalid Ticker, returns empty result`() {
    vm.searchTicker(invalidSearchInput)
    assertEquals(
      vm.searchResultStateFlow.value,
      UIResource.Success(emptyList<UISearchItem>())
    )

    argumentCaptor<String> {
      verifyBlocking(tickerRepositoryMock) {
        search(capture())
      }
      assertEquals(invalidSearchInput, firstValue)
    }
  }

}