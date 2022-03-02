package android.example.hw1

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)
        val secondActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val res = result.data?.extras?.get("result") as Array<*>
                    if (res.isArrayOf<String>())
                        findViewById<ListView>(R.id.listView).adapter = ArrayAdapter(
                            this,
                            R.layout.list_item,
                            res
                        )
                }
            }
        findViewById<Button>(R.id.button1).setOnClickListener {
            secondActivityResult.launch(Intent(this, MainActivity2::class.java))
        }
    }
}
