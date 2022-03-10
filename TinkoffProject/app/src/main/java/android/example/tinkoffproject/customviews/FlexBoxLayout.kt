package android.example.tinkoffproject.customviews

import android.content.Context
import android.content.res.TypedArray
import android.example.tinkoffproject.R
import android.graphics.*
import android.util.AttributeSet
import android.view.ViewGroup

class FlexBoxLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
    private val nextRowIndexes = mutableListOf<Int>()
    private val innerPadding: Int
    private val lastRect = RectF()

    init {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.FlexBoxLayout)
        innerPadding =
            typedArray.getDimension(R.styleable.FlexBoxLayout_flexBoxInnerPadding, 10f).toInt()
        typedArray.recycle()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.drawRoundRect(lastRect, 25f, 25f, Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#2A3136")
        })
        super.dispatchDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        nextRowIndexes.clear()
        var resWidth = 0
        var currentWidth = 0
        var totalHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            if (currentWidth + child.measuredWidth + innerPadding > MeasureSpec.getSize(
                    widthMeasureSpec
                )
            ) {
                nextRowIndexes.add(i)
                resWidth = MeasureSpec.getSize(widthMeasureSpec)
                totalHeight += child.measuredHeight + innerPadding
                currentWidth = 0
                measureChildWithMargins(
                    child,
                    widthMeasureSpec,
                    currentWidth,
                    heightMeasureSpec,
                    totalHeight
                )
            } else {
                if (resWidth < MeasureSpec.getSize(widthMeasureSpec))
                    resWidth = currentWidth + child.measuredWidth + innerPadding
                measureChildWithMargins(
                    child,
                    widthMeasureSpec,
                    currentWidth,
                    heightMeasureSpec,
                    totalHeight
                )
            }
            currentWidth += child.measuredWidth + innerPadding
            if (i == childCount - 1)
                totalHeight += child.measuredHeight + innerPadding
        }
        setMeasuredDimension(
            resolveSize(resWidth, widthMeasureSpec),
            resolveSize(totalHeight, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var currentTop = 0
        var currentWidth = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (i in nextRowIndexes) {
                currentTop += child.measuredHeight + innerPadding
                currentWidth = 0
                child.layout(
                    currentWidth,
                    currentTop,
                    currentWidth + child.measuredWidth + innerPadding,
                    currentTop + child.measuredHeight + innerPadding
                )
            } else {
                child.layout(
                    currentWidth,
                    currentTop,
                    currentWidth + child.measuredWidth + innerPadding,
                    currentTop + child.measuredHeight + innerPadding
                )
            }
            currentWidth += child.measuredWidth + innerPadding
        }
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