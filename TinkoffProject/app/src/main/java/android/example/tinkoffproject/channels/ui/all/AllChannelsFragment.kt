package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.model.ChannelsRepository
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.database.AppDatabase
import androidx.fragment.app.viewModels
import androidx.paging.ExperimentalPagingApi

@ExperimentalPagingApi
class AllChannelsFragment : BaseChannelsTabFragment<ChannelEntity.AllChannelsEntity>() {
    override val viewModel: BaseChannelsViewModel<ChannelEntity.AllChannelsEntity> by viewModels {
        AllChannelViewModelFactory(
            ChannelsRepository(
                AppDatabase.getInstance(requireContext()).allChannelsDAO()
            )
        )
    }
}