package android.example.tinkoffproject.message.customviews

import android.content.Context
import android.example.tinkoffproject.R
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged

class TopicInputCustomViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var tempBounds = Rect()
    private val backgroundRect = RectF()
    private val innerPadding: Int
    private val verticalInnerPadding: Int
    private val topicTextInput: EditText
    private var currentTextHeight: Int
    private var currentTextIsEmpty: Boolean
    private var contentHeight: Int = 0

    init {
        inflate(context, R.layout.topic_input_custom_view_group_layout, this)
        topicTextInput = getChildAt(0) as EditText
        currentTextHeight = topicTextInput.height
        currentTextIsEmpty = topicTextInput.text.isEmpty()
        topicTextInput.doAfterTextChanged {
            if (currentTextHeight != topicTextInput.height) {
                currentTextHeight = topicTextInput.height
                requestLayout()
            }
            if (currentTextIsEmpty != topicTextInput.text.isEmpty()) {
                currentTextIsEmpty = topicTextInput.text.isEmpty()
                invalidate()
            }
        }
        with(context.obtainStyledAttributes(attrs, R.styleable.TopicInputCustomViewGroup))
        {
            innerPadding =
                this.getDimension(
                    R.styleable.TopicInputCustomViewGroup_topicInputCustomViewGroupInnerPadding,
                    10f
                ).toInt()

            verticalInnerPadding = innerPadding / 2

            val text = "😁 0"
            val textPaint = TextPaint().apply {
                isAntiAlias = true
            }
            textPaint.getTextBounds(text, 0, text.length, tempBounds)
            this.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        measureChildWithMargins(
            topicTextInput,
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec) - innerPadding * TEXT_INPUT_PADDING_COUNT,
                MeasureSpec.EXACTLY
            ),
            innerPadding * TOTAL_HORIZONTAL_PADDING_COUNT,
            heightMeasureSpec,
            verticalInnerPadding * VERTICAL_INNER_PADDING_COUNT
        )

        contentHeight =
            topicTextInput.measuredHeight + if (topicTextInput.lineCount == 1) verticalInnerPadding * 3 else verticalInnerPadding * (2 * VERTICAL_INNER_PADDING_COUNT)
        backgroundRect.left = innerPadding.toFloat()
        backgroundRect.bottom =
            contentHeight.toFloat()
        backgroundRect.top =
            backgroundRect.bottom - verticalInnerPadding - topicTextInput.measuredHeight - verticalInnerPadding
        backgroundRect.right =
            backgroundRect.left + topicTextInput.measuredWidth + TEXT_INPUT_PADDING_COUNT * innerPadding

        setMeasuredDimension(
            resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec),
            resolveSize(contentHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val topicTextInput = getChildAt(0) as EditText

        topicTextInput.layout(
            innerPadding * TEXT_INPUT_PADDING_COUNT,
            contentHeight - VERTICAL_INNER_PADDING_COUNT * verticalInnerPadding - topicTextInput.measuredHeight,
            innerPadding * TEXT_INPUT_PADDING_COUNT + topicTextInput.measuredWidth,
            contentHeight - verticalInnerPadding
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawRoundRect(backgroundRect, 60f, 60f, Paint().apply {
            isAntiAlias = true
            color = resources.getColor(R.color.default_background_color, null)
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
        private const val TOTAL_HORIZONTAL_PADDING_COUNT = 2
        private const val VERTICAL_INNER_PADDING_COUNT = 1
        private const val TEXT_INPUT_PADDING_COUNT = 2
    }
}