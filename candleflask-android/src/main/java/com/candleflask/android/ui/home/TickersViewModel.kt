package com.candleflask.android.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.candleflask.framework.domain.entities.ticker.Ticker
import com.candleflask.framework.features.tickers.DisplaySubscribedTickersUseCase
import com.candleflask.framework.features.tickers.UpdateSubscribedTickersUseCase
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
  private val subscribedTickersUseCase: DisplaySubscribedTickersUseCase,
  private val updateSubscribedTickersUseCase: UpdateSubscribedTickersUseCase,
  private val application: Application
) : ViewModel() {

  private val _loadingState = MutableStateFlow<UIResource<Any>>(UIResource.Empty())
  val loadingState: StateFlow<UIResource<Any>> get() = _loadingState

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

  fun connect(forceRefresh: Boolean) {
    Log.d("@DBG-CONN", "Connecting with force: $forceRefresh")
    viewModelScope.launch {
      if (_loadingState.value !is UIResource.Loading) {
        _loadingState.value = UIResource.Loading()
        withContext(Dispatchers.IO) {
          if (application.isNetworkConnected()) {
            subscribedTickersUseCase.execute(forceRefresh)
          }
        }
        _loadingState.value = UIResource.Empty()
      }
    }
  }

  fun removeTicker(tickerItem: UITickerItem) {
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        updateSubscribedTickersUseCase.removeAndUnsubscribe(Ticker(tickerItem.model.symbol))
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