package android.example.tinkoffproject

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.snackbar.Snackbar


class MainActivity : AppCompatActivity() {
    val context: Context by lazy { applicationContext }
    private val connectivityManager: ConnectivityManager by lazy {
        getSystemService(ConnectivityManager::class.java)
    }
    private val mainView: View by lazy { this.findViewById(R.id.activity_main_view) }
    private var hasNetworkBeenLost = false
    private val networkCallback: ConnectivityManager.NetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (hasNetworkBeenLost) {
                    Snackbar.make(
                        mainView,
                        getString(R.string.network_is_available),
                        Snackbar.LENGTH_SHORT
                    ).apply {
                        view.layoutParams =
                            (view.layoutParams as FrameLayout.LayoutParams).apply {
                                gravity = Gravity.TOP
                            }
                        setTextColor(Color.WHITE)
                        setBackgroundTint(Color.GREEN)
                    }.show()
                    hasNetworkBeenLost = false
                }
                super.onAvailable(network)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                hasNetworkBeenLost = true
                Snackbar.make(
                    mainView,
                    getString(R.string.network_is_lost),
                    Snackbar.LENGTH_SHORT
                ).apply {
                    view.layoutParams =
                        (view.layoutParams as FrameLayout.LayoutParams).apply {
                            gravity = Gravity.TOP
                        }
                    setTextColor(Color.WHITE)
                    setBackgroundTint(Color.RED)
                }.show()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (connectivityManager.activeNetwork == null) {
            hasNetworkBeenLost = true
            Snackbar.make(
                mainView,
                getString(R.string.network_is_unavailable),
                Snackbar.LENGTH_SHORT
            ).apply {
                view.layoutParams =
                    (view.layoutParams as FrameLayout.LayoutParams).apply { gravity = Gravity.TOP }
                setTextColor(Color.WHITE)
                setBackgroundTint(Color.RED)
            }.show()
        }
    }

    override fun onStart() {
        super.onStart()
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onStop() {
        super.onStop()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}