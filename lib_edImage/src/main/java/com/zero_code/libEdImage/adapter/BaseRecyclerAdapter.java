package com.zero_code.libEdImage.adapter;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import androidx.annotation.IntDef;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * * Created by Extends on 2016/3/28 0028.
 */
public abstract class BaseRecyclerAdapter<T,VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    private static final int TYPE_HEADER = 0;//头部
    private static final int TYPE_NORMAL = 1;//身部
    private static final int TYPE_FOOTER = 2;//底部
    private static final int TYPE_EMPTY  = 3;//空页面

    private List<T> mDatas = new ArrayList<>();

    private View mHeaderView;   //头部View
    private View mFooterView;   //底部View
    private View mEmptyView;    //空View

    private OnItemClickListener<T> mListener;

    /**
     * Use with {@link #openLoadAnimation}
     */
    public static final int ALPHAIN = 0x00000001;
    public static final int SCALEIN = 0x00000002;
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    public static final int SLIDEIN_LEFT = 0x00000004;
    public static final int SLIDEIN_RIGHT = 0x00000005;
    public static final int SLIDEIN_TOP = 0x00000006;

    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT,SLIDEIN_TOP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {}

    private boolean mFirstOnlyEnable = false;
    private boolean mOpenAnimationEnable = false;
    private TimeInterpolator mInterpolator = new LinearInterpolator();
    private int mDuration = 300;
    private int mLastPosition = -1;

//    private BaseAnimation mCustomAnimation;
    private BaseAnimation[] mSelectAnimation = {new AlphaInAnimation()};

    public void setOnItemClickListener(OnItemClickListener<T> li) {
        mListener = li;
    }

    /**
     * 设置头部控件
     * @param headerView
     */
    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    /**
     * 设置底部控件
     * @param footerView
     */
    public void setFooterView(View footerView) {
        mFooterView = footerView;
        notifyItemInserted(0);
    }

    /**
     * 设置空布局
     * @param emptyView
     */
    public void setEmptyView(View emptyView){
        mEmptyView = emptyView;
        notifyItemChanged(0);
    }

    /**
     * 获取头部控件
     * @return
     */
    public View getHeaderView() {
        return mHeaderView;
    }

    /**
     * 获取底部控件
     * @return
     */
    public View getFooterView() {
        return mFooterView;
    }

    /**
     * 添加数据
     * @param datas
     */
    public void setDatas(List<T> datas) {
        mDatas = datas;
        notifyDataSetChanged();
    }

    public void addData(T data){
        mDatas.add(data);
        notifyDataSetChanged();
    }

    public void addAllData(List<T> datas){
        int startPoint = mDatas.size();
        mDatas.addAll(datas);
        notifyItemRangeChanged(startPoint == 0 ? startPoint : (startPoint-1),datas.size());
    }

    public void clearData(){
        mDatas.clear();
        notifyDataSetChanged();
    }

    public void clearAddAllData(List<T> datas){
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }
    public void notifyItemRemove(int position){
        mDatas.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position,mDatas.size()-position);
//        notifyDataSetChanged();
    }

    /**
     * 获取数据
     */
    public List<T> getDatas() {
        return mDatas;
    }

    /**
     * 获取Item类型
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        if(mHeaderView != null && position==0){
            return TYPE_HEADER;
        }
        if(mFooterView != null && position == getItemCount()-1){
            return TYPE_FOOTER;
        }
        if(mEmptyView != null && mDatas.size()==0){
            return TYPE_EMPTY;
        }

        return TYPE_NORMAL;
    }

    /**
     * 创建ViewHolder
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public VH onCreateViewHolder(ViewGroup parent, final int viewType) {
        if(mHeaderView != null && viewType == TYPE_HEADER) {
            return (VH) new Holder(mHeaderView);
        }
        if(mFooterView != null && viewType == TYPE_FOOTER) {
            return (VH) new Holder(mFooterView);
        }
        if(mEmptyView != null && viewType == TYPE_EMPTY) {
            return (VH) new Holder(mEmptyView);
        }
        return onCreate(parent, viewType);
    }

    /**
     * 绑定ViewHolder
     * @param viewHolder
     * @param position
     */
    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if(getItemViewType(position) != TYPE_NORMAL) {
            return;
        }

        final int pos = getRealPosition(viewHolder);
        final T data = mDatas.get(pos);

        onBind(viewHolder, pos, data);

        if(mListener != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onItemClick(pos, data);
                }
            });
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if(manager instanceof GridLayoutManager) {
            final GridLayoutManager gridManager = ((GridLayoutManager) manager);
            gridManager.setSpanSizeLookup(   new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if((getItemViewType(position) == TYPE_FOOTER)
                            || (getItemViewType(position) == TYPE_HEADER)
                            || (getItemViewType(position) == TYPE_EMPTY)
                            ) {
                        return gridManager.getSpanCount();
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(VH holder) {
        super.onViewAttachedToWindow(holder);
        int type = holder.getItemViewType();
        if (type == TYPE_HEADER || type == TYPE_FOOTER || type == TYPE_EMPTY) {
            setFullSpan(holder);
        } else {
            addAnimation(holder);
        }
    }

    protected void setFullSpan(RecyclerView.ViewHolder holder) {
        if (holder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams params = (StaggeredGridLayoutManager.LayoutParams) holder
                    .itemView.getLayoutParams();
            params.setFullSpan(true);
        }
    }

    /**
     * 添加动画
     * @param holder
     */
    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            int position = holder.getLayoutPosition();
            if (!mFirstOnlyEnable || position > mLastPosition) {
                int i = position%mSelectAnimation.length;
                for (Animator anim : mSelectAnimation[i].getAnimators(holder.itemView)) {
                    startAnim(anim);
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }


    /**
     * 设置动画类型
     *
     * @param animationType One of {@link #ALPHAIN}, {@link #SCALEIN}, {@link #SLIDEIN_BOTTOM},
     *                      {@link #SLIDEIN_LEFT}, {@link #SLIDEIN_RIGHT}.
     */
    public void openLoadAnimation(@AnimationType int animationType) {
        BaseAnimation ba = null;
        switch (animationType) {
            case ALPHAIN:
                ba = new AlphaInAnimation();
                break;
            case SCALEIN:
                ba = new ScaleInAnimation();
                break;
            case SLIDEIN_TOP:
                ba = new SlideInTopAnimation();
                break;
            case SLIDEIN_BOTTOM:
                ba = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                ba = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                ba = new SlideInRightAnimation();
                break;
            default:break;
        }
        openLoadAnimation(ba);
    }

    /**
     * 设置自定义的动画
     *
     * @param animation ObjectAnimator
     */
    public void openLoadAnimation(BaseAnimation... animation) {
        this.mOpenAnimationEnable = true;
        this.mSelectAnimation = animation;
    }

    /**
     * 设置动画和插值器
     * @param animation
     * @param interpolator
     */
    public void openLoadAnimation(BaseAnimation[] animation, TimeInterpolator interpolator){
        openLoadAnimation(animation);
        setInterpolator(interpolator);
    }

    /**
     * 设置动画和插值器
     * @param animation
     * @param interpolator
     */
    public void openLoadAnimation(BaseAnimation[] animation, TimeInterpolator interpolator, int time){
        openLoadAnimation(animation);
        setInterpolator(interpolator);
        this.mDuration = time;
    }

    /**
     * 设置插值器
     * @param interpolator
     * * AccelerateDecelerateInterpolator   在动画开始与介绍的地方速率改变比较慢，在中间的时候加速
     * AccelerateInterpolator                     在动画开始的地方速率改变比较慢，然后开始加速
     * AnticipateInterpolator                      开始的时候向后然后向前甩
     * AnticipateOvershootInterpolator     开始的时候向后然后向前甩一定值后返回最后的值
     * BounceInterpolator                          动画结束的时候弹起
     * CycleInterpolator                             动画循环播放特定的次数，速率改变沿着正弦曲线
     * DecelerateInterpolator                    在动画开始的地方快然后慢
     * LinearInterpolator                            以常量速率改变
     * OvershootInterpolator                      向前甩一定值后再回到原来位置
     */
    public void setInterpolator(TimeInterpolator interpolator){
        this.mInterpolator = interpolator;
    }



    /**
     * 开启动画，默认 开启
     */
    public void openLoadAnimation() {
        this.mOpenAnimationEnable = true;
    }
    /**
     * 关闭动画
     */
    public void closeLoadAnimation() {
        this.mOpenAnimationEnable = false;
    }

    /**
     * 设置是否只有第一次显示动画，
     * true：只有第一次显示动画
     * false：每次都显示动画
     * 默认是每次都显示动画
     */
    public void onlyFirstAnimationEnable(boolean isFirst){
        this.mFirstOnlyEnable = isFirst;
    }

    /**
     * 获取真实的position
     * @param holder
     * @return
     */
    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获得Item数量
     * @return
     */
    @Override
    public int getItemCount() {
        int count = mDatas.size();
        if(mHeaderView!=null) {
            count++;
        }
        if(mFooterView!=null) {
            count++;
        }
        if(mEmptyView!=null){
            if(mDatas.size()==0){
                count++;
            }
        }
        return count;
    }

    /**
     * 获得Item数量
     * @return
     */
    public int getRealItemCount() {
        return mDatas.size();
    }


    /**
     * 开启动画
     * @param anim
     */
    protected void startAnim(Animator anim) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            anim.setInterpolator(mInterpolator);
            anim.setDuration(mDuration).start();
        }

    }

    public abstract VH onCreate(ViewGroup parent, final int viewType);
    public abstract void onBind(VH viewHolder, int realPosition, T data);

    public class Holder<VH> extends RecyclerView.ViewHolder {
        public Holder(View itemView) {
            super(itemView);
        }
    }

    public interface OnItemClickListener<T> {
        void onItemClick(int position, T data);
    }

}