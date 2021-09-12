package com.candleflask.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.UiThread
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
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

  private val tickerAdapterDelegate = object : SubscribedTickersAdapter.ItemDelegate {
    override fun onClickDelete(data: UITickerItem) {
      tickersViewModel.removeTicker(data)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initViews(ThemedTypedValues(requireContext()))
    initLogic()
    launchRoutines(savedInstanceState == null)
  }

  @UiThread
  private fun initViews(themedTypedValues: ThemedTypedValues) {
    with(binding.tickerRecyclerView) {
      adapter = SubscribedTickersAdapter(themedTypedValues, tickerAdapterDelegate)
      addItemDecoration(DividerItemDecoration(requireContext(), LinearLayout.VERTICAL))
    }
    with(binding.refreshLayout) {
      with(themedTypedValues) {
        setProgressBackgroundColorSchemeColor(backgroundColor)
        setColorSchemeColors(colorPrimary, colorSecondary)
      }
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
        with(tickersViewModel) {
          refresh(forceRefresh = true)
          connect(forceRefresh = true)
        }
      }
    }
    with(binding.fabAddTicker) {
      setOnClickListener {
        findNavController().navigate(R.id.action_navHome_to_navSearchTicker)
      }
    }

    with(binding.fabExpandedMode) {
      setOnClickListener {
        tickersViewModel.toggleExpandedMode()
      }
    }
  }

  private fun launchRoutines(shouldInit: Boolean) {
    if (shouldInit) {
      launchInViewLifecycleScope {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
          with(tickersViewModel) {
            optionallyInit()
            refresh(forceRefresh = true)
            connect(forceRefresh = false)
          }
        }
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