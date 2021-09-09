package com.candleflask.android.ui.searchticker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.UiThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.candleflask.android.databinding.SearchTickersItemBinding

class SearchTickersResultAdapter(private val itemDelegate: ItemDelegate) :
  ListAdapter<UISearchItem, SearchTickersResultAdapter.ViewHolder>(DiffCallback()) {

  class DiffCallback : DiffUtil.ItemCallback<UISearchItem>() {
    override fun areItemsTheSame(oldItem: UISearchItem, newItem: UISearchItem): Boolean {
      return oldItem.ticker == newItem.ticker
    }

    override fun areContentsTheSame(oldItem: UISearchItem, newItem: UISearchItem): Boolean {
      return oldItem == newItem
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val binding = SearchTickersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding, itemDelegate)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  class ViewHolder(private val binding: SearchTickersItemBinding, private val itemDelegate: ItemDelegate) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(searchItem: UISearchItem) {
      with(binding) {
        tickerName.text = searchItem.ticker.key
        tickerInfo.text = searchItem.priceCents?.orEmpty()
        tickerPrice.text = searchItem.info
        tickerIsAdded.visibility = if (searchItem.isAdded) View.VISIBLE else View.GONE
        rootLayout.setOnClickListener {
          itemDelegate.onClickItem(searchItem)
        }
      }
    }
  }

  fun interface ItemDelegate {
    @UiThread
    fun onClickItem(searchItem: UISearchItem)
  }
}