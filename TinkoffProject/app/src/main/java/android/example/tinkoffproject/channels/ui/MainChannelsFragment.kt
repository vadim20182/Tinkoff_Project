package android.example.tinkoffproject.channels.ui

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.os.Bundle
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainChannelsFragment : Fragment(R.layout.fragment_channels_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewPager = view.findViewById<ViewPager2>(R.id.view_pager)
        viewPager.adapter = ChannelsFragmentAdapter(this)

        val tabLayout = view.findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text =
                if (position == 0) getString(R.string.tab_subscribed_channels) else getString(R.string.tab_all_channels)
        }.attach()
    }
}