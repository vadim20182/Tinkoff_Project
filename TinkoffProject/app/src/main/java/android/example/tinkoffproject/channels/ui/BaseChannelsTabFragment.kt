package android.example.tinkoffproject.channels.ui

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.model.ChannelItem
import android.example.tinkoffproject.chat.ui.ChatFragment
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

abstract class BaseChannelsTabFragment : Fragment(R.layout.base_fragment_channels),
    ChannelsAdapter.OnItemClickedListener {

    protected abstract val viewModel: BaseChannelsViewModel
    private val channelsAdapter: ChannelsAdapter by lazy { ChannelsAdapter(this) }
    private val navController: NavController by lazy {
        var parent = parentFragment
        var navController = findNavController()
        while (parent != null) {
            if (parent is NavHostFragment)
                navController = parent.findNavController()
            parent = parent.parentFragment
        }
        return@lazy navController
    }
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun afterTextChanged(text: Editable?) {
            if (text.toString() != viewModel.currentSearch) {
                val input = text?.toString().orEmpty()
                if (input.isNotBlank())
                    viewModel.searchChannels(input)
                else
                    viewModel.resetSearch()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_channels)
        val shimmer = requireParentFragment().requireView()
            .findViewById<ShimmerFrameLayout>(R.id.shimmer_channels_view)

        with(viewModel) {
            this.isAsyncTaskCompleted.observe(viewLifecycleOwner) { isAsyncTaskCompleted ->
                if (isAsyncTaskCompleted) {
                    shimmer.visibility = View.GONE
                    view.visibility = View.VISIBLE
                } else {
                    shimmer.visibility = View.VISIBLE
                    view.visibility = View.GONE
                }
            }
            this.channels.observe(viewLifecycleOwner) {
                channelsAdapter.data = it
            }
            this.itemToUpdate.observe(viewLifecycleOwner) {
                channelsAdapter.notifyItemChanged(it)
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = channelsAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        (parentFragment as MainChannelsFragment).addTextListener(this)
    }

    override fun onDestroyView() {
        (parentFragment as MainChannelsFragment).removeTextListener(this)
        super.onDestroyView()
    }

    override fun onItemClicked(position: Int, item: ChannelItem) {
        if (item.isTopic) {
            val bundle =
                bundleOf(
                    ChatFragment.ARG_CHANNEL_NAME to item.parentChannel,
                    ChatFragment.ARG_TOPIC_NAME to item.name
                )
            navController.navigate(R.id.action_channelsFragment_to_chatFragment, bundle)
        } else
            viewModel.clickChannel(position)
    }
}