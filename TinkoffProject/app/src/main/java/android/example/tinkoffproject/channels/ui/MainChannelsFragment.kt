package android.example.tinkoffproject.channels.ui

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.ui.all.AllChannelsViewModel
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.paging.ExperimentalPagingApi
import androidx.viewpager2.widget.ViewPager2
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

@ExperimentalPagingApi
class MainChannelsFragment : Fragment(R.layout.fragment_channels_main) {
    private lateinit var viewPager: ViewPager2
    private lateinit var searchText: EditText
    private val viewModel: MainChannelsViewModel by lazy { ViewModelProvider(this)[MainChannelsViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchText = view.findViewById(R.id.channel_search)
        searchText.doAfterTextChanged {
            viewModel.search(it?.toString().orEmpty())
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
}