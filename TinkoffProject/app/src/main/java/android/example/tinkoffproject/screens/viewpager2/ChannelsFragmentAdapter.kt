package android.example.tinkoffproject.screens.viewpager2

import android.example.tinkoffproject.screens.channels.ChannelsTabFragment
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NUM_PAGES = 2

class ChannelsFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = NUM_PAGES

    override fun createFragment(position: Int): Fragment = ChannelsTabFragment().apply {
        arguments = Bundle().apply {
            putInt(
                "channel_type", when (position) {
                    1 -> ChannelsTabFragment.ALL_CHANNELS
                    else -> ChannelsTabFragment.MY_CHANNELS
                }
            )
        }
    }

}