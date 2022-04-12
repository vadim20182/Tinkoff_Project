package android.example.tinkoffproject.channels.ui

import android.example.tinkoffproject.channels.ui.all.AllChannelsFragment
import android.example.tinkoffproject.channels.ui.my.MyChannelsFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.paging.ExperimentalPagingApi
import androidx.viewpager2.adapter.FragmentStateAdapter

private const val NUM_PAGES = 2

@ExperimentalPagingApi
class ChannelsFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int = NUM_PAGES

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> MyChannelsFragment()
        else -> AllChannelsFragment()
    }
}