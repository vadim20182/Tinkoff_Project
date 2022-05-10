package android.example.tinkoffproject.profile.di

import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.contacts.data.network.GetPresenceResponse
import android.example.tinkoffproject.contacts.data.network.UserResponse
import android.example.tinkoffproject.network.ApiService
import android.example.tinkoffproject.utils.makePublishSubject
import dagger.Module
import dagger.Provides
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

@Module
class ProfileModule {

    @Provides
    @Profile
    fun getQueryUser() = makePublishSubject<Int>()

    @Provides
    @Profile
    fun getQueryUserPresence() = makePublishSubject<ContactItem>()

    @Provides
    @Profile
    fun getUserObservable(
        publishSubject: PublishSubject<Int>,
        client: ApiService
    ): Observable<UserResponse> = publishSubject
        .observeOn(Schedulers.io())
        .flatMapSingle {
            client.getUser(it)
        }

    @Provides
    @Profile
    fun getUserPresenceObservable(
        publishSubject: PublishSubject<ContactItem>,
        client: ApiService
    ): Observable<GetPresenceResponse> = publishSubject
        .observeOn(Schedulers.io())
        .flatMapSingle {
            client.getUserPresence(it.email)
        }
}