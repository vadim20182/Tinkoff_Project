package android.example.tinkoffproject.screens.channels

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.example.tinkoffproject.data.ChannelItem
import android.example.tinkoffproject.recyclerview.ChannelsAdapter
import android.example.tinkoffproject.screens.channels.tabviewmodels.AllChannelsViewModel
import android.example.tinkoffproject.screens.channels.tabviewmodels.BaseChannelsViewModel
import android.example.tinkoffproject.screens.channels.tabviewmodels.MyChannelsViewModel
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ChannelsTabFragment : Fragment(R.layout.base_fragment_channels),
    ChannelsAdapter.OnItemClickedListener {

    private lateinit var viewModel: BaseChannelsViewModel
    private lateinit var channelsAdapter: ChannelsAdapter
    private lateinit var navController: NavController
    private var channelType: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)
        if (channelType == null)
            channelType = arguments?.getInt("channel_type") ?: ALL_CHANNELS

        viewModel = if (channelType == ALL_CHANNELS)
            ViewModelProvider(this)[AllChannelsViewModel::class.java]
        else
            ViewModelProvider(this)[MyChannelsViewModel::class.java]

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_channels)
        channelsAdapter = ChannelsAdapter(this)
        viewModel.channels.observe(viewLifecycleOwner) {
            channelsAdapter.data = it
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = channelsAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))

        var parent = parentFragment
        while (parent != null) {
            if (parent is NavHostFragment)
                navController = parent.findNavController()
            parent = parent.parentFragment
        }
    }

    override fun onItemClicked(position: Int, item: ChannelItem) {
        if (item.isTopic) {
            val bundle =
                bundleOf("channel_name" to item.parentChannel?.name, "topic_name" to item.name)
            navController.navigate(R.id.action_channelsFragment_to_chatFragment, bundle)
        } else {
            val count = viewModel.showOrHideTopics(position)
            if (count > 0)
                channelsAdapter.notifyItemRangeInserted(position, count)
            else
                channelsAdapter.notifyItemRangeRemoved(position, -count)
        }
    }

    companion object {
        const val MY_CHANNELS = 2
        const val ALL_CHANNELS = 1
    }
}