package android.example.tinkoffproject

import android.app.Activity
import android.app.Application
import android.content.Context
import android.example.tinkoffproject.channels.ui.ChannelsAdapter
import android.example.tinkoffproject.di.AppComponent
import android.example.tinkoffproject.di.DaggerAppComponent
import android.util.Log
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins

class App : Application() {

    lateinit var component: AppComponent


    override fun onCreate() {
        super.onCreate()
        appContext = this
        component = DaggerAppComponent.factory().create(applicationContext)
        RxJavaPlugins.setErrorHandler { e ->
            if (e is UndeliverableException) {
                Log.e("Undeliverable exception", e.localizedMessage)
            } else {
                // Forward all others to current thread's uncaught exception handler
                Thread.currentThread().also { thread ->
                    thread.uncaughtExceptionHandler?.uncaughtException(thread, e)
                }
            }
        }
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}

fun Activity.getComponent() = (this.application as App).component