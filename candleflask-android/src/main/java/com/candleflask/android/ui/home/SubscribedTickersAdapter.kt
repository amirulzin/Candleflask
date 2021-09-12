package com.candleflask.android.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.candleflask.android.databinding.HomeTickersItemBinding
import com.candleflask.android.ui.ThemedTypedValues
import com.candleflask.framework.domain.entities.ticker.TickerModel.PriceMovement.*

class SubscribedTickersAdapter(
  private val themedTypedValues: ThemedTypedValues,
  private val itemDelegate: ItemDelegate
) :
  ListAdapter<UITickerItem, SubscribedTickersAdapter.ViewHolder>(DiffCallback()) {

  class ViewHolder(private val binding: HomeTickersItemBinding, private val itemDelegate: ItemDelegate) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
      @JvmStatic
      fun create(parent: ViewGroup, itemDelegate: ItemDelegate) = ViewHolder(
        HomeTickersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        itemDelegate
      )
    }

    fun bind(data: UITickerItem, themedTypedValues: ThemedTypedValues) {
      with(binding) {
        tickerName.text = data.model.symbolNormalized

        with(tickerPrice) {
          text = data.currentPriceString ?: "Retrieving"

          setTextColor(
            when (data.model.priceMovement) {
              POSITIVE -> themedTypedValues.textColorPositive
              NEGATIVE -> themedTypedValues.textColorNegative
              UNKNOWN -> themedTypedValues.textColorNeutral
            }
          )
        }

        val expandedElementVisibility = if (data.isExpanded) View.VISIBLE else View.GONE

        with(tickerDelete) {
          visibility = expandedElementVisibility

          setOnClickListener { itemDelegate.onClickDelete(data) }
        }
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return ViewHolder.create(parent, itemDelegate)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position), themedTypedValues)
  }

  private class DiffCallback : DiffUtil.ItemCallback<UITickerItem>() {
    override fun areItemsTheSame(oldItem: UITickerItem, newItem: UITickerItem) =
      oldItem.model.symbolNormalized == newItem.model.symbolNormalized

    override fun areContentsTheSame(oldItem: UITickerItem, newItem: UITickerItem): Boolean =
      oldItem == newItem
  }

  interface ItemDelegate {
    fun onClickDelete(data: UITickerItem)
  }
}