package com.zero_code.libEdImage.sticker

import android.graphics.Canvas
import android.graphics.RectF
import android.view.View

/**
 *
 * @author ZeroCode
 * @date 2021/5/8 : 13:33
 */
interface EditStickerPortrait {
    fun show(): Boolean
    fun remove(): Boolean
    fun dismiss(): Boolean
    val isShowing: Boolean
    val frame: RectF

    //    RectF getAdjustFrame();
    //
    //    RectF getDeleteFrame();
    fun onSticker(canvas: Canvas?)
    fun registerCallback(callback: Callback?)
    fun unregisterCallback(callback: Callback?)
    interface Callback {
        fun <V> onDismiss(stickerView: V) where V : View?, V : EditSticker?
        fun <V> onShowing(stickerView: V) where V : View?, V : EditSticker?
        fun <V> onRemove(stickerView: V): Boolean where V : View?, V : EditSticker?
    }
}