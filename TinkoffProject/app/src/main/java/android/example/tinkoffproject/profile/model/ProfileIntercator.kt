package android.example.tinkoffproject.profile.model

import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.contacts.data.network.GetPresenceResponse
import android.example.tinkoffproject.contacts.data.network.UserResponse
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ProfileInteractor @Inject constructor(
    val userObservable: Observable<UserResponse>,
    val userPresenceObservable: Observable<GetPresenceResponse>,
    val queryGetUser: PublishSubject<Int>,
    val queryGetUserPresence: PublishSubject<ContactItem>
)