package com.zero_code.libEdImage.core

import android.graphics.*

/**
 * IMGPath
 * @author ZeroCode
 * @date 2021/5/8 : 13:35
 */
open class EditImagePath @JvmOverloads constructor(
    var path: Path = Path(),
    mode: EditImageMode = EditImageMode.DOODLE,
    color: Int = Color.RED,
    // 初始化是，这里会导致马赛克的 宽带固定死 ，应该写活哈
    width: Float = BASE_MOSAIC_WIDTH
) {
    var color = Color.RED
    var width =
        BASE_MOSAIC_WIDTH
    var mode = EditImageMode.DOODLE

    fun onDrawDoodle(canvas: Canvas, paint: Paint) {
        if (mode == EditImageMode.DOODLE) {
            paint.color = color
            paint.strokeWidth =
                BASE_DOODLE_WIDTH
            // rewind
            canvas.drawPath(path, paint)
        }
    }

    fun onDrawMosaic(canvas: Canvas, paint: Paint) {
        if (mode == EditImageMode.MOSAIC) {
            paint.strokeWidth = width
            canvas.drawPath(path, paint)
        }
    }

    fun transform(matrix: Matrix?) {
        path.transform(matrix!!)
    }

    companion object {
        const val BASE_DOODLE_WIDTH = 20f
        var BASE_MOSAIC_WIDTH = 72f
    }

    init {
        this.mode = mode
        this.color = color
        this.width = width
        if (mode == EditImageMode.MOSAIC) {
            path.fillType = Path.FillType.EVEN_ODD
        }
    }
}