package com.microsoft.mobile.polymer.mishtu.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView


class BNNestedScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
    defStyle: Int = 0): NestedScrollView(context,attrs,defStyle) {


    private var maxHeight = -1

    fun getMaxHeight(): Int {
        return maxHeight
    }



    fun setMaxHeight(maxHeight: Int) {
        this.maxHeight = maxHeight
    }

    fun setMaxHeightDensity(dps: Int) {
        maxHeight = (dps * context.resources.displayMetrics.density).toInt()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var h = heightMeasureSpec
        if (maxHeight > 0) {
            h = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
        }
        super.onMeasure(widthMeasureSpec, h)
    }
}