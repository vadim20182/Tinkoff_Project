package android.example.tinkoffproject.channels.presentation.my

import android.example.tinkoffproject.channels.data.db.ChannelEntity
import android.example.tinkoffproject.channels.data.repository.tabs.MyChannelsRepositoryImpl
import android.example.tinkoffproject.channels.presentation.BaseChannelsViewModel
import android.example.tinkoffproject.utils.convertChannelFromDbToNetwork
import android.example.tinkoffproject.utils.displayErrorMessage
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import javax.inject.Inject

class MyChannelsViewModel @Inject constructor(
    private val myChannelsRepository: MyChannelsRepositoryImpl
) :
    BaseChannelsViewModel(myChannelsRepository) {
    private val compositeDisposable = CompositeDisposable()

    init {
        subscribeGetChannels()
        subscribeGetTopics()
        subscribeToSearch()
        subscribeChannelClick()
        subscribeAddChannel()
        subscribeRefresh()
        _isLoaded.value = false
    }

    override fun subscribeGetChannels() {
        disposables[KEY_GET_MY_CHANNELS]?.dispose()
        disposables[KEY_GET_MY_CHANNELS]=myChannelsRepository.getMyChannelsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
            }, onError = {
                _errorMessage.value = displayErrorMessage(it, "Ошибка при загрузке каналов")
            })
    }


    override fun subscribeGetTopics() {
        disposables[KEY_GET_MY_TOPICS]?.dispose()
        disposables[KEY_GET_MY_TOPICS]=myChannelsRepository.getMyTopicsObservable
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {}, onError = {
                _errorMessage.value = displayErrorMessage(it, "Ошибка при загрузке топиков")
            })
    }

    override fun loadChannels() {
        disposables[KEY_LOAD_MY_CHANNELS]?.dispose()
        disposables[KEY_LOAD_MY_CHANNELS] = myChannelsRepository.loadMyChannelsFromDb()
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
                    myChannelsRepository.allChannels.clear()
                    myChannelsRepository.allChannels.addAll(current)
                    for (parent in channelsProcessed)
                        myChannelsRepository.topics[parent.name] =
                            all.filter { it.parentChannel == parent.name }
                    myChannelsRepository.currentChannels = current
                    _isLoading.value = false
                } else {
                    _isLoading.value = true
                }
                if (isLoaded.value == false) {
                    myChannelsRepository.queryGetChannels.onNext(Unit)
                    _isLoaded.value = true
                }
                subscribeToDbUpdates()
            }, onError = { _errorMessage.value = "Ошибка при загрузке" })
    }

    private fun subscribeToDbUpdates() {
        disposables[KEY_MY_SUBSCRIBE_TO_DB]?.dispose()
        disposables[KEY_MY_SUBSCRIBE_TO_DB] = myChannelsRepository.getMyChannelsFromDb()
            .map { dbList ->
                val dbResponse = dbList.map {
                    convertChannelFromDbToNetwork(it)
                }
                DbResult(dbResponse, dbResponse.filter { !it.isTopic })
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(onNext = {
                if (it.all.isNotEmpty()) {
                    myChannelsRepository.allChannels.clear()
                    myChannelsRepository.allChannels.addAll(it.channels)
                    myChannelsRepository.currentChannels = it.channels
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