package android.example.tinkoffproject

import android.app.Activity
import android.app.Application
import android.example.tinkoffproject.di.AppComponent
import android.example.tinkoffproject.di.DaggerAppComponent

class App : Application() {

    lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        component = DaggerAppComponent.factory().create(applicationContext)
    }
}

fun Activity.getComponent() = (this.application as App).component