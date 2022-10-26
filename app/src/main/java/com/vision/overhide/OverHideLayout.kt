package com.vision.overhide

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import kotlin.math.max

class OverHideLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {
    private var lastVisibleIndex = -1

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
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
            val layoutParams = child.layoutParams as MarginLayoutParams
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

}
