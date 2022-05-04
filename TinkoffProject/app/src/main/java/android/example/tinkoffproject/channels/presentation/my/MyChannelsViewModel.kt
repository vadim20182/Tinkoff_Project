package android.example.tinkoffproject.channels.presentation.my

import android.example.tinkoffproject.channels.data.ChannelsRepository
import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.presentation.BaseChannelsViewModel
import android.example.tinkoffproject.utils.convertChannelFromDbToNetwork
import android.example.tinkoffproject.utils.convertChannelFromNetworkToDb
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MyChannelsViewModel @Inject constructor(channelsRepository: ChannelsRepository) :
    BaseChannelsViewModel(channelsRepository) {
    private val compositeDisposable = CompositeDisposable()

    init {
        subscribeGetTopics()
        subscribeGetChannels()
        subscribeToSearch()
        subscribeChannelClick()
        _isLoaded.value = false
    }

    override fun subscribeGetChannels() {
        channelsRepository.getMyChannelsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = { findMyChannels() }, onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            })
            .addTo(compositeDisposable)
    }

    override fun subscribeGetTopics() {
        channelsRepository.getMyTopicsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {}, onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            }).addTo(compositeDisposable)
    }

    private fun findMyChannels() {
        channelsRepository.getFindMyChannelsObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onComplete = {
                channelsRepository.insertChannelsReplace(channelsRepository.allChannels.map {
                    convertChannelFromNetworkToDb(
                        it, true
                    )
                })
            }, onError = {
                _errorMessage.value = "Ошибка при загрузке подписок"
            })
            .addTo(compositeDisposable)
    }


    override fun loadChannels() {
        channelsRepository.loadMyChannelsFromDb()
            .map { dbList ->
                Triple(dbList.map {
                    convertChannelFromDbToNetwork(it)
                }, dbList.map {
                    convertChannelFromDbToNetwork(it)
                }.filter { !it.isTopic }, dbList.map {
                    convertChannelFromDbToNetwork(it)
                }.filter { it.parentChannel == ChannelEntity.NO_PARENT })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onSuccess = { (all, current, channelsProcessed) ->
                if (all.isNotEmpty()) {
                    channelsRepository.allChannels.clear()
                    channelsRepository.allChannels.addAll(current)
                    for (parent in channelsProcessed)
                        channelsRepository.topics[parent.name] =
                            all.filter { it.parentChannel == parent.name }
                    channelsRepository.currentChannels = current
                    _isLoading.value = false
                } else {
                    _isLoading.value = true
                }
                if (isLoaded.value == false) {
                    channelsRepository.queryGetChannels.onNext(Unit)
                    _isLoaded.value = true
                }
                subscribeToDbUpdates()
            }).addTo(compositeDisposable)
    }

    private fun subscribeToDbUpdates() {
        channelsRepository.getMyChannelsFromDb()
            .map { dbList ->
                val dbResponse = dbList.map {
                    convertChannelFromDbToNetwork(it)
                }
                DbResult(dbResponse,dbResponse.filter { !it.isTopic })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                if (it.all.isNotEmpty()) {
                    channelsRepository.allChannels.clear()
                    channelsRepository.allChannels.addAll(it.channels)
                    channelsRepository.currentChannels = it.channels
                    _isLoading.value = false
                }
            }, onError = {
            }).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}