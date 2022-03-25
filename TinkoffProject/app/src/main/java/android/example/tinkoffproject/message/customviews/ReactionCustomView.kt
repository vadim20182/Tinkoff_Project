package android.example.tinkoffproject.message.customviews

import android.content.Context
import android.content.res.ColorStateList
import android.example.tinkoffproject.R
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

class ReactionCustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var emoji = ""
    private var reactionCount = NO_REACTION
    private var text = ""
    private val textPaint = TextPaint().apply {
        isAntiAlias = true
    }
    private val backgroundRectPaint = Paint().apply {
        isAntiAlias = true
    }
    private var tempBounds = Rect()
    private val backgroundRect = RectF()
    private val startPoint = PointF()
    private val colorStateList: ColorStateList?
    var isButton: Boolean = false
    var isSimpleEmoji: Boolean = false
    private var addEmojiBitmap: Bitmap? = null

    init {
        with(context.obtainStyledAttributes(attrs, R.styleable.ReactionCustomView))
        {
            setEmoji(
                this.getString(R.styleable.ReactionCustomView_reactionCustomEmoji).orEmpty()
            )
            setReactionCount(
                this.getInt(R.styleable.ReactionCustomView_reactionCustomCount, 0)
            )
            textPaint.textSize =
                this.getDimension(
                    R.styleable.ReactionCustomView_reactionCustomTextSize,
                    DEFAULT_TEXT_SIZE_PX
                )
            textPaint.color = this.getColor(
                R.styleable.ReactionCustomView_reactionCustomTextColor,
                Color.WHITE
            )
            colorStateList = this.getColorStateList(
                R.styleable.ReactionCustomView_reactionCustomBackground
            )
            isButton = this.getBoolean(R.styleable.ReactionCustomView_reactionCustomIsButton, false)
            this.recycle()
        }

        text = "$emoji $reactionCount"
    }

    fun setEmoji(emoji: String) {
        this.emoji = emoji
    }

    fun getEmoji(): String {
        return emoji
    }

    fun setReactionCount(reactionCount: Int) {
        this.reactionCount = reactionCount
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (!isButton && !isSimpleEmoji) {
            text = "$emoji $reactionCount"
            textPaint.textSize = resources.getDimension(R.dimen.default_text_size)
            textPaint.getTextBounds(text, 0, text.length, tempBounds)
        } else if (isSimpleEmoji) {
            text = emoji
            textPaint.getTextBounds(text, 0, text.length, tempBounds)
            backgroundRectPaint.color = Color.TRANSPARENT
        } else if (isButton && addEmojiBitmap == null) {
            val text = "😁 0"
            val src = BitmapFactory.decodeResource(resources, R.drawable.plus_icon)
            textPaint.textSize = resources.getDimension(R.dimen.default_text_size)
            textPaint.getTextBounds(text, 0, text.length, tempBounds)
            addEmojiBitmap =
                Bitmap.createScaledBitmap(src, tempBounds.height(), tempBounds.height(), false)
            backgroundRectPaint.color = Color.parseColor("#2A3136")
        }

        val contentWidth = tempBounds.width()
        val contentHeight = tempBounds.height()

        val sumWidth = contentWidth + paddingLeft + paddingRight
        val sumHeight = contentHeight + paddingBottom + paddingTop

        val resultWidth = resolveSize(sumWidth, widthMeasureSpec)
        val resultHeight = resolveSize(sumHeight, heightMeasureSpec)

        setMeasuredDimension(resultWidth, resultHeight)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (!isButton || isSimpleEmoji) {
            startPoint.x = w / 2f - tempBounds.width() / 2f
            startPoint.y = h / 2f + tempBounds.height() / 2f - textPaint.descent()
            backgroundRect.left = startPoint.x - paddingLeft
            backgroundRect.top =
                startPoint.y - tempBounds.height() + textPaint.descent() - paddingTop
            backgroundRect.right = startPoint.x + tempBounds.width() + paddingRight
            backgroundRect.bottom =
                startPoint.y + textPaint.descent() + paddingBottom
        } else {
            startPoint.x = w / 2f - tempBounds.height() / 2f
            startPoint.y = h / 2f - tempBounds.height() / 2f
            backgroundRect.left =
                startPoint.x - tempBounds.width() / 2f + tempBounds.height() / 2f - paddingLeft
            backgroundRect.top =
                startPoint.y - paddingTop
            backgroundRect.right =
                startPoint.x + tempBounds.width() / 2 + tempBounds.height() / 2 + paddingRight
            backgroundRect.bottom =
                startPoint.y + tempBounds.height() + paddingBottom
        }
    }

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + SUPPORTED_DRAWABLE_STATE.size)
        if (!isButton && !isSimpleEmoji) {
            if (isSelected) {
                mergeDrawableStates(drawableState, SUPPORTED_DRAWABLE_STATE)
            }
            backgroundRectPaint.color = colorStateList!!.getColorForState(drawableState, Color.BLUE)
        }
        return drawableState
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRoundRect(backgroundRect, 25f, 25f, backgroundRectPaint)
        if (!isButton)
            canvas.drawText(text, startPoint.x, startPoint.y, textPaint)
        else
            canvas.drawBitmap(addEmojiBitmap!!, startPoint.x, startPoint.y, null)
    }

    companion object {
        private const val NO_REACTION = -1
        private const val DEFAULT_TEXT_SIZE_PX = 10f
        private val SUPPORTED_DRAWABLE_STATE = intArrayOf(android.R.attr.state_selected)
        val EMOJI_LIST =
            arrayOf(
                "😀",
                "😃",
                "😄",
                "😁",
                "😆",
                "😅",
                "🤣",
                "😂",
                "🙂",
                "🙃",
                "😉",
                "😊",
                "😇",
                "😍",
                "🤩",
                "😘",
                "☺",
                "😜",
                "🤪",
                "😝",
                "🤭",
                "🤫",
                "🤔",
                "🤨",
                "😬",
                "😴",
                "🤮",
                "🤯",
                "🧐",
                "😳",
                "😭",
                "💩",
                "👺",
                "👻"
            )
    }
}