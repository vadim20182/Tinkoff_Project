package android.example.tinkoffproject.customviews

import android.content.Context
import android.content.res.TypedArray
import android.example.tinkoffproject.R
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class CustomImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val backgroundRectPaint = Paint().apply {
        isAntiAlias = true
        color = Color.parseColor("#2A3136")
    }
    private val tempBounds = Rect()
    private val backgroundRect = RectF()
    private val point = PointF()
    private val addEmojiBitmap: Bitmap

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.CustomImageView)
        val text = "😁 0"
        val textPaint = TextPaint()
        textPaint.textSize =
            typedArray.getDimension(R.styleable.CustomImageView_customImageViewSize, 10f)
        val src = BitmapFactory.decodeResource(resources, R.mipmap.plus_icon)
        textPaint.getTextBounds(text, 0, text.length, tempBounds)
        addEmojiBitmap =
            Bitmap.createScaledBitmap(src, tempBounds.height(), tempBounds.height(), false)
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val imageWidth = tempBounds.width()
        val imageHeight = tempBounds.height()

        val sumWidth = imageWidth + paddingLeft + paddingRight
        val sumHeight = imageHeight + paddingBottom + paddingTop

        val resultWidth = resolveSize(sumWidth, widthMeasureSpec)
        val resultHeight = resolveSize(sumHeight, heightMeasureSpec)

        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        point.x = w / 2f - tempBounds.height() / 2f
        point.y = h / 2f - tempBounds.height() / 2f
        backgroundRect.left =
            point.x - tempBounds.width() / 2f + tempBounds.height() / 2f - paddingLeft
        backgroundRect.top =
            point.y - paddingTop
        backgroundRect.right =
            point.x + tempBounds.width() / 2 + tempBounds.height() / 2 + paddingRight
        backgroundRect.bottom =
            point.y + tempBounds.height() + paddingBottom
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(backgroundRect, 25f, 25f, backgroundRectPaint)
        canvas.drawBitmap(addEmojiBitmap, point.x, point.y, null)
    }
}