package android.example.tinkoffproject.channels.ui.all

import android.example.tinkoffproject.channels.model.ChannelItem
import android.example.tinkoffproject.channels.ui.BaseChannelsViewModel
import android.example.tinkoffproject.contacts.model.ContactItem
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class AllChannelsViewModel : BaseChannelsViewModel() {
    override val allChannels = mutableListOf<ChannelItem>()


    init {
        loadChannels()
        subscribe()
    }

    override fun loadChannels() {
        _isAsyncTaskCompleted.value = false
        Completable.fromCallable {}
            .subscribeOn(Schedulers.io())
            .doOnSubscribe {
                allChannels.apply {
                    for (i in 0..19) {
                        val channel = ChannelItem("#Channel ${i + 1}")
                        this.add(channel)
                        if (topics[channel.name] == null)
                            topics[channel.name] = mutableListOf()
                        for (j in 0..5) {
                            topics[channel.name]?.add(
                                ChannelItem(
                                    "TOPIC ${j + 1}",
                                    true,
                                    parentChannel = channel.name
                                )
                            )
                        }
                    }
                }
            }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onComplete = {
                    _channels.value = allChannels
                    _isAsyncTaskCompleted.value = true
                }
            )
            .addTo(compositeDisposable)
    }

}