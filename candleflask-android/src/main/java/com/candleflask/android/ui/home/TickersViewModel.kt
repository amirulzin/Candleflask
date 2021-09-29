package com.candleflask.android.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.android.di.DelegatedDispatchers
import com.candleflask.android.di.DelegatedNetwork
import com.candleflask.android.di.UIDelegatedStateFlow
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.features.tickers.ForceRefreshSnapshotTickersUseCase
import com.candleflask.framework.domain.features.tickers.IsStreamConnectedUseCase
import com.candleflask.framework.domain.features.tickers.StreamSubscribedTickersUseCase
import com.candleflask.framework.domain.features.tickers.UpdateSubscribedTickersUseCase
import common.android.ui.UIResource
import common.android.ui.UIResource.Loading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class TickersViewModel @Inject constructor(
  private val delegatedDispatchers: DelegatedDispatchers,
  private val streamSubscribedTickersUseCase: StreamSubscribedTickersUseCase,
  private val updateSubscribedTickersUseCase: UpdateSubscribedTickersUseCase,
  private val forceRefreshSnapshotTickersUseCase: ForceRefreshSnapshotTickersUseCase,
  private val isStreamConnectedUseCase: IsStreamConnectedUseCase,
  private val _loadingState: UIDelegatedStateFlow<Any>,
  private val _tickers: UIDelegatedStateFlow<List<UITickerItem>>,
  private val delegatedNetwork: DelegatedNetwork
) : ViewModel() {

  private var initJob: Job? = null

  private val isExpandedMode = AtomicBoolean(false)

  val tickers = _tickers.immutable

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
        streamSubscribedTickersUseCase.tickerUpdates.map { tickerPrice ->
          tickerPrice.mapIndexed { index, item ->
            UITickerItem(index, item, isExpanded = isExpandedMode.get())
          }.let { UIResource.Success(it) }
        }.collectLatest { tickerList ->
          _tickers.value = tickerList
        }
      }
    }
  }

  fun refresh(forceRefresh: Boolean) {
    if (_loadingState.value !is Loading) {
      viewModelScope.launch(delegatedDispatchers.IO) {
        if (delegatedNetwork.isNetworkConnected() && forceRefresh) {
          _loadingState.value = Loading()
          forceRefreshSnapshotTickersUseCase.execute()
          _loadingState.value = UIResource.Success(Any())
        }
      }
    }
  }

  fun connect(forceRefresh: Boolean) {
    viewModelScope.launch(delegatedDispatchers.IO) {
      if (delegatedNetwork.isNetworkConnected()) {
        streamSubscribedTickersUseCase.execute(forceRefresh)
      }
    }
  }

  fun removeTicker(tickerItem: UITickerItem) {
    viewModelScope.launch(delegatedDispatchers.IO) {
      updateSubscribedTickersUseCase.removeAndUnsubscribe(Ticker(tickerItem.model.symbolNormalized))
    }
  }

  fun toggleExpandedMode(isEnabled: Boolean = !isExpandedMode.get()) {
    viewModelScope.launch(delegatedDispatchers.DEFAULT) {
      isExpandedMode.set(isEnabled)
      _tickers.update { list ->
        list.map { ticker -> ticker.copy(isExpanded = isEnabled) }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    streamSubscribedTickersUseCase.cleanUp()
    initJob = null
  }
}