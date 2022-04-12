package android.example.tinkoffproject.chat.ui

import android.content.Context
import android.example.tinkoffproject.R
import android.example.tinkoffproject.chat.model.db.MessageEntity
import android.example.tinkoffproject.chat.model.network.UserMessage
import android.graphics.*
import android.text.TextPaint
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MessageCustomItemDecoration(private val context: Context, var data: List<MessageEntity>) :
    RecyclerView.ItemDecoration() {

    private val textPaint = TextPaint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = context.resources.getDimension(R.dimen.default_decoration_text_size)
        typeface = Typeface.DEFAULT
    }
    private var tempBounds = Rect()
    private var itemHeight: Int = 0
    private val innerPadding: Float = context.resources.getDimension(R.dimen.default_padding)

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        val textDate =
            SimpleDateFormat("dd MMM", Locale.US).format(Date(data[position].date * 1000))
        textPaint.getTextBounds(
            textDate.toString(),
            0,
            textDate.toString().length,
            tempBounds
        )
        itemHeight = tempBounds.height()

        if (position == 0)
            outRect.top = itemHeight + textPaint.descent()
                .toInt() + innerPadding.toInt() / 3 + 5 * innerPadding.toInt() / 3
        else {
            val currentMessage = data[position]
            val previousMessage = data[position - 1]

            val currentDate =
                SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(currentMessage.date * 1000))
            val previousDate =
                SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(previousMessage.date * 1000))

            if (currentDate != previousDate)
                if (currentDate != previousDate)
                    if (currentDate != previousDate)
                        outRect.top = itemHeight + textPaint.descent()
                            .toInt() + innerPadding.toInt() / 3 + 5 * innerPadding.toInt() / 3
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        for (child in parent.children) {
            val position = parent.getChildAdapterPosition(child)
            val currentMessage = data[position]
            val textDate =
                SimpleDateFormat("dd MMM", Locale.US).format(Date(data[position].date * 1000))
            val currentDate =
                SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(currentMessage.date * 1000))


            if (data.isNotEmpty() && (position == 0 || currentDate != SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.US
                ).format(Date(data[position - 1].date * 1000)))
            ) {
                c.drawRoundRect(
                    (c.width - textPaint.measureText(textDate.toString())
                        .toInt()) / 2f - innerPadding,
                    child.top - itemHeight - textPaint.descent() - 2 * innerPadding / 3 - innerPadding,
                    (c.width + textPaint.measureText(textDate.toString())
                        .toInt()) / 2f + innerPadding,
                    child.top.toFloat() - innerPadding / 3 - innerPadding,
                    40f,
                    40f,
                    Paint().apply {
                        isAntiAlias = true
                        color = Color.parseColor("#070707")
                    })
                c.drawText(
                    textDate.toString(),
                    (c.width - textPaint.measureText(textDate.toString()).toInt()) / 2f,
                    child.top - textPaint.descent() - innerPadding / 3 - innerPadding,
                    textPaint
                )
            }
        }

    }
}