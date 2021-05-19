package com.zero_code.libEdImage.ui.text

import android.graphics.Color
import android.text.TextUtils

/**
 * IMGText
 * @author ZeroCode
 * @date 2021/5/8 : 13:35
 */
class EditImageText(var text: String, color: Int) {
    var color = Color.WHITE

    var backgroundColor:Int=Color.TRANSPARENT

    val isEmpty: Boolean
        get() = TextUtils.isEmpty(text)

    fun length(): Int {
        return if (isEmpty) 0 else text.length
    }

    override fun toString(): String {
        return "IMGText{" +
                "text='" + text + '\'' +
                ", color=" + color +
                '}'
    }

    init {
        this.color = color
    }
}