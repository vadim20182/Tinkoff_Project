package android.example.tinkoffproject.message.customviews

import android.content.Context
import android.example.tinkoffproject.R
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.TextView

class MyMessageCustomViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private val backgroundRect = RectF()
    private val innerPadding: Int
    private val messageInnerPadding: Int
    private var contentWidth: Int = 0
    private var shader = LinearGradient(
        0f,
        0f,
        0f,
        height.toFloat(),
        Color.parseColor("#269688"),
        Color.parseColor("#127b6d"),
        Shader.TileMode.MIRROR
    )

    init {
        inflate(context, R.layout.my_message_custom_view_group_layout, this)
        with(context.obtainStyledAttributes(attrs, R.styleable.MyMessageCustomViewGroup))
        {
            innerPadding =
                this.getDimension(
                    R.styleable.MyMessageCustomViewGroup_myMessageCustomViewGroupInnerPadding,
                    10f
                ).toInt()
            messageInnerPadding = (MESSAGE_PADDING_SCALE_FACTOR * innerPadding).toInt()
            this.recycle()
        }
    }

    fun setShader(isFetched: Boolean) {
        if (isFetched)
            shader = LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                Color.parseColor("#269688"),
                Color.parseColor("#127b6d"),
                Shader.TileMode.MIRROR
            )
        else
            shader = LinearGradient(
                0f,
                0f,
                0f,
                height.toFloat(),
                Color.GRAY,
                Color.GRAY,
                Shader.TileMode.MIRROR
            )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val messageTextView = getChildAt(0) as TextView
        val reactionsLayout = getChildAt(1) as FlexBoxLayout

        contentWidth = MeasureSpec.getSize(widthMeasureSpec)

        measureChildWithMargins(
            messageTextView,
            widthMeasureSpec,
            2 * messageInnerPadding,
            heightMeasureSpec,
            messageInnerPadding
        )

        measureChildWithMargins(
            reactionsLayout,
            widthMeasureSpec,
            0,
            heightMeasureSpec,
            messageInnerPadding +
                    messageTextView.measuredHeight + innerPadding + messageInnerPadding
        )


        val contentHeight =
            messageTextView.measuredHeight +
                    reactionsLayout.measuredHeight + innerPadding + 2 * messageInnerPadding

        backgroundRect.left =
            contentWidth - messageInnerPadding - messageTextView.measuredWidth - messageInnerPadding.toFloat()
        backgroundRect.top = 0f
        backgroundRect.right =
            contentWidth.toFloat()
        backgroundRect.bottom =
            messageInnerPadding + (messageTextView.measuredHeight).toFloat() + messageInnerPadding

        setMeasuredDimension(
            resolveSize(contentWidth, widthMeasureSpec),
            resolveSize(contentHeight, heightMeasureSpec)
        )
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val messageTextView = getChildAt(0) as TextView
        val reactionsLayout = getChildAt(1) as FlexBoxLayout

        messageTextView.layout(
            contentWidth - messageInnerPadding - messageTextView.measuredWidth,
            messageInnerPadding,
            contentWidth - messageInnerPadding,
            messageInnerPadding + messageTextView.measuredHeight
        )
        reactionsLayout.layout(
            contentWidth - reactionsLayout.measuredWidth,
            messageInnerPadding +
                    messageTextView.measuredHeight + messageInnerPadding + innerPadding,
            contentWidth,
            messageInnerPadding +
                    messageTextView.measuredHeight + messageInnerPadding + innerPadding + reactionsLayout.measuredHeight
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawRoundRect(backgroundRect, 40f, 40f, Paint().apply {
            isAntiAlias = true
            shader = this@MyMessageCustomViewGroup.shader
        })
        super.dispatchDraw(canvas)
    }


    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun checkLayoutParams(p: LayoutParams): Boolean {
        return p is MarginLayoutParams
    }

    override fun generateLayoutParams(p: LayoutParams): LayoutParams {
        return MarginLayoutParams(p)
    }

    companion object {
        private const val MESSAGE_PADDING_SCALE_FACTOR = 6 / 5f
    }
}