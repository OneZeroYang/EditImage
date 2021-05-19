package com.zero_code.libEdImage.core

import android.graphics.Path
import com.zero_code.libEdImage.view.ArrowsCoordinate
import com.zero_code.libEdImage.view.PathArrows

/**
 * 箭头封装类
 * @author ZeroCode
 * @date 2021/5/17 : 9:48
 */
object EditImageArrows {
    var pathList: ArrayList<PathArrows> = arrayListOf()

    var path: Path = Path()


    fun addArrows(x: Float, y: Float, color: Int, size: Int) {
        pathList.add(
            PathArrows(
                ArrowsCoordinate(x, y),
                ArrowsCoordinate(x, y),
                color,
                size
            )
        )
    }

    fun upDateArrows(x: Float, y: Float) {
        val orNull = pathList.getOrNull(
            pathList.size - 1)
        if (orNull != null) {
            orNull.end = ArrowsCoordinate(x, y)
        }
    }

    fun deleteArrows() {
        if (!pathList.isNullOrEmpty() && pathList.size > 0) {
            pathList.removeAt(
                pathList.size - 1)
        }
    }

    fun init() {
        pathList = arrayListOf()
        path = Path()
    }
}