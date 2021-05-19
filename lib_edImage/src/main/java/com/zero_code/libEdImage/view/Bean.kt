package com.zero_code.libEdImage.view




/**
 * 编辑页面 底部 工具局域栏实体
 */
data class EditImageToolsBean(
    val res:IntArray = IntArray(2),                          //图片资源  0  是选中了  1是未选中
    var isSelected:Boolean=false                                   //是否选中
){
    fun getImage():Int{
        return if (isSelected) res[0] else res[1]
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EditImageToolsBean

        if (!res.contentEquals(other.res)) return false
        if (isSelected != other.isSelected) return false

        return true
    }

    override fun hashCode(): Int {
        var result = res.contentHashCode()
        result = 31 * result + isSelected.hashCode()
        return result
    }
}




