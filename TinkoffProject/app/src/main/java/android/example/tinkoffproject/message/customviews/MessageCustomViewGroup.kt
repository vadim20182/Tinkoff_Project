package android.example.tinkoffproject.message.customviews

import android.content.Context
import android.example.tinkoffproject.R
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.imageview.ShapeableImageView


class MessageCustomViewGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    private var userAvatarId: Int = R.mipmap.avatar
    private val backgroundRect = RectF()
    private var avatarSize: Int
    private var avatarBitmap: Bitmap? = null
    private val innerPadding: Int
    private val messageInnerPadding: Int


    init {
        inflate(context, R.layout.message_custom_view_group_layout, this)
        with(context.obtainStyledAttributes(attrs, R.styleable.MessageCustomViewGroup))
        {
            innerPadding =
                this.getDimension(
                    R.styleable.MessageCustomViewGroup_messageCustomViewGroupInnerPadding,
                    10f
                ).toInt()
            messageInnerPadding = (MESSAGE_PADDING_SCALE_FACTOR * innerPadding).toInt()
            avatarSize =
                this.getDimension(
                    R.styleable.MessageCustomViewGroup_avatarSize,
                    DEFAULT_AVATAR_SIZE
                ).toInt()
            val src = BitmapFactory.decodeResource(resources, userAvatarId)
            avatarBitmap = Bitmap.createScaledBitmap(src, avatarSize, avatarSize, false)
            this.recycle()
        }
    }

    fun setAvatarId(avatarId: Int) {
        this.userAvatarId = avatarId
        val src = BitmapFactory.decodeResource(resources, userAvatarId)
        avatarBitmap = Bitmap.createScaledBitmap(src, avatarSize, avatarSize, false)
        requestLayout()
    }

    fun setAvatar(avatar: Drawable) {
        avatarBitmap = Bitmap.createScaledBitmap(avatar.toBitmap(), avatarSize, avatarSize, false)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val avatarView = getChildAt(0) as ShapeableImageView
        val nameTextView = getChildAt(1) as TextView
        val messageTextView = getChildAt(2) as TextView
        val reactionsLayout = getChildAt(3) as FlexBoxLayout
        val messageOffset = messageInnerPadding * 5

        avatarView.setImageBitmap(avatarBitmap)

        measureChildWithMargins(
            nameTextView,
            widthMeasureSpec,
            avatarSize + innerPadding + messageInnerPadding,
            heightMeasureSpec,
            messageInnerPadding
        )
        measureChildWithMargins(
            messageTextView,
            widthMeasureSpec,
            avatarSize + innerPadding + messageInnerPadding + messageOffset,
            heightMeasureSpec,
            messageInnerPadding + nameTextView.measuredHeight + innerPadding
        )

        measureChildWithMargins(
            reactionsLayout,
            widthMeasureSpec,
            avatarSize + innerPadding,
            heightMeasureSpec,
            messageInnerPadding + nameTextView.measuredHeight + innerPadding +
                    messageTextView.measuredHeight + messageInnerPadding + innerPadding
        )

        val contentWidth = avatarSize + innerPadding + messageInnerPadding + maxOf(
            nameTextView.measuredWidth,
            messageTextView.measuredWidth,
            reactionsLayout.measuredWidth
        )
        val contentHeight =
            messageInnerPadding + nameTextView.measuredHeight + innerPadding + messageTextView.measuredHeight +
                    messageInnerPadding + innerPadding + reactionsLayout.measuredHeight

        backgroundRect.left = avatarSize.toFloat() + innerPadding
        backgroundRect.top = 0f
        backgroundRect.right =
            avatarSize + innerPadding + messageInnerPadding + maxOf(
                nameTextView.measuredWidth,
                messageTextView.measuredWidth
            ).toFloat() + messageInnerPadding
        backgroundRect.bottom =
            (nameTextView.measuredHeight + messageTextView.measuredHeight).toFloat() + innerPadding + 2 * messageInnerPadding

        setMeasuredDimension(
            resolveSize(contentWidth, widthMeasureSpec) + messageInnerPadding,
            resolveSize(contentHeight, heightMeasureSpec)
        )
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val avatarView = getChildAt(0) as ShapeableImageView
        val nameTextView = getChildAt(1) as TextView
        val messageTextView = getChildAt(2) as TextView
        val reactionsLayout = getChildAt(3) as FlexBoxLayout

        avatarView.layout(
            0,
            0,
            avatarSize,
            avatarSize
        )

        nameTextView.layout(
            avatarSize + innerPadding + messageInnerPadding,
            messageInnerPadding,
            avatarSize + innerPadding + messageInnerPadding + nameTextView.measuredWidth,
            messageInnerPadding + nameTextView.measuredHeight
        )
        messageTextView.layout(
            avatarSize + innerPadding + messageInnerPadding,
            messageInnerPadding + nameTextView.measuredHeight + innerPadding,
            avatarSize + innerPadding + messageInnerPadding + messageTextView.measuredWidth,
            messageInnerPadding + nameTextView.measuredHeight + innerPadding + messageTextView.measuredHeight
        )
        reactionsLayout.layout(
            avatarSize + innerPadding,
            messageInnerPadding + nameTextView.measuredHeight + innerPadding +
                    messageTextView.measuredHeight + messageInnerPadding + innerPadding,
            avatarSize + innerPadding + reactionsLayout.measuredWidth,
            messageInnerPadding + nameTextView.measuredHeight + innerPadding +
                    messageTextView.measuredHeight + messageInnerPadding + innerPadding + reactionsLayout.measuredHeight
        )
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawRoundRect(backgroundRect, 40f, 40f, Paint().apply {
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

    companion object {
        private const val MESSAGE_PADDING_SCALE_FACTOR = 6 / 5f
        private const val DEFAULT_AVATAR_SIZE = 20f
    }
}