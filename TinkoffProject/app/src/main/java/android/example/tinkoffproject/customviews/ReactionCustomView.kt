package android.example.tinkoffproject.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.example.tinkoffproject.R
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import kotlin.random.Random

class ReactionCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var emoji = ""
    private var reactionCount = -1
    private var text = ""

    fun setEmoji(emoji: String) {
        this.emoji = emoji
    }

    fun setReactionCount(reactionCount: Int) {
        this.reactionCount = reactionCount
    }


    private val textPaint = TextPaint().apply {
        isAntiAlias = true
    }
    private val backgroundRectPaint = Paint().apply {
        isAntiAlias = true
    }
    private val tempBounds = Rect()
    private val backgroundRect = RectF()
    private val tempTextPoint = PointF()
    private val colorStateList: ColorStateList

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.ReactionCustomView)
        setEmoji(
            typedArray.getString(R.styleable.ReactionCustomView_reactionCustomEmoji).orEmpty()
        )
        setReactionCount(
            typedArray.getString(R.styleable.ReactionCustomView_reactionCustomCount)!!.toInt()
        )
        text = "$emoji $reactionCount"
        textPaint.textSize =
            typedArray.getDimension(R.styleable.ReactionCustomView_reactionCustomTextSize, 10f)
        textPaint.color = typedArray.getColor(
            R.styleable.ReactionCustomView_reactionCustomTextColor,
            Color.WHITE
        )
        colorStateList = typedArray.getColorStateList(
            R.styleable.ReactionCustomView_reactionCustomBackground
        )!!
        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        text = "$emoji $reactionCount"
        textPaint.getTextBounds(text, 0, text.length, tempBounds)

        val textWidth = tempBounds.width()
        val textHeight = tempBounds.height()

        val sumWidth = textWidth + paddingLeft + paddingRight
        val sumHeight = textHeight + paddingBottom + paddingTop

        val resultWidth = resolveSize(sumWidth, widthMeasureSpec)
        val resultHeight = resolveSize(sumHeight, heightMeasureSpec)

        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        tempTextPoint.x = w / 2f - tempBounds.width() / 2f
        tempTextPoint.y = h / 2f + tempBounds.height() / 2f - textPaint.descent()
        backgroundRect.left = tempTextPoint.x - paddingLeft
        backgroundRect.top =
            tempTextPoint.y - tempBounds.height() + textPaint.descent() - paddingTop
        backgroundRect.right = tempTextPoint.x + tempBounds.width() + paddingRight
        backgroundRect.bottom =
            tempTextPoint.y + textPaint.descent() + paddingBottom
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + SUPPORTED_DRAWABLE_STATE.size)
        if (isSelected) {
            reactionCount = Random.nextInt(1000)
            mergeDrawableStates(drawableState, SUPPORTED_DRAWABLE_STATE)
        } else reactionCount = 0
        backgroundRectPaint.color = colorStateList.getColorForState(drawableState, Color.BLUE)
        requestLayout()
        return drawableState
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(backgroundRect, 25f, 25f, backgroundRectPaint)
        canvas.drawText(text, tempTextPoint.x, tempTextPoint.y, textPaint)
    }

    companion object {
        private val SUPPORTED_DRAWABLE_STATE = intArrayOf(android.R.attr.state_selected)
        val EMOJIS = arrayOf("😁", "😆", "🤣", "🙃", "😍")
    }
}