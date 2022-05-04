package android.example.tinkoffproject.profile

import android.example.tinkoffproject.di.AppComponent
import android.example.tinkoffproject.profile.ui.ProfileFragment
import dagger.Component
import javax.inject.Scope

@Profile
@Component(dependencies = [AppComponent::class])
interface ProfileComponent {

    fun inject(profileFragment: ProfileFragment)

    @Component.Factory
    interface Factory {
        fun create(
            appComponent: AppComponent
        ): ProfileComponent
    }
}

@Scope
annotation class Profile