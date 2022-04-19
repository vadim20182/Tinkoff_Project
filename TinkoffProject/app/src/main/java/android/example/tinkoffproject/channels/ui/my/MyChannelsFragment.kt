package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.data.ChannelsRepository
import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.channels.presentation.BaseChannelsViewModel
import android.example.tinkoffproject.channels.presentation.my.MyChannelViewModelFactory
import android.example.tinkoffproject.database.AppDatabase
import androidx.fragment.app.viewModels

class MyChannelsFragment : BaseChannelsTabFragment() {
    override val viewModel: BaseChannelsViewModel by viewModels {
        MyChannelViewModelFactory(
            ChannelsRepository(
                AppDatabase.getInstance(requireContext()).channelsDAO()
            )
        )
    }
}