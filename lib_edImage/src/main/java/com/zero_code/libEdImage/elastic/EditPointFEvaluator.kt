package com.zero_code.libEdImage.elastic

import android.animation.TypeEvaluator
import android.graphics.PointF

/**
 * IMGPointFEvaluator
 * @author ZeroCode
 * @date 2021/5/8 : 13:37
 */
class EditPointFEvaluator : TypeEvaluator<PointF> {
    private var mPoint: PointF? = null

    constructor() {}
    constructor(reuse: PointF?) {
        mPoint = reuse
    }

    override fun evaluate(
        fraction: Float,
        startValue: PointF,
        endValue: PointF
    ): PointF {
        val x = startValue.x + fraction * (endValue.x - startValue.x)
        val y = startValue.y + fraction * (endValue.y - startValue.y)
        return if (mPoint != null) {
            mPoint!![x] = y
            mPoint!!
        } else {
            PointF(x, y)
        }
    }
}