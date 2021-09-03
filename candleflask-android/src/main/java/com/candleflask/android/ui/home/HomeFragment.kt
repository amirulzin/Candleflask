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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest

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

  /**
   * ADDENDUM:
   * Suspend block is **sequential**.
   * Don't make the mistake of grouping them all up (initializations, collections)
   * under a single `lifecycleScope.launch` if there's a blocking call in between.
   *
   * WARNING:
   * As per current lifecycle runtime alphas (2.4.x):
   *
   * * `repeatOnLifecycle` will multiply if launchRoutines() was called in OnViewCreated,
   *   We can change to other fragment, then pop back, and this issue will be displayed.
   *   Most likely due to LifecycleObserver not being uniquely hashcode()
   *
   * * `whenStarted` will not be called if we call this during in onCreate
   *
   * Due to the above, we must call this within the Fragment `init` block
   */
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
      tickersViewModel.isStreamConnected.collect { isConnected ->
        val imageRes =
          if (isConnected) R.drawable.round_positive_8
          else R.drawable.round_white_stroke_8
        binding.streamConnectionStatusImage.setImageResource(imageRes)
      }
    }
  }
}