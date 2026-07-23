package com.guillermonegrete.gallery.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.guillermonegrete.gallery.databinding.ItemNetworkStateBinding
import timber.log.Timber

class NetworkStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<NetworkStateAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ViewHolder {
        val binding = ItemNetworkStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, retry)
    }

    class ViewHolder(val binding: ItemNetworkStateBinding, retry: () -> Unit) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.retryButton.setOnClickListener { retry() }
        }

        fun bind(loadState: LoadState) = with(binding) {
            Timber.d("$absoluteAdapterPosition, $bindingAdapterPosition: $loadState")
            progressBar.isVisible = loadState is LoadState.Loading

            // Error views
            retryButton.isVisible = loadState is LoadState.Error
            errorMsg.isVisible = loadState is LoadState.Error
            if (loadState is LoadState.Error) errorMsg.text = loadState.error.localizedMessage
        }
    }
}
