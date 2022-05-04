package android.example.tinkoffproject.channels.ui.my

import android.content.Context
import android.example.tinkoffproject.channels.presentation.my.MyChannelsViewModel
import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.getComponent
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class MyChannelsFragment : BaseChannelsTabFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val viewModel: MyChannelsViewModel by viewModels { viewModelFactory }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.requireActivity().getComponent().myChannelsComponent().create().inject(this)

    }
}