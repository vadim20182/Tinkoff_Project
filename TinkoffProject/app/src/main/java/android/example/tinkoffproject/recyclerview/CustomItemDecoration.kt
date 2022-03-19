package android.example.tinkoffproject.recyclerview

import android.content.Context
import android.example.tinkoffproject.R
import android.example.tinkoffproject.data.UserMessage
import android.graphics.*
import android.text.TextPaint
import android.view.View
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

class CustomItemDecoration(private val context: Context, private val data: List<UserMessage>) :
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
        textPaint.getTextBounds(data[position].date, 0, data[position].date.length, tempBounds)
        itemHeight = tempBounds.height()

        if (position == 0)
            outRect.top = itemHeight + textPaint.descent()
                .toInt() + innerPadding.toInt() / 3 + 5 * innerPadding.toInt() / 3
        else {
            val currentMessage = data[position]
            val previousMessage = data[position - 1]

            if (currentMessage.date != previousMessage.date)
                outRect.top = itemHeight + textPaint.descent()
                    .toInt() + innerPadding.toInt() / 3 + 5 * innerPadding.toInt() / 3
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        for (child in parent.children) {
            val position = parent.getChildAdapterPosition(child)
            val currentMessage = data[position]

            if (data.isNotEmpty() && (position == 0 || currentMessage.date != data[position - 1].date)) {
                c.drawRoundRect(
                    (c.width - textPaint.measureText(data[position].date)
                        .toInt()) / 2f - innerPadding,
                    child.top - itemHeight - textPaint.descent() - 2 * innerPadding / 3 - innerPadding,
                    (c.width + textPaint.measureText(data[position].date)
                        .toInt()) / 2f + innerPadding,
                    child.top.toFloat() - innerPadding / 3 - innerPadding,
                    40f,
                    40f,
                    Paint().apply {
                        isAntiAlias = true
                        color = Color.parseColor("#070707")
                    })
                c.drawText(
                    data[position].date,
                    (c.width - textPaint.measureText(data[position].date).toInt()) / 2f,
                    child.top - textPaint.descent() - innerPadding / 3 - innerPadding,
                    textPaint
                )
            }
        }

    }
}