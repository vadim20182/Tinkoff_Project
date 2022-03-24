package android.example.tinkoffproject.channels.ui.my

import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import androidx.lifecycle.ViewModelProvider

class MyChannelsFragment : BaseChannelsTabFragment() {
    override val viewModel: BaseChannelsViewModel by lazy {
        return@lazy ViewModelProvider(this)[MyChannelsViewModel::class.java]
    }
}