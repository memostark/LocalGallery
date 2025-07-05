package com.guillermonegrete.gallery.common.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.widget.NestedScrollView
import com.guillermonegrete.gallery.R
import androidx.core.content.withStyledAttributes


class MaxHeightNestedScrollView @JvmOverloads constructor(
    context: Context,
     attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {
    var maxHeight: Int = -1

    init {
        init(context, attrs, defStyleAttr) // Modified changes
    }

    // Modified changes
    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        context.withStyledAttributes(
            attrs, R.styleable.MaxHeightNestedScrollView, defStyleAttr, 0
        ) {
            maxHeight =
                getDimensionPixelSize(R.styleable.MaxHeightNestedScrollView_maxHeight, 0)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (maxHeight > 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}