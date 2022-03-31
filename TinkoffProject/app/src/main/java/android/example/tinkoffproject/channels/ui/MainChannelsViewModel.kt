package android.example.tinkoffproject.channels.ui

import androidx.lifecycle.ViewModel
import io.reactivex.subjects.PublishSubject

class MainChannelsViewModel : ViewModel() {

    fun search(input: String) {
        querySearch.onNext(input)
    }

    companion object {
        val querySearch = PublishSubject.create<String>()
    }
}