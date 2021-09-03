package com.candleflask.android.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.domain.features.tickers.ForceRefreshSnapshotTickersUseCase
import com.candleflask.framework.domain.features.tickers.IsStreamConnectedUseCase
import com.candleflask.framework.domain.features.tickers.StreamSubscribedTickersUseCase
import com.candleflask.framework.domain.features.tickers.UpdateSubscribedTickersUseCase
import common.android.network.isNetworkConnected
import common.android.ui.UIResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TickersViewModel @Inject constructor(
  private val subscribedTickersUseCase: StreamSubscribedTickersUseCase,
  private val updateSubscribedTickersUseCase: UpdateSubscribedTickersUseCase,
  private val forceRefreshSnapshotTickersUseCase: ForceRefreshSnapshotTickersUseCase,
  private val isStreamConnectedUseCase: IsStreamConnectedUseCase,
  private val application: Application
) : ViewModel() {

  private val _loadingState = MutableStateFlow<UIResource<Any>>(UIResource.Empty())

  val loadingState: StateFlow<UIResource<Any>> get() = _loadingState

  val isStreamConnected by lazy {
    isStreamConnectedUseCase.observe()
  }

  val tickers by lazy {
    subscribedTickersUseCase.tickerUpdates
      .map { tickerPrice ->
        tickerPrice.mapIndexed { index, item ->
          UITickerItem(index, item)
        }
      }
      .flowOn(Dispatchers.Default)
      .stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
      )
  }

  fun refresh(forceRefresh: Boolean) {
    if (_loadingState.value !is UIResource.Loading) {
      viewModelScope.launch {
        withContext(Dispatchers.IO) {
          _loadingState.value = UIResource.Loading()
          if (application.isNetworkConnected() && forceRefresh) {
            forceRefreshSnapshotTickersUseCase.execute()
          }
          _loadingState.value = UIResource.Empty()
        }
      }
    }
  }

  fun connect(forceRefresh: Boolean) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        if (application.isNetworkConnected()) {
          subscribedTickersUseCase.execute(forceRefresh)
        }
      }
    }
  }

  fun removeTicker(tickerItem: UITickerItem) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        updateSubscribedTickersUseCase.removeAndUnsubscribe(Ticker(tickerItem.model.symbolNormalized))
      }
    }
  }

  fun addTicker(ticker: Ticker) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        updateSubscribedTickersUseCase.addAndSubscribe(ticker)
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    Log.d("@DBG", "VM Cleared")
    subscribedTickersUseCase.cleanUp()
  }
}