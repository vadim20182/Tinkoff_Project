package android.example.hw1

import android.Manifest
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MainActivity2 : AppCompatActivity() {

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }
    private val PERMISSIONS_REQUEST_READ_CONTACTS = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        findViewById<Button>(R.id.button2).setOnClickListener {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS
                )
            else
                startService(Intent(this, MyService::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager.registerReceiver(
            broadcastReceiver,
            IntentFilter(MyService.BROADCAST_ACTION)
        )
    }

    override fun onStop() {
        val localBroadCastManager = LocalBroadcastManager.getInstance(this)
        localBroadCastManager.unregisterReceiver(broadcastReceiver)
        stopService(Intent(this, MyService::class.java))
        super.onStop()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
                )
                    startService(Intent(this, MyService::class.java))
                else
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

}

