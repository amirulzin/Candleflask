package com.candleflask.android.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.android.di.DelegatedDispatchers
import com.candleflask.android.di.UIDelegatedStateFlow
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.features.tickers.ForceRefreshSnapshotTickersUseCase
import com.candleflask.framework.domain.features.tickers.IsStreamConnectedUseCase
import com.candleflask.framework.domain.features.tickers.StreamSubscribedTickersUseCase
import com.candleflask.framework.domain.features.tickers.UpdateSubscribedTickersUseCase
import common.android.network.isNetworkConnected
import common.android.ui.UIResource
import common.android.ui.UIResource.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class TickersViewModel @Inject constructor(
  private val delegatedDispatchers: DelegatedDispatchers,
  private val subscribedTickersUseCase: StreamSubscribedTickersUseCase,
  private val updateSubscribedTickersUseCase: UpdateSubscribedTickersUseCase,
  private val forceRefreshSnapshotTickersUseCase: ForceRefreshSnapshotTickersUseCase,
  private val isStreamConnectedUseCase: IsStreamConnectedUseCase,
  private val _loadingState: UIDelegatedStateFlow<Any>,
  private val application: Application
) : ViewModel() {

  private var initJob: Job? = null


  private val _tickers = MutableStateFlow<List<UITickerItem>>(emptyList())
  val tickers = _tickers.asStateFlow()

  val loadingState = _loadingState.immutable

  val isStreamConnected by lazy {
    isStreamConnectedUseCase.stateFlow
  }

  /**
   * For easier unit tests compared to init blocks
   */
  fun optionallyInit() {
    if (initJob == null) {
      initJob = viewModelScope.launch(delegatedDispatchers.IO) {
        subscribedTickersUseCase.tickerUpdates.map { tickerPrice ->
          tickerPrice.mapIndexed { index, item ->
            UITickerItem(index, item, isExpanded = isExpandedMode.get())
          }
        }.collectLatest { tickerList ->
          _tickers.update { tickerList }
        }
      }
    }
  }

  fun refresh(forceRefresh: Boolean) {
    if (_loadingState.value !is Loading) {
      viewModelScope.launch {
        withContext(delegatedDispatchers.IO) {
          _loadingState.value = Loading()
          if (application.isNetworkConnected() && forceRefresh) {
            forceRefreshSnapshotTickersUseCase.execute()
          }
          _loadingState.value = UIResource.Success(Any())
        }
      }
    }
  }

  fun connect(forceRefresh: Boolean) {
    viewModelScope.launch {
      withContext(delegatedDispatchers.IO) {
        if (application.isNetworkConnected()) {
          subscribedTickersUseCase.execute(forceRefresh)
        }
      }
    }
  }

  fun removeTicker(tickerItem: UITickerItem) {
    viewModelScope.launch {
      withContext(delegatedDispatchers.IO) {
        updateSubscribedTickersUseCase.removeAndUnsubscribe(Ticker(tickerItem.model.symbolNormalized))
      }
    }
  }

  fun addTicker(ticker: Ticker) {
    viewModelScope.launch {
      withContext(delegatedDispatchers.IO) {
        updateSubscribedTickersUseCase.addAndSubscribe(ticker)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    subscribedTickersUseCase.cleanUp()
    initJob = null
  }
}