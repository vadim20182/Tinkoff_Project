package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.channels.ui.MyChannelViewModelFactory
import android.example.tinkoffproject.database.AppDatabase
import androidx.fragment.app.viewModels
import androidx.paging.ExperimentalPagingApi

@ExperimentalPagingApi
class MyChannelsFragment : BaseChannelsTabFragment() {
    override val viewModel: BaseChannelsViewModel by viewModels {
        MyChannelViewModelFactory(
            AppDatabase.getInstance(requireContext()).channelDAO()
        )
    }
}