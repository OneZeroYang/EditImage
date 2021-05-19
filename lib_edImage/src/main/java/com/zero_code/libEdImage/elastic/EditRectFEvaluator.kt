package com.zero_code.libEdImage.elastic

import android.animation.TypeEvaluator
import android.graphics.RectF

/**
 * IMGRectFEvaluator
 * @author ZeroCode
 * @date 2021/5/8 : 13:37
 */
class EditRectFEvaluator : TypeEvaluator<RectF> {
    private var mRect: RectF? = null

    constructor() {}
    constructor(reuseRect: RectF?) {
        mRect = reuseRect
    }

    override fun evaluate(
        fraction: Float,
        startValue: RectF,
        endValue: RectF
    ): RectF {
        val left = startValue.left + (endValue.left - startValue.left) * fraction
        val top = startValue.top + (endValue.top - startValue.top) * fraction
        val right = startValue.right + (endValue.right - startValue.right) * fraction
        val bottom =
            startValue.bottom + (endValue.bottom - startValue.bottom) * fraction
        return if (mRect == null) {
            RectF(left, top, right, bottom)
        } else {
            mRect!![left, top, right] = bottom
            mRect!!
        }
    }
}