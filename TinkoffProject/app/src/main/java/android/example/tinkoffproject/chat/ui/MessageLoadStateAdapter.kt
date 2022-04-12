package android.example.tinkoffproject.chat.ui

import android.example.tinkoffproject.databinding.ContactItemBinding
import android.example.tinkoffproject.databinding.MessageCustomViewGroupLayoutBinding
import android.example.tinkoffproject.databinding.MessagesLoadingItemBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView

class MessageLoadStateAdapter : LoadStateAdapter<RecyclerView.ViewHolder>() {

    override fun getStateViewType(loadState: LoadState): Int = when (loadState) {
        is LoadState.NotLoading -> error("not sup")
        LoadState.Loading -> PROGRESS
        is LoadState.Error -> ERROR
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, loadState: LoadState) {
        when (holder) {
            is LoadingViewHolder -> holder.bind()
            is ErrorViewHolder -> holder.bind()
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): RecyclerView.ViewHolder {
        return when (loadState) {
            LoadState.Loading -> {
                val binding = MessagesLoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                LoadingViewHolder(binding.root)
            }
            is LoadState.Error -> {
                val binding = MessagesLoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                LoadingViewHolder(binding.root)
            }
            is LoadState.NotLoading -> error("not sup")
        }

    }


    private companion object {
        private const val PROGRESS = 0
        private const val ERROR = 1
    }
}

sealed class LoaderBaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

private class LoadingViewHolder(view: View) : LoaderBaseViewHolder(view) {
    fun bind() {

    }
}

private class ErrorViewHolder(view: View) : LoaderBaseViewHolder(view) {
    fun bind() {

    }
}