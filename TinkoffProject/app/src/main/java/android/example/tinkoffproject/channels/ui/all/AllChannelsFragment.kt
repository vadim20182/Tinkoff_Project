package android.example.tinkoffproject.channels.ui.all

import android.content.Context
import android.example.tinkoffproject.channels.presentation.all.AllChannelsViewModel
import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.getComponent
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class AllChannelsFragment : BaseChannelsTabFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val viewModel: AllChannelsViewModel by viewModels { viewModelFactory }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.requireActivity().getComponent().allChannelsComponent().create().inject(this)
    }
}