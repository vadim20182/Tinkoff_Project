package android.example.tinkoffproject.channels.ui

import androidx.fragment.app.Fragment
import android.example.tinkoffproject.R
import android.example.tinkoffproject.channels.data.network.ChannelItem
import android.example.tinkoffproject.channels.presentation.BaseChannelsViewModel
import android.example.tinkoffproject.chat.ui.ChatFragment
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.SingleSubject
import java.util.concurrent.TimeUnit

abstract class BaseChannelsTabFragment :
    Fragment(R.layout.base_fragment_channels),
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
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_channels)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_channels_view)
        val queryUpdateChannels = PublishSubject.create<Boolean>()

        makeSearchDisposable(queryUpdateChannels, shimmer, recyclerView)
            .addTo(compositeDisposable)

        with(viewModel) {
            isLoading.observe(viewLifecycleOwner) { isLoading ->
                queryUpdateChannels.onNext(isLoading)
            }
            isChannelClicked.observe(viewLifecycleOwner) {
                val res = channelsAdapter.update(viewModel.channelsRepository.currentChannels)
                channelsAdapter.data = viewModel.channelsRepository.currentChannels
                res.dispatchUpdatesTo(channelsAdapter)
            }
            itemToUpdate.observe(viewLifecycleOwner) {
                channelsAdapter.notifyItemChanged(it)
            }
            errorMessage.observe(viewLifecycleOwner) {
                Snackbar.make(
                    view,
                    it,
                    Snackbar.LENGTH_SHORT
                ).apply {
                    setTextColor(Color.WHITE)
                    setBackgroundTint(Color.RED)
                }.show()
            }
            if (isLoaded.value == false)
                loadChannels()
        }

        recyclerView.itemAnimator = null
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = channelsAdapter
        recyclerView.addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
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

    override fun onDestroyView() {
        compositeDisposable.clear()
        super.onDestroyView()
    }

    private fun makeSearchDisposable(
        querySearch: PublishSubject<Boolean>,
        shimmer: ShimmerFrameLayout,
        recyclerView: RecyclerView
    ) = querySearch
        .debounce(100, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .switchMapSingle {
            val queryTimeoutReset = SingleSubject.create<Boolean>()
            val disposableTimeout = queryTimeoutReset
                .subscribeOn(Schedulers.io())
                .timeout(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onError = {
                    shimmer.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                })
            Single.fromCallable {
                Pair(
                    it, channelsAdapter.update(viewModel.channelsRepository.currentChannels)
                )
            }
                .subscribeOn(Schedulers.io())
                .doAfterSuccess {
                    queryTimeoutReset.onSuccess(true)
                    disposableTimeout.dispose()
                }
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onNext = {
            channelsAdapter.data = viewModel.channelsRepository.currentChannels
            it.second.dispatchUpdatesTo(channelsAdapter)
            recyclerView.scrollToPosition(0)
            if (!it.first) {
                shimmer.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            } else {
                shimmer.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
        })
}