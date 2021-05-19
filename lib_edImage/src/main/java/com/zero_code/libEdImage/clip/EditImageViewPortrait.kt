package com.zero_code.libEdImage.clip

/**
 * IMGViewPortrait
 * @author ZeroCode
 * @date 2021/5/8 : 13:35
 */
interface EditImageViewPortrait {
    val width: Int
    val height: Int
    var scaleX: Float
    var scaleY: Float
    var rotation: Float
    val pivotX: Float
    val pivotY: Float
    var x: Float
    var y: Float
    var scale: Float
    fun addScale(scale: Float)
}