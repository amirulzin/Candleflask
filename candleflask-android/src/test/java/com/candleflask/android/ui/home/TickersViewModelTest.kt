package com.candleflask.android.ui.home

import com.candleflask.android.di.DelegatedDispatchers
import com.candleflask.android.di.UIDelegatedStateFlow
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.entities.ticker.TickerModel
import com.candleflask.framework.domain.features.tickers.*
import common.android.ui.UIResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class TickersViewModelTest {
  private lateinit var vm: TickersViewModel
  private lateinit var tickerRepositoryMock: TickerRepository
  private lateinit var testTickers: UIDelegatedStateFlow<List<UITickerItem>>

  @ExperimentalCoroutinesApi
  @Before
  fun setup() {
    testTickers = UIDelegatedStateFlow()
    tickerRepositoryMock = mock()
    vm = TickersViewModel(
      delegatedDispatchers = DelegatedDispatchers(TestCoroutineDispatcher()),
      streamSubscribedTickersUseCase = StreamSubscribedTickersUseCase(tickerRepositoryMock),
      updateSubscribedTickersUseCase = UpdateSubscribedTickersUseCase(tickerRepositoryMock),
      forceRefreshSnapshotTickersUseCase = ForceRefreshSnapshotTickersUseCase(tickerRepositoryMock),
      isStreamConnectedUseCase = IsStreamConnectedUseCase(tickerRepositoryMock),
      _loadingState = UIDelegatedStateFlow(),
      _tickers = testTickers,
      delegatedNetwork = mock { on { isNetworkConnected() } doReturn true }
    )
  }

  @Test
  fun `when invoking optionallyInit first time, invoke tickerRepository retrieveHotTickers`() {
    vm.optionallyInit()
    verify(tickerRepositoryMock).retrieveHotTickers()
  }

  @Test
  fun `when invoking optionallyInit subsequently, only invoke tickerRepository retrieveHotTickers once`() {
    runBlocking {
      vm.optionallyInit()
      delay(50)
      vm.optionallyInit()
    }
    verify(tickerRepositoryMock).retrieveHotTickers()
  }

  @Test
  fun `when refresh is forced, invoke forceSnapshotUpdate`() {
    val emissionList = mutableListOf<UIResource<Any>>()
    runBlocking {
      val collectionJob = launch(Dispatchers.Unconfined) {
        vm.loadingState.collectLatest {
          emissionList.add(it)
        }
      }

      launch {
        assertTrue(emissionList[0] is UIResource.Empty)

        vm.refresh(forceRefresh = true)
        assertTrue(emissionList[1] is UIResource.Loading)

        delay(25)
        assertTrue(emissionList[2] is UIResource.Success)

        collectionJob.cancel()
      }
    }

    verifyBlocking(tickerRepositoryMock) {
      forceSnapshotUpdate()
    }
  }

  @Test
  fun `when refresh is not forced, never invoke forceSnapshotUpdate`() {
    val emissionList = mutableListOf<UIResource<Any>>()
    runBlocking {
      val collectionJob = launch(Dispatchers.Unconfined) {
        vm.loadingState.collectLatest {
          emissionList.add(it)
        }
      }

      launch {
        assertTrue(emissionList[0] is UIResource.Empty)
        vm.refresh(forceRefresh = false)
        delay(25)
        collectionJob.cancel()
      }
    }

    assertEquals(1, emissionList.size)

    verifyBlocking(tickerRepositoryMock, never()) {
      forceSnapshotUpdate()
    }
  }

  @Test
  fun `when connect is forced, invoke tickerRepository optionallyReconnect forced`() {
    vm.connect(forceRefresh = true)

    argumentCaptor<Boolean> {
      verifyBlocking(tickerRepositoryMock) {
        optionallyReconnect(force = capture())
      }
      assertEquals(true, firstValue)
    }
  }

  @Test
  fun `when connect is not forced, invoke tickerRepository optionallyReconnect without force`() {
    vm.connect(forceRefresh = false)

    argumentCaptor<Boolean> {
      verifyBlocking(tickerRepositoryMock) {
        optionallyReconnect(force = capture())
      }
      assertEquals(false, firstValue)
    }
  }

  @Test
  fun `when removeTicker, invoke removeAndUnsubscribeTicker`() {
    val tickerModel = TickerModel("NVDA")
    vm.removeTicker(UITickerItem(model = tickerModel))

    argumentCaptor<Ticker> {
      verifyBlocking(tickerRepositoryMock) {
        removeAndUnsubscribeTicker(capture())
      }
      assertEquals(tickerModel.symbolNormalized, firstValue.key)
    }
  }

  @Test
  fun `when toggleExpandedMode is true, items must have expanded flag true`() {

    val tickerModel = TickerModel("NVDA")
    testTickers.value = UIResource.Success(listOf(UITickerItem(model = tickerModel, isExpanded = false)))
    vm.toggleExpandedMode()

    val list = (testTickers.value as UIResource.Success<List<UITickerItem>>).payload
    val matchingModel = requireNotNull(list.find { it.model.symbolNormalized == tickerModel.symbolNormalized })

    assertEquals(UITickerItem(model = tickerModel, isExpanded = true), matchingModel)
    verifyZeroInteractions(tickerRepositoryMock)
  }

  @Test
  fun `when toggleExpandedMode is subsequently reversed toggled, items must have expanded flag false`() {

    val tickerModel = TickerModel("NVDA")
    testTickers.value = UIResource.Success(listOf(UITickerItem(model = tickerModel, isExpanded = false)))

    fun currentTickersPayload() = (testTickers.value as UIResource.Success<List<UITickerItem>>).payload
    fun requireMatchingModel() = requireNotNull(
      currentTickersPayload().find { it.model.symbolNormalized == tickerModel.symbolNormalized }
    )

    vm.toggleExpandedMode()
    val toggledMatchingModel = requireMatchingModel()
    assertEquals(UITickerItem(model = tickerModel, isExpanded = true), toggledMatchingModel)

    vm.toggleExpandedMode()
    val unToggledMatchingModel = requireMatchingModel()
    assertEquals(UITickerItem(model = tickerModel, isExpanded = false), unToggledMatchingModel)

    verifyZeroInteractions(tickerRepositoryMock)
  }
}