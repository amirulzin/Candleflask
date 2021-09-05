package com.candleflask.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.candleflask.android.R
import com.candleflask.android.databinding.HomeFragmentBinding
import com.candleflask.android.ui.ThemedTypedValues
import common.android.ui.UIResource
import common.android.ui.ViewBindingFragment
import common.android.ui.launchInViewLifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : ViewBindingFragment<HomeFragmentBinding>() {

  override fun createBinding(
    inflater: LayoutInflater,
    container: ViewGroup?,
    attachToParent: Boolean
  ): HomeFragmentBinding {
    return HomeFragmentBinding.inflate(inflater, container, attachToParent)
  }

  private val tickersViewModel: TickersViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initViews(ThemedTypedValues(requireContext()))
    initLogic()
    launchRoutines()
  }

  @UiThread
  private fun initViews(themedTypedValues: ThemedTypedValues) {
    with(binding.tickerRecyclerView) {
      adapter = SubscribedTickersAdapter(themedTypedValues)
    }
  }

  private fun initLogic() {
    with(binding.fabSetting) {
      setOnClickListener {
        findNavController().navigate(R.id.action_navHome_to_navTokenUpdate)
      }
    }

    with(binding.refreshLayout) {
      setOnRefreshListener {
        tickersViewModel.refresh(forceRefresh = true)
        tickersViewModel.connect(forceRefresh = true)
      }
    }
    with(binding.fabAddTicker) {
      setOnClickListener {
        findNavController().navigate(R.id.action_navHome_to_navSearchTicker)
      }
    }
  }

  private fun launchRoutines() {
    launchInViewLifecycleScope {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        tickersViewModel.connect(forceRefresh = true)
      }
    }
    launchInViewLifecycleScope {
      tickersViewModel.tickers.collectLatest { list ->
        (binding.tickerRecyclerView.adapter as? SubscribedTickersAdapter)
          ?.submitList(list)
      }
    }
    launchInViewLifecycleScope {
      tickersViewModel.loadingState.collectLatest { state ->
        binding.refreshLayout.isRefreshing = state is UIResource.Loading
      }
    }
    launchInViewLifecycleScope {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        launch {
          tickersViewModel.isStreamConnected.collectLatest { isConnected ->
            val imageRes = when {
              isConnected -> R.drawable.round_positive_8
              else -> R.drawable.round_white_stroke_8
            }
            binding.streamConnectionStatusImage.setImageResource(imageRes)
          }
        }
      }
    }
  }
}