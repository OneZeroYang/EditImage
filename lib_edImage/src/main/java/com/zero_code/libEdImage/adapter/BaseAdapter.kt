package com.zero_code.libEdImage.adapter

import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import com.zero_code.libEdImage.*

/**
 * 通用Adapter
 * Created by Extends on 2017/3/23.
 */

open class BaseAdapter<T, BD : ViewDataBinding>(@LayoutRes layoutId: Int, dataList: ArrayList<T>, isStandard: Boolean = true) : BaseRecyclerAdapter<T, BaseAdapter.Holder>(),
    ItemTouchHelperAdapter {
    private var layoutId: Int = 0
    private var isStandard: Boolean = true
    private var if_onBind: OnBind<T, BD>? = null
    private var mPresenter: Presenter? = null

    override fun onCreate(parent: ViewGroup?, viewType: Int): Holder {
        return Holder(LayoutInflater.from(parent?.context).inflate(layoutId, parent, false))
    }

    override fun onBind(viewHolder: Holder, realPosition: Int, data: T) {
        if (isStandard) {
            (viewHolder.getBinding() as BD).apply {
                setVariable(BR.item, data)
                setVariable(BR.presenter, getPresenter())
                setVariable(BR.position,realPosition)
            }
        }
        if_onBind?.onBind(viewHolder.getBinding() as BD, realPosition, data)
        //不加这一行，刷新时会闪烁
        //当变量的值更新的时候，binding 对象将在下个更新周期中更新。这样就会有一点时间间隔，
        // 如果你像立刻更新，则可以使用 executePendingBindings 函数。
        viewHolder.getBinding()?.executePendingBindings()
    }

    fun setOnBind(if_onBind: OnBind<T, BD>) {
        this.if_onBind = if_onBind
    }

    fun onBind(l: (itemBingding: BD, position: Int, data: T) -> Unit) {
        setOnBind(object : OnBind<T, BD> {
            override fun onBind(itemBingding: BD, position: Int, data: T) {
                l(itemBingding, position, data)
            }
        })
    }

    fun setPresenter(presenter: Presenter) {
        mPresenter = presenter
    }

    protected fun getPresenter(): Presenter? {
        return mPresenter
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        private val b: ViewDataBinding? by lazy {
            DataBindingUtil.bind<ViewDataBinding>(itemView)
        }

        fun getBinding(): ViewDataBinding? {
            return b
        }
    }

    var onItemMove : ((fromPosition: Int, toPosition: Int)->Unit)? = null

    var onItemDissmiss : ((position: Int)->Unit)? = null

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        Collections.swap(datas,fromPosition,toPosition)
        notifyItemMoved(fromPosition,toPosition)
        onItemMove?.invoke(fromPosition, toPosition)
    }

    override fun onItemDissmiss(position: Int) {
        //移除数据
        datas.removeAt(position)
        notifyItemRemoved(position)
        onItemDissmiss?.invoke(position)
    }

    init {
        this.layoutId = layoutId
        this.isStandard = isStandard
        datas = dataList
    }

    interface OnBind<in T, BD> {
        fun onBind(itemBingding: BD, position: Int, data: T)
    }

    interface Presenter
}
