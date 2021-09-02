package com.candleflask.android.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.candleflask.android.databinding.HomeTickersItemBinding

class SubscribedTickersAdapter :
  ListAdapter<UITickerItem, SubscribedTickersAdapter.ViewHolder>(DiffCallback()) {

  class ViewHolder(private val binding: HomeTickersItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
      @JvmStatic
      fun create(parent: ViewGroup) = ViewHolder(
        HomeTickersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
      )
    }

    fun bind(data: UITickerItem) {
      binding.apply {
        tickerName.text = data.model.symbol
        tickerPrice.text = data.model.currentPrice?.amount?.toPlainString() ?: "Retrieving"
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder.create(parent)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  private class DiffCallback : DiffUtil.ItemCallback<UITickerItem>() {
    override fun areItemsTheSame(oldItem: UITickerItem, newItem: UITickerItem) =
      oldItem.model.symbol == newItem.model.symbol

    override fun areContentsTheSame(oldItem: UITickerItem, newItem: UITickerItem): Boolean =
      oldItem == newItem
  }
}