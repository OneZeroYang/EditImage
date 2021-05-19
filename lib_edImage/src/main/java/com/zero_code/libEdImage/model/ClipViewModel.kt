package com.zero_code.libEdImage.model

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.zero_code.libEdImage.EditImageView
import com.zero_code.libEdImage.databinding.ActivityEditImageBinding

/**
 * 剪裁得ViewModel
 * @author ZeroCode
 * @date 2021/5/13 : 10:21
 */
class ClipViewModel(
    lifecycle: Lifecycle,
    var image: EditImageView?,
    var binding: ActivityEditImageBinding?
) : LifecycleObserver {

    private var rotate=0f
    private var isVerticalMirror=false
    private var isHorizontalMirror=false


    init {
        lifecycle.addObserver(this)
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        image = null
        binding = null
    }


    /**
     * left 旋转
     */
    fun rotateLeft(view: View) {
        rotate+=-90
        image?.doRotate(-90f)
    }

    /**
     * Right 旋转
     */
    fun rotateRight(view: View) {
        rotate+=90
        image?.doRotate(90f)
    }

    /**
     * 向上镜面反转
     */
    fun mirrorUp(view: View) {
        image?.doVerticalMirror()
        isVerticalMirror=!isVerticalMirror
    }


    /**
     * 向左镜像反转
     */
    fun mirrorLeft(view: View) {
        image?.doHorizontalMirror()
        isHorizontalMirror=!isHorizontalMirror
    }

    /**
     * 关闭
     */
    fun close(view: View) {
        binding?.edTools?.visibility = View.VISIBLE
        binding?.clipView?.visibility = View.GONE
        image?.cancelClip()
    }

    /**
     * 重置
     * TODO 在多次进行旋转 翻转后，重置有一定几率不能恢复至初始状态
     */
    fun restore(view: View) {
        if (isHorizontalMirror) image?.doHorizontalMirror()
        if (isVerticalMirror) image?.doVerticalMirror()
        isHorizontalMirror=false
        isVerticalMirror=false
        image?.doRotate(-rotate)
        rotate=0f
        image?.resetClip()
    }

    /**
     * 确认
     */
    fun ok(view: View) {
        binding?.edTools?.visibility = View.VISIBLE
        binding?.clipView?.visibility = View.GONE
        isHorizontalMirror=false
        isVerticalMirror=false
        rotate=0f
        image?.doClip()
    }


}