package android.example.tinkoffproject

import android.Manifest.permission.*
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
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

    private val requestPermissionLauncher =
        if (SDK_INT >= Build.VERSION_CODES.R)
            registerForActivityResult(
                RequestReadAllFiles()
            ) {}
        else
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager())
                requestPermissionLauncher.launch(arrayOf(MANAGE_EXTERNAL_STORAGE))
        } else
            requestPermissionLauncher.launch(
                arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE)
            )

//        context.deleteDatabase("Tinkoff_Project.db")
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

private class RequestReadAllFiles :
    ActivityResultContract<Array<String?>, Map<String?, Boolean>>() {
    @RequiresApi(Build.VERSION_CODES.R)
    override fun createIntent(context: Context, input: Array<String?>): Intent {
        return createIntent()
    }

    override fun getSynchronousResult(
        context: Context, input: Array<String?>?
    ): SynchronousResult<Map<String?, Boolean>>? {
        if (input == null || input.isEmpty()) {
            return SynchronousResult(emptyMap())
        }
        val grantState: MutableMap<String?, Boolean> = ArrayMap()
        var allGranted = true
        for (permission in input) {
            val granted = (ContextCompat.checkSelfPermission(
                context,
                permission!!
            )
                    == PackageManager.PERMISSION_GRANTED)
            grantState[permission] = granted
            if (!granted) allGranted = false
        }
        return if (allGranted) {
            SynchronousResult(grantState)
        } else null
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?
    ): Map<String?, Boolean> {
        if (resultCode != Activity.RESULT_OK) return emptyMap()
        if (intent == null) return emptyMap()
        val permissions = intent.getStringArrayExtra(EXTRA_PERMISSIONS)
        val grantResults = intent.getIntArrayExtra(EXTRA_PERMISSION_GRANT_RESULTS)
        if (grantResults == null || permissions == null) return emptyMap()
        val result: MutableMap<String?, Boolean> = HashMap()
        var i = 0
        val size = permissions.size
        while (i < size) {
            result[permissions[i]] = grantResults[i] == PackageManager.PERMISSION_GRANTED
            i++
        }
        return result
    }

    companion object {
        const val EXTRA_PERMISSIONS = "androidx.activity.result.contract.extra.PERMISSIONS"

        const val EXTRA_PERMISSION_GRANT_RESULTS =
            "androidx.activity.result.contract.extra.PERMISSION_GRANT_RESULTS"

        @RequiresApi(Build.VERSION_CODES.R)
        fun createIntent(): Intent {
            return Intent(ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }
    }
}