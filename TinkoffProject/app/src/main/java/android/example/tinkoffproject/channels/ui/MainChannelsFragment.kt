package android.example.tinkoffproject.channels.ui

import android.content.Context
import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.presentation.MainChannelsViewModel
import android.example.tinkoffproject.getComponent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class MainChannelsFragment : Fragment(R.layout.fragment_channels_main) {
    private lateinit var viewPager: ViewPager2
    private lateinit var searchText: EditText
    private lateinit var addChannelButton: FloatingActionButton
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private val addChannelDialog by lazy { AddChannelDialogFragment() }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainChannelsViewModel by viewModels { viewModelFactory }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.requireActivity().getComponent().mainChannelsComponent().create().inject(this)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchText = view.findViewById(R.id.channel_search)
        searchText.doAfterTextChanged {
            viewModel.search(it?.toString().orEmpty())
        }
        addChannelButton = view.findViewById(R.id.add_channel_button)
        addChannelButton.setOnClickListener {
            childFragmentManager.beginTransaction()
                .add(addChannelDialog, "add channel")
                .commit()
        }
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        swipeRefresh.setOnRefreshListener {
            if (searchText.text.isBlank())
                viewModel.refresh()
            swipeRefresh.isRefreshing = false
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) {
            Snackbar.make(
                this.requireView(),
                it,
                Snackbar.LENGTH_SHORT
            ).apply {
                setTextColor(Color.WHITE)
                setBackgroundTint(Color.RED)
            }.show()
        }

        viewPager = view.findViewById(R.id.view_pager)
        viewPager.adapter =
            ChannelsFragmentAdapter(childFragmentManager, viewLifecycleOwner.lifecycle)
        viewPager.offscreenPageLimit = 1
        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text =
                if (position == 0) getString(R.string.tab_subscribed_channels) else getString(R.string.tab_all_channels)
        }.attach()
    }

    fun createChannel(channelName: String, description: String?) {
        viewModel.addChannel(channelName, description)
    }
}