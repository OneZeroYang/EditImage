package com.zero_code.libEdImage.elastic

import android.animation.ValueAnimator
import android.graphics.PointF
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * IMGElasticAnimator
 * @author ZeroCode
 * @date 2021/5/8 : 13:38
 */
class EditElasticAnimator() : ValueAnimator() {
    private var mElastic: EditElastic? = null

    constructor(elastic: EditElastic?) : this() {
        setElastic(elastic)
    }

    fun setElastic(elastic: EditElastic?) {
        mElastic = elastic
        requireNotNull(mElastic) { "IMGElastic cannot be null." }
    }

    fun start(x: Float, y: Float) {
        setObjectValues(PointF(x, y), mElastic!!.pivot)
        start()
    }

    init {
        setEvaluator(EditPointFEvaluator())
        interpolator = AccelerateDecelerateInterpolator()
    }
}