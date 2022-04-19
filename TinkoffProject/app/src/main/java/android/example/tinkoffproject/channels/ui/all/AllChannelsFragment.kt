package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.data.ChannelsRepository
import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.channels.presentation.BaseChannelsViewModel
import android.example.tinkoffproject.channels.presentation.all.AllChannelViewModelFactory
import android.example.tinkoffproject.database.AppDatabase
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.viewModels

class AllChannelsFragment : BaseChannelsTabFragment() {
    override val viewModel: BaseChannelsViewModel by viewModels {
        AllChannelViewModelFactory(
            ChannelsRepository(
                AppDatabase.getInstance(requireContext()).channelsDAO()
            )
        )
    }
}