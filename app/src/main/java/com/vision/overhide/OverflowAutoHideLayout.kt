package com.vision.overhide

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import kotlin.math.max

class OverflowAutoHideLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {
    private var lastVisibleIndex = -1

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams?): Boolean {
        return p is LayoutParams
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        if (p is LayoutParams) {
            return LayoutParams(p)
        } else if (p is MarginLayoutParams) {
            return LayoutParams(p)
        }
        return LayoutParams(p)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY, MeasureSpec.AT_MOST -> MeasureSpec.getSize(widthMeasureSpec)
            else -> Int.MAX_VALUE
        }
        var consumedWidth = paddingLeft + paddingRight
        val paddingVertical = paddingTop + paddingBottom

        var maxHeight = 0
        var hasOverflowed = false
        children.forEachIndexed { index, child ->
            if (hasOverflowed) {
                return@forEachIndexed
            }
            if (child.visibility == GONE) {
                return@forEachIndexed
            }
            val layoutParams = child.layoutParams as LayoutParams
            measureChildWithMargins(
                child,
                widthMeasureSpec,
                consumedWidth,
                heightMeasureSpec,
                paddingVertical,
            )
            consumedWidth += child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
            if (consumedWidth > availableWidth) {
                hasOverflowed = true
                lastVisibleIndex = index - 1
            } else if (consumedWidth == availableWidth) {
                lastVisibleIndex = index
                hasOverflowed = true
                if (layoutParams.overflowAutoHide) {
                    val childMeasuredWidth = child.measuredWidth
                    child.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), getChildMeasureSpec(heightMeasureSpec, paddingVertical, layoutParams.height))
                    val childIntrinsicWidth = child.measuredWidth
                    if (childIntrinsicWidth > childMeasuredWidth) {
                        lastVisibleIndex = index - 1
                    }
                }
            }
            maxHeight = max(
                maxHeight,
                child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
            )
        }
        if (!hasOverflowed) {
            lastVisibleIndex = childCount
        }

        val width = resolveSize(consumedWidth, widthMeasureSpec)
        val height = resolveSize(maxHeight + paddingVertical, heightMeasureSpec)

        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var childTop: Int
        var childBottom: Int
        var childLeft = paddingLeft
        var childRight: Int

        children.forEachIndexed { index, child ->
            if (child.visibility == GONE) {
                return@forEachIndexed
            }
            if (index > lastVisibleIndex) {
                return@forEachIndexed
            }
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val lp = child.layoutParams as MarginLayoutParams
            childTop = paddingTop + lp.topMargin
            childBottom = childHeight + childTop + lp.bottomMargin + paddingBottom
            childLeft += lp.leftMargin
            childRight = childLeft + lp.rightMargin + childWidth
            child.layout(childLeft, childTop, childRight, childBottom)
            childLeft = childRight
        }
    }

    class LayoutParams : MarginLayoutParams {

        var overflowAutoHide: Boolean = false

        constructor(width: Int, height: Int): super(width, height) {
            overflowAutoHide = false
        }
        constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.OverflowAutoHideLayout_Layout)
            overflowAutoHide = a.getBoolean(R.styleable.OverflowAutoHideLayout_Layout_layout_overflowAutoHide, false)
            a.recycle()
        }
        constructor(width: Int, height: Int, overflowAutoHide: Boolean): super(width, height) {
            this.overflowAutoHide = overflowAutoHide
        }
        constructor(lp: ViewGroup.LayoutParams): super(lp)
        constructor(lp: MarginLayoutParams): super(lp)
    }

}
