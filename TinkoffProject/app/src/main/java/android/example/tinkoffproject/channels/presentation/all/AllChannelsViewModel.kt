package android.example.tinkoffproject.channels.presentation.all

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.repository.tabs.AllChannelsRepositoryImpl
import android.example.tinkoffproject.channels.presentation.BaseChannelsViewModel
import android.example.tinkoffproject.utils.convertChannelFromDbToNetwork
import android.example.tinkoffproject.utils.displayErrorMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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
        subscribeAddChannel()
        subscribeRefresh()
        _isLoaded.value = false
    }

    override fun loadChannels() {
        disposables[KEY_LOAD_ALL_CHANNELS]?.dispose()
        disposables[KEY_LOAD_ALL_CHANNELS] = allChannelsRepository.loadChannelsFromDb()
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
            })
    }

    override fun subscribeGetChannels() {
        disposables[KEY_GET_ALL_CHANNELS]?.dispose()
        disposables[KEY_GET_ALL_CHANNELS]=allChannelsRepository.getAllChannelsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
            }, onError = {
                _errorMessage.value = displayErrorMessage(it,"Ошибка при загрузке каналов")
            })
    }

    override fun subscribeGetTopics() {
        disposables[KEY_GET_ALL_TOPICS]?.dispose()
        disposables[KEY_GET_ALL_TOPICS]=allChannelsRepository.getAllTopicsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onError = {
                _errorMessage.value = displayErrorMessage(it,"Ошибка при загрузке топиков")
            })
    }

    private fun subscribeToDbUpdates() {
        disposables[KEY_ALL_SUBSCRIBE_TO_DB]?.dispose()
        disposables[KEY_ALL_SUBSCRIBE_TO_DB] = allChannelsRepository.getChannelsFromDb()
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
            })
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}