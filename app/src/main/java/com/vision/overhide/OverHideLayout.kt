package com.vision.overhide

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.view.children
import kotlin.math.max

class OverHideLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : ViewGroup(context, attrs) {

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        return MarginLayoutParams(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec) - this.paddingRight - this.paddingLeft
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        var contentWidth = 0
        var maxHeight = 0
        for (child in children) {
            val layoutParams = child.layoutParams as MarginLayoutParams
            val childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, paddingLeft + paddingBottom, layoutParams.width)
            val childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, paddingTop + paddingBottom, layoutParams.height)
            val zeroSizeMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY)
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec)
            val childWidth = child.measuredWidth + layoutParams.leftMargin + layoutParams.rightMargin
            if (childWidth + contentWidth > availableWidth) {
                child.measure(zeroSizeMeasureSpec, zeroSizeMeasureSpec)
            }
            contentWidth += childWidth
            maxHeight = max(maxHeight, child.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin)
        }

        if (layoutParams.height >= 0) {
            setMeasuredDimension(availableWidth, layoutParams.height)
        } else if (
            layoutParams.height == LayoutParams.WRAP_CONTENT
        ) {
            setMeasuredDimension(availableWidth, maxHeight + paddingTop + paddingBottom)
        } else {
            setMeasuredDimension(availableWidth, sizeHeight)
        }

    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        var childTop: Int
        var childBottom: Int
        var childLeft = paddingLeft
        var childRight: Int

        for (child in children) {
            if (child.visibility != GONE) {
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

}
