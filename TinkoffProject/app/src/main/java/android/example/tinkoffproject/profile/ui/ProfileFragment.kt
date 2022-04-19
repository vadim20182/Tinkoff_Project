package android.example.tinkoffproject.profile.ui

import android.content.Context
import android.example.tinkoffproject.R
import android.example.tinkoffproject.contacts.data.network.ContactItem
import android.example.tinkoffproject.network.NetworkClient
import android.example.tinkoffproject.utils.makePublishSubject
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.bumptech.glide.Glide
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.snackbar.Snackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy


class ProfileFragment : Fragment(R.layout.fragment_profile) {
    private val queryGetUser = makePublishSubject<Int>()
    private val queryGetUserPresence = makePublishSubject<ContactItem>()
    private val getUserObservable =
        NetworkClient.getUserObservable(queryGetUser)
    private val getUserPresenceObservable =
        NetworkClient.getUserPresenceForProfileObservable(queryGetUserPresence)
    private var getUserDisposable: Disposable? = null
    private var getUserPresenceDisposable: Disposable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userStatus = view.findViewById<TextView>(R.id.profile_user_status)
        val userName = view.findViewById<TextView>(R.id.profile_user_name)
        val userAvatar = view.findViewById<ImageView>(R.id.profile_user_avatar)
        val shimmer = view.findViewById<ShimmerFrameLayout>(R.id.shimmer_profile)
        val profileLayout = view.findViewById<ConstraintLayout>(R.id.profile_layout)

        if (arguments != null) {
            val status = arguments?.getString(ARG_PROFILE_STATUS)
            userStatus.text = status
            when (status) {
                "offline" -> userStatus.setTextColor(Color.RED)
                "idle" -> userStatus.setTextColor(Color.rgb(255, 165, 0))
                else -> userStatus.setTextColor(Color.GREEN)
            }
            userName.text = arguments?.getString(ARG_PROFILE_NAME)

            val avatarUrl = arguments?.getString(ARG_PROFILE_AVATAR)
            Glide.with(this)
                .asDrawable()
                .load(avatarUrl)
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.avatar)
                .into(userAvatar)

            val toolbar = view.findViewById<Toolbar>(R.id.profile_toolbar)
            toolbar.visibility = View.VISIBLE
            NavigationUI.setupWithNavController(
                toolbar,
                findNavController()
            )
            toolbar.title = ""
            toolbar.findViewById<TextView>(R.id.profile_toolbar_title).text =
                getString(R.string.profile_title)
        } else {
            val pref = context?.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            val editor = pref?.edit()
            if (pref?.getString(KEY_NAME, PREFS_DEFAULT_VALUE) == PREFS_DEFAULT_VALUE) {
                shimmer.visibility = View.VISIBLE
                profileLayout.visibility = View.GONE
            } else {
                userName.text = pref?.getString(KEY_NAME, PREFS_DEFAULT_VALUE)
                userStatus.text = "offline"
                Glide.with(this)
                    .asDrawable()
                    .load(
                        pref?.getString(
                            KEY_AVATAR,
                            PREFS_DEFAULT_VALUE
                        )
                    )
                    .placeholder(R.mipmap.ic_launcher_round)
                    .error(R.mipmap.avatar)
                    .into(userAvatar)
            }
            getUserDisposable = getUserObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    editor?.putString(KEY_NAME, it.user.name)
                    editor?.putString(KEY_AVATAR, it.user.avatarUrl)
                    editor?.apply()
                    userName.text = it.user.name
                    Glide.with(this)
                        .asDrawable()
                        .load(it.user.avatarUrl)
                        .placeholder(R.mipmap.ic_launcher_round)
                        .error(R.mipmap.avatar)
                        .into(userAvatar)
                    queryGetUserPresence.onNext(it.user)
                }, onError = {
                    Snackbar.make(
                        view,
                        "Ошибка загрузки профиля",
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        setTextColor(Color.WHITE)
                        setBackgroundTint(Color.RED)
                    }.show()
                })

            getUserPresenceDisposable = getUserPresenceObservable
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(onNext = {
                    when (it.presence.clientType.status) {
                        "offline" -> userStatus.setTextColor(Color.RED)
                        "idle" -> userStatus.setTextColor(Color.rgb(255, 165, 0))
                        else -> userStatus.setTextColor(Color.GREEN)
                    }
                    userStatus.text = it.presence.clientType.status
                    shimmer.visibility = View.GONE
                    profileLayout.visibility = View.VISIBLE
                }, onError = { })

            queryGetUser.onNext(NetworkClient.MY_USER_ID)
        }
    }

    override fun onDestroyView() {
        getUserDisposable?.dispose()
        getUserPresenceDisposable?.dispose()
        super.onDestroyView()
    }

    companion object {
        const val ARG_PROFILE_NAME = "name"
        const val ARG_PROFILE_STATUS = "status"
        const val ARG_PROFILE_AVATAR = "image"
        const val SHARED_PREFS = "profile shared prefs"
        const val KEY_NAME = "name"
        const val KEY_AVATAR = "avatar"
        const val PREFS_DEFAULT_VALUE = "empty"
    }
}