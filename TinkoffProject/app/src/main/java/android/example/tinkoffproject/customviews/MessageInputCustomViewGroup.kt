package android.example.tinkoffproject.customviews

import android.content.Context
import android.example.tinkoffproject.R
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.widget.doAfterTextChanged

class MessageInputCustomViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var tempBounds = Rect()
    private val backgroundRect = RectF()
    private var sendTextBitmap: RoundedBitmapDrawable? = null
    private var sendOtherBitmap: RoundedBitmapDrawable? = null
    private val innerPadding: Int
    private var sendButtonSize: Int = 0
    private val messageTextInput: EditText
    private var currentTextHeight: Int
    private var currentTextIsEmpty: Boolean
    private val sendImageView: ImageView
    private var contentHeight: Int = 0

    init {
        inflate(context, R.layout.message_input_custom_view_group_layout, this)
        messageTextInput = getChildAt(1) as EditText
        sendImageView = getChildAt(0) as ImageView
        currentTextHeight = messageTextInput.height
        currentTextIsEmpty = messageTextInput.text.isEmpty()
        messageTextInput.doAfterTextChanged {
            if (currentTextHeight != messageTextInput.height) {
                currentTextHeight = messageTextInput.height
                requestLayout()
            }
            if (currentTextIsEmpty != messageTextInput.text.isEmpty()) {
                currentTextIsEmpty = messageTextInput.text.isEmpty()
                invalidate()
            }

        }
        with(context.obtainStyledAttributes(attrs, R.styleable.MessageInputCustomViewGroup))
        {
            innerPadding =
                this.getDimension(
                    R.styleable.MessageInputCustomViewGroup_messageInputCustomViewGroupInnerPadding,
                    10f
                ).toInt()

            val text = "😁 0"
            val textPaint = TextPaint().apply {
                isAntiAlias = true
            }
            textPaint.getTextBounds(text, 0, text.length, tempBounds)
            this.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        if (sendButtonSize == 0) {
            measureChildWithMargins(
                messageTextInput,
                MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(widthMeasureSpec) - innerPadding * 2,
                    MeasureSpec.EXACTLY
                ),
                innerPadding * 3,
                heightMeasureSpec,
                innerPadding * 4
            )
            sendButtonSize = messageTextInput.measuredHeight + innerPadding
            sendOtherBitmap =
                RoundedBitmapDrawableFactory.create(
                    resources,
                    Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(
                            resources,
                            R.mipmap.add_btn
                        ), sendButtonSize, sendButtonSize, false
                    )
                ).apply {
                    isCircular = true
                }

            sendTextBitmap =
                RoundedBitmapDrawableFactory.create(
                    resources,
                    Bitmap.createScaledBitmap(
                        BitmapFactory.decodeResource(
                            resources,
                            R.mipmap.send_btn
                        ), sendButtonSize, sendButtonSize, false
                    )
                ).apply {
                    isCircular = true
                }
        }

        measureChildWithMargins(
            messageTextInput,
            MeasureSpec.makeMeasureSpec(
                MeasureSpec.getSize(widthMeasureSpec) - innerPadding * 2 - sendButtonSize,
                MeasureSpec.EXACTLY
            ),
            innerPadding * 3,
            heightMeasureSpec,
            innerPadding * 4
        )
        measureChildWithMargins(
            sendImageView,
            widthMeasureSpec,
            innerPadding * 2 + messageTextInput.measuredWidth,
            heightMeasureSpec,
            innerPadding * 4
        )

        contentHeight =
            maxOf(
                sendButtonSize,
                messageTextInput.measuredHeight
            ) + if (messageTextInput.lineCount == 1) innerPadding * 3 else innerPadding * 4

        backgroundRect.left = innerPadding.toFloat()
        backgroundRect.bottom =
            contentHeight - innerPadding.toFloat()
        backgroundRect.top =
            backgroundRect.bottom - innerPadding - messageTextInput.measuredHeight - innerPadding
        backgroundRect.right =
            backgroundRect.left + messageTextInput.measuredWidth + 2 * innerPadding

        setMeasuredDimension(
            resolveSize(MeasureSpec.getSize(widthMeasureSpec), widthMeasureSpec),
            resolveSize(contentHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val sendImageView = getChildAt(0) as ImageView
        val messageTextInput = getChildAt(1) as EditText

        messageTextInput.layout(
            innerPadding * 2,
            contentHeight - 2 * innerPadding - messageTextInput.measuredHeight,
            innerPadding * 2 + messageTextInput.measuredWidth,
            contentHeight - 2 * innerPadding
        )
        sendImageView.layout(
            innerPadding * 2 + messageTextInput.measuredWidth + 2 * innerPadding,
            contentHeight - innerPadding - innerPadding / 2 - sendButtonSize,
            innerPadding * 2 + messageTextInput.measuredWidth + innerPadding + sendButtonSize + innerPadding,
            contentHeight - innerPadding - innerPadding / 2
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (messageTextInput.text.isEmpty())
            sendImageView.setImageDrawable(sendOtherBitmap)
        else
            sendImageView.setImageDrawable(sendTextBitmap)

        canvas.drawRoundRect(backgroundRect, 60f, 60f, Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#2A3136")
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
}