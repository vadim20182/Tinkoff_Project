package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import androidx.lifecycle.ViewModelProvider

class AllChannelsFragment : BaseChannelsTabFragment() {
    override val viewModel: BaseChannelsViewModel by lazy {
        return@lazy ViewModelProvider(this)[AllChannelsViewModel::class.java]
    }
}