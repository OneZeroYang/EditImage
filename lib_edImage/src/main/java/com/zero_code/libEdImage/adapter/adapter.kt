package com.zero_code.libEdImage.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView


/**
 *
 * @author Extends
 * @date 2018/12/19/019
 */
interface ItemTouchHelperAdapter{
    //数据交换
    fun onItemMove(fromPosition:Int,toPosition:Int)
    //数据删除
    fun onItemDissmiss(position:Int)
}

class SimpleItemTouchHelperCallback(val mAdapter: ItemTouchHelperAdapter): ItemTouchHelper.Callback(){

    /**
     * 该方法用于返回可以滑动的方向
     */
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        //允许上下的拖动
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        //只允许从右向左侧滑
        val swipeFlags = ItemTouchHelper.LEFT
        return makeMovementFlags(dragFlags,swipeFlags)
    }

    /**
     * 该方法返回true时，表示支持长按拖动
     */
    override fun isLongPressDragEnabled() = true

    /**
     * 该方法返回true时，表示如果用户触摸并左右滑动了View，那么可以执行滑动删除操作
     */
    override fun isItemViewSwipeEnabled() = true

    /**
     * 当用户拖动一个Item进行上下移动从旧的位置到新的位置的时候会调用该方法，
     * 在该方法内，我们可以调用Adapter的notifyItemMoved方法来交换两个ViewHolder的位置，
     * 最后返回true，表示被拖动的ViewHolder已经移动到了目的位置。
     * 所以，如果要实现拖动交换位置，可以重写该方法
     */
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        mAdapter.onItemMove(viewHolder.adapterPosition,target.adapterPosition)
        return true
    }

    /**
     * 当用户左右滑动Item达到删除条件时，会调用该方法，一般手指触摸滑动的距离达到RecyclerView宽度的一半时，
     * 再松开手指，此时该Item会继续向原先滑动方向滑过去并且调用onSwiped方法进行删除，否则会反向滑回原来的位置
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemDissmiss(viewHolder.adapterPosition)
    }
}

class DragItemTouchHelperCallback(val mAdapter: ItemTouchHelperAdapter): ItemTouchHelper.Callback(){
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlags,0)
    }

    override fun isLongPressDragEnabled() = true
    override fun isItemViewSwipeEnabled() = false
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        mAdapter.onItemMove(viewHolder.adapterPosition,target.adapterPosition)
        return true
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    var onDragEnd : (()->Unit)?=null
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)
        when(actionState){
            ItemTouchHelper.ACTION_STATE_SWIPE -> {
                //开始删除
            }
            ItemTouchHelper.ACTION_STATE_DRAG -> {
                //开始拖动
            }
            ItemTouchHelper.ACTION_STATE_IDLE -> {
                //结束拖动或删除
                onDragEnd?.invoke()
            }
            else -> {}
        }
    }
}

