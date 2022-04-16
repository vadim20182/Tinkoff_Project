package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.model.ChannelsRepository
import android.example.tinkoffproject.channels.model.db.ChannelEntity
import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.database.AppDatabase
import androidx.fragment.app.viewModels
import androidx.paging.ExperimentalPagingApi

@ExperimentalPagingApi
class MyChannelsFragment : BaseChannelsTabFragment<ChannelEntity.MyChannelsEntity>() {
    override val viewModel: BaseChannelsViewModel<ChannelEntity.MyChannelsEntity> by viewModels {
        MyChannelViewModelFactory(
            ChannelsRepository(
                AppDatabase.getInstance(requireContext()).myChannelsDAO()
            )
        )
    }
}