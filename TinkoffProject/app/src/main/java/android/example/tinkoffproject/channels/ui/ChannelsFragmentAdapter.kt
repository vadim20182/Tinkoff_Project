package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.ui.all.AllChannelsFragment
import android.example.tinkoffproject.channels.ui.my.MyChannelsFragment
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NUM_PAGES = 2

class ChannelsFragmentAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = NUM_PAGES

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MyChannelsFragment()
        else -> AllChannelsFragment()
    }
}