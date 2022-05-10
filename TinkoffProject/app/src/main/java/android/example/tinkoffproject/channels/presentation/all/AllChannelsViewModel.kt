package android.example.tinkoffproject.channels.presentation.all

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.repository.AllChannelsRepositoryImpl
import android.example.tinkoffproject.channels.presentation.BaseChannelsViewModel
import android.example.tinkoffproject.utils.convertChannelFromDbToNetwork
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class AllChannelsViewModel @Inject constructor(private val allChannelsRepository: AllChannelsRepositoryImpl) :
    BaseChannelsViewModel(allChannelsRepository) {
    private val compositeDisposable = CompositeDisposable()

    init {
        subscribeGetTopics()
        subscribeGetChannels()
        subscribeToSearch()
        subscribeChannelClick()
        _isLoaded.value = false
    }

    override fun subscribeGetTopics() {
        allChannelsRepository.getAllTopicsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            })
            .addTo(compositeDisposable)
    }

    override fun loadChannels() {
        allChannelsRepository.loadChannelsFromDb()
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
                    allChannelsRepository.allChannels.clear()
                    allChannelsRepository.allChannels.addAll(current)
                    for (parent in channelsProcessed)
                        allChannelsRepository.topics[parent.name] =
                            all.filter { it.parentChannel == parent.name }
                    allChannelsRepository.currentChannels = current
                    _isLoading.value = false
                } else {
                    _isLoading.value = true
                }
                if (isLoaded.value == false) {
                    allChannelsRepository.queryGetChannels.onNext(Unit)
                    _isLoaded.value = true
                }
                subscribeToDbUpdates()
            }, onError = {
            }).addTo(compositeDisposable)
    }

    override fun subscribeGetChannels() {
        allChannelsRepository.getAllChannelsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = "Ошибка при загрузке каналов"
            })
            .addTo(compositeDisposable)
    }

    private fun subscribeToDbUpdates() {
        allChannelsRepository.getChannelsFromDb()
            .map { dbList ->
                val dbResponse = dbList.map {
                    convertChannelFromDbToNetwork(it)
                }
                DbResult(dbResponse, dbResponse.filter { !it.isTopic })

            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                if (it.all.isNotEmpty()) {
                    allChannelsRepository.allChannels.clear()
                    allChannelsRepository.allChannels.addAll(it.channels)
                    allChannelsRepository.currentChannels = it.channels
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