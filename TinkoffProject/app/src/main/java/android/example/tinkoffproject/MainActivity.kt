package android.example.tinkoffproject

import android.example.tinkoffproject.customviews.FlexBoxLayout
import android.example.tinkoffproject.customviews.ReactionCustomView
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        for (ch in findViewById<FlexBoxLayout>(R.id.message_emojis).children)
            ch.setOnClickListener { view ->
                view.isSelected = !view.isSelected
            }
    }
}