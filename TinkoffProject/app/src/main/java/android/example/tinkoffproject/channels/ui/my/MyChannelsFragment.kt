package android.example.tinkoffproject.channels.ui.my

import android.content.Context
import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.presentation.my.MyChannelsViewModel
import android.example.tinkoffproject.channels.ui.BaseChannelsTabFragment
import android.example.tinkoffproject.getComponent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import javax.inject.Inject

class MyChannelsFragment : BaseChannelsTabFragment() {
    override lateinit var recyclerView: RecyclerView
    override lateinit var shimmer: ShimmerFrameLayout

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override val viewModel: MyChannelsViewModel by viewModels { viewModelFactory }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.requireActivity().getComponent().myChannelsComponent().create().inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.my_channels_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_view_my_channels)
        shimmer = view.findViewById(R.id.shimmer_channels_view)
        super.onViewCreated(view, savedInstanceState)
    }
}