package com.zero_code.libEdImage.adapter

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View

/**
 * Created by Extends on 2018/4/14/014.
 */
interface BaseAnimation {
    fun getAnimators(view: View): Array<Animator>
}

/**
 * 透明度动画
 */
class AlphaInAnimation @JvmOverloads constructor(private val mFrom: Float = DEFAULT_ALPHA_FROM) :
    BaseAnimation {
    override fun getAnimators(view: View): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "alpha", mFrom, 1f))
    }
    companion object {
        private val DEFAULT_ALPHA_FROM = 0f
    }
}

/**
 * 缩放动画
 */
class ScaleInAnimation @JvmOverloads constructor(private val mFrom: Float = DEFAULT_SCALE_FROM) :
    BaseAnimation {
    override fun getAnimators(view: View): Array<Animator> {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", mFrom, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", mFrom, 1f)
        return arrayOf(scaleX, scaleY)
    }
    companion object {
        private val DEFAULT_SCALE_FROM = .5f
    }
}

/**
 * 从下往上的滑入动画
 */
class SlideInBottomAnimation : BaseAnimation {
    override fun getAnimators(view: View): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "translationY", view.measuredHeight.toFloat(), 0f))
    }
}

/**
 * 从上往下的滑入动画
 */
class SlideInTopAnimation : BaseAnimation {
    override fun getAnimators(view: View): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "translationY", -view.measuredHeight.toFloat(), 0f))
    }
}

/**
 * 从左往右的滑入动画
 */
class SlideInLeftAnimation : BaseAnimation {
    override fun getAnimators(view: View): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "translationX", -view.rootView.width.toFloat(), 0f))
    }
}

/**
 * 从右往左的滑入动画
 */
class SlideInRightAnimation : BaseAnimation {
    override fun getAnimators(view: View): Array<Animator> {
        return arrayOf(ObjectAnimator.ofFloat(view, "translationX", view.rootView.width.toFloat(), 0f))
    }
}