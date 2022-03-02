package android.example.hw1

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MyService : Service() {

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Thread {
            val localBroadcastManager = LocalBroadcastManager.getInstance(this)
            val namesList = mutableListOf<String>()
            val cursor =
                contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    arrayOf(ContactsContract.Profile.DISPLAY_NAME_PRIMARY),
                    null,
                    null,
                    ContactsContract.Contacts.SORT_KEY_PRIMARY
                )

            if (cursor != null && cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val columnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                    if (columnIndex > -1)
                        namesList.add(cursor.getString(columnIndex))
                }
            }
            cursor?.close()

            localBroadcastManager.sendBroadcast(
                Intent(BROADCAST_ACTION).putExtra(
                    "result",
                    namesList.toTypedArray()
                )
            )
        }.start()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val BROADCAST_ACTION = "MyService"
    }
}