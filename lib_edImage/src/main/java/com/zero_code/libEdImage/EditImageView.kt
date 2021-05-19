package com.zero_code.libEdImage

import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.ScaleGestureDetector.OnScaleGestureListener
import android.widget.FrameLayout
import com.zero_code.libEdImage.anim.EditHomingAnimator
import com.zero_code.libEdImage.core.*
import com.zero_code.libEdImage.homing.EditHoming
import com.zero_code.libEdImage.sticker.EditSticker
import com.zero_code.libEdImage.sticker.EditStickerPortrait
import com.zero_code.libEdImage.ui.text.EditImageText
import com.zero_code.libEdImage.widget.EditStickerTextView
import java.lang.RuntimeException

/**
 * 图片编辑的 核心  自定义View
 * @author ZeroCode
 * @date 2021/5/8 : 13:17
 */
class EditImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), Runnable, OnScaleGestureListener,
    AnimatorUpdateListener, EditStickerPortrait.Callback,
    Animator.AnimatorListener {
    private var mPreMode = EditImageMode.NONE
    private val mImage: EditImage? =
        EditImage()
    private var arrowsColor: Int = Color.RED   //箭头颜色
    private var arrowsSize: Int = 2            //箭头大小
    private var mGDetector: GestureDetector? = null
    private var mSGDetector: ScaleGestureDetector? = null
    private var mHomingAnimator: EditHomingAnimator? = null
    private val mPen = Pen()
    private var mPointerCount = 0
    private val mDoodlePaint =
        Paint(Paint.ANTI_ALIAS_FLAG)
    private val mMosaicPaint =
        Paint(Paint.ANTI_ALIAS_FLAG)

    /**
     * 初始化
     */
    private fun initialize(context: Context) {
        mPen.mode = mImage!!.mode

        mGDetector = GestureDetector(context, MoveAdapter())
        mSGDetector = ScaleGestureDetector(context, this)
    }

//TODO//============================================= 对外api


    /**
     * ************************************  编辑模式选择
    NONE,       //默认
    DOODLE,     //涂鸦
    MOSAIC,     //马赛克
    CLIP,       //剪裁
    ARROWS      //箭头

     * this.mode=EditMode.NONE               //默认模式 该模式下只有放大缩小移动 相当于预览
     * this.mode=EditMode.DOODLE             //涂鸦模式
     * this.mode=EditMode.MOSAIC             //马赛克模式
     * this.mode=EditMode.CLIP               //裁剪模式 包含旋转 镜像反转等
     * this.mode=IMGMode.ARROWS             //箭头
     *



     * ************************************  编辑模式选择
     */


    /**
     * 设置图片
     * @param image 资源
     */
    fun setImageBitmap(image: Bitmap?) {
        mImage!!.setBitmap(image)
        postInvalidate()
    }

    /**
     * 是否真正修正归位
     */
    val isHoming: Boolean
        get() = (mHomingAnimator != null
                && mHomingAnimator!!.isRunning)


    /**
     * 旋转
     * @param rotate 旋转度数 0-360f
     */
    fun doRotate(rotate: Float) {
//        ////方案1
//        mImage!!.rotate(rotate)
//        //可以考虑不重置 剪裁框
//        mImage!!.resetClip()
//        onHoming()
        ///////// 方案2
        val rotateBitmap = mImage!!.getRotateBitmap(rotate)
        setImageBitmap(rotateBitmap)
        resetClip()
    }


    /**
     * 设置箭头颜色
     * @param color 颜色值
     */
    fun setArrowsColor(color: Int) {
        this.arrowsColor = color
    }

    /**设置箭头大小 1 最小  5最大
     *@param size 1-5
     */
    fun setArrowsSize(size: Int) {
        if (size in 1..5) {
            this.arrowsSize = size
        } else {
            throw RuntimeException("设置箭头大小 1 最小  5最大")
        }
    }


    /**
     * 清除上一步 箭头
     */
    fun clearLastArrows() {
        EditImageArrows.deleteArrows()
        postInvalidate()
    }

    /**
     * 水平镜像翻转
     */
    fun doHorizontalMirror() {
        mImage!!.toHorizontalMirror()
        onHoming()
    }


    /**
     * 垂直镜像翻转
     */
    fun doVerticalMirror() {
        mImage!!.toVerticalMirror()
        onHoming()
    }

    /**
     * 清除镜像反转
     */
    fun clearMirror() {
        if (!isHoming) {
            mImage!!.clearMirror()
            onHoming()
        }
    }

    /**
     * 设置马赛克宽度
     *  public static final  Float IMG_MOSAIC_SIZE_1=0.2F;
    public static final  Float IMG_MOSAIC_SIZE_2=0.3F;
    public static final  Float IMG_MOSAIC_SIZE_3=0.4F;
    public static final  Float IMG_MOSAIC_SIZE_4=0.5F;
    public static final  Float IMG_MOSAIC_SIZE_5=0.6F;
     */
    fun setMosaicWidth(float: Float) {
        EditImagePath.BASE_MOSAIC_WIDTH = float * 100
        mPen.width = float * 100
    }

    /**
     * 返回图像
     */
    fun getImage(): EditImage? = mImage


    /**
     * 重新设定裁剪
     */
    fun resetClip() {
        mImage!!.resetClip()
        mImage!!.clearMirror()
        onHoming()
    }


    /**
     * 开始剪裁
     */
    fun doClip() {
        mImage!!.clip(scrollX.toFloat(), scrollY.toFloat())
        mode = EditImageMode.NONE
        onHoming()
    }

    /**
     * 取消剪裁
     */
    fun cancelClip() {
        mImage!!.toBackupClip()
        mode = EditImageMode.NONE
    }

    /**
     * 这只画笔颜色
     * @param color 颜色值
     */
    fun setPenColor(color: Int) {
        mPen.color = color
    }

    /**
     * 涂鸦是否为空
     */
    val isDoodleEmpty: Boolean
        get() = mImage!!.isDoodleEmpty


    /**
     * 撤销上一步 涂鸦
     */
    fun undoDoodle() {
        mImage!!.undoDoodle()
        invalidate()
    }

    /**
     * 马赛克是否为空
     */
    val isMosaicEmpty: Boolean
        get() = mImage!!.isMosaicEmpty


    /**
     * 撤销上一步 马赛克
     */
    fun undoMosaic() {
        mImage!!.undoMosaic()
        invalidate()
    }


    /**
     * 加入自定义扩展 view 如，加入文字 自定义 view
     * @param stickerView 需要加入得View  一般继承于  IMGStickerView
     * @param params LayoutParams
     */
    fun <V> addStickerView(
        stickerView: V?,
        params: LayoutParams?
    ) where V : View?, V : EditSticker? {
        if (stickerView != null) {
            addView(stickerView, params)
            stickerView.registerCallback(this)
            mImage!!.addSticker(stickerView)
        }
    }

    /**
     * 加入文字
     * @param text 文字控件
     */
    fun addStickerText(text: EditImageText?) {
        val textView =
            EditStickerTextView(context)
        textView.text = text
        val layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        // Center of the drawing window.
        layoutParams.gravity = Gravity.CENTER
        textView.x = scrollX.toFloat()
        textView.y = scrollY.toFloat()
        addStickerView(textView, layoutParams)

    }


    /**
     * 保存 编辑好得图片
     */
    fun saveBitmap(): Bitmap {
        mImage!!.stickAll()
        val scale = 1f / mImage.scale
        val frame = RectF(mImage.clipFrame)

        // 旋转基画布
        val m = Matrix()
        m.setRotate(mImage.rotate, frame.centerX(), frame.centerY())
        m.mapRect(frame)

        // 缩放基画布
        m.setScale(scale, scale, frame.left, frame.top)
        m.mapRect(frame)
        val bitmap = Bitmap.createBitmap(
            Math.round(frame.width()),
            Math.round(frame.height()), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)

        // 平移到基画布原点&缩放到原尺寸
        canvas.translate(-frame.left, -frame.top)
        canvas.scale(scale, scale, frame.left, frame.top)
        onDrawImages(canvas)
        return bitmap
    }


    /**
     * 复位
     * 一般外部不是去实现，对外暴露只是为了扩展
     */
    fun onHoming() {
        invalidate()
        stopHoming()
        startHoming(
            mImage!!.getStartHoming(scrollX.toFloat(), scrollY.toFloat()),
            mImage.getEndHoming(scrollX.toFloat(), scrollY.toFloat())
        )
    }

    /**
     * 开始修正
     * 一般外部不是去实现，对外暴露只是为了扩展
     */
    fun startHoming(sHoming: EditHoming, eHoming: EditHoming) {
        if (mHomingAnimator == null) {
            mHomingAnimator = EditHomingAnimator()
            mHomingAnimator!!.addUpdateListener(this)
            mHomingAnimator!!.addListener(this)
        }
        mHomingAnimator!!.setHomingValues(sHoming, eHoming)
//        mHomingAnimator!!.duration = 3000
        mHomingAnimator!!.start()
    }


    /**
     * 完成修正
     * 一般外部不是去实现，对外暴露只是为了扩展
     */
    fun stopHoming() {
        if (mHomingAnimator != null) {
            mHomingAnimator!!.cancel()
        }
    }


    //TODO//============================================= 对外api

    // 保存现在的编辑模式

    // 设置新的编辑模式

    // 矫正区域
    var mode: EditImageMode?
        get() = mImage!!.mode
        set(mode) {
            // 保存现在的编辑模式
            mPreMode = mImage!!.mode

            // 设置新的编辑模式
            mImage.mode = mode!!
            mPen.mode = mode

            // 矫正区域
            onHoming()
        }

    override fun onDraw(canvas: Canvas?) {
        if (canvas != null)
            onDrawImages(canvas)
    }

    private fun onDrawImages(canvas: Canvas?) {
        canvas?.save()

        // clip 中心旋转
        val clipFrame = mImage!!.clipFrame
        canvas?.rotate(mImage.rotate, clipFrame.centerX(), clipFrame.centerY())

        // 图片
        mImage.onDrawImage(canvas!!)


        // 马赛克
        if (!mImage.isMosaicEmpty || mImage.mode == EditImageMode.MOSAIC && !mPen.isEmpty) {
            val count = mImage.onDrawMosaicsPath(canvas)
            if (mImage.mode == EditImageMode.MOSAIC && !mPen.isEmpty) {
                mDoodlePaint.strokeWidth = EditImagePath.BASE_MOSAIC_WIDTH
                canvas.save()
                val frame = mImage.clipFrame
                canvas.rotate(-mImage.rotate, frame.centerX(), frame.centerY())
                canvas.translate(scrollX.toFloat(), scrollY.toFloat())
                canvas.drawPath(mPen.path, mDoodlePaint)
                canvas.restore()
            }
            mImage.onDrawMosaic(canvas, count)
        }

        // 涂鸦   TODO 将绘制好的图片显示上去 目前只是在画布上显示而且  对于马赛克操作不是很友好
        mImage.onDrawDoodles(canvas)
        if (mImage.mode == EditImageMode.DOODLE && !mPen.isEmpty) {
            mDoodlePaint.color = mPen.color
            mDoodlePaint.strokeWidth = EditImagePath.BASE_DOODLE_WIDTH * mImage.scale
            canvas.save()
            val frame = mImage.clipFrame
            canvas.rotate(-mImage.rotate, frame.centerX(), frame.centerY())
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())
            canvas.drawPath(mPen.path, mDoodlePaint)
            canvas.restore()
        }





        if (mImage.isFreezing) {
            // 文字贴片
            mImage.onDrawStickers(canvas)
        }

        canvas.restore()

        if (!mImage.isFreezing) {
            // 文字贴片
            mImage.onDrawStickerClip(canvas)
            mImage.onDrawStickers(canvas)
        }

        // 箭头
        EditImageArrows.pathList.forEach {
            canvas.save()
            val frame = mImage.clipFrame
            canvas.rotate(-mImage.rotate, frame.centerX(), frame.centerY())
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())
            //开始绘画箭头
            canvas?.triangle(it)
            canvas.restore()
        }


        mImage.onDrawShade(canvas)
        // 裁剪
        if (mImage.mode == EditImageMode.CLIP) {
            canvas.save()
            canvas.translate(scrollX.toFloat(), scrollY.toFloat())
            mImage.onDrawClip(canvas, scrollX.toFloat(), scrollY.toFloat())
            canvas.restore()
        }
    }


    override fun onLayout(
        changed: Boolean,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            mImage!!.onWindowChanged(right - left.toFloat(), bottom - top.toFloat())
        }
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            onInterceptTouch(ev) || super.onInterceptTouchEvent(ev)
        } else super.onInterceptTouchEvent(ev)
    }

    private fun onInterceptTouch(event: MotionEvent?): Boolean {
        if (isHoming) {
            stopHoming()
            return true
        } else if (mImage!!.mode == EditImageMode.CLIP) {
            return true
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                removeCallbacks(this)
                if (mode == EditImageMode.ARROWS) {
                    if (IS_DEBUG)
                        Log.e(TAG, "箭头开始x==${event.x}y==${event.y}")
                    EditImageArrows.addArrows(event.x, event.y, arrowsColor, arrowsSize)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                postDelayed(this, 1200)
                if (mode == EditImageMode.ARROWS) {
                    if (IS_DEBUG)
                        Log.e(TAG, "箭头结束x==${event.x}y==${event.y}")
                    EditImageArrows.upDateArrows(event.x, event.y)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (mode == EditImageMode.ARROWS) {
                    if (IS_DEBUG)
                        Log.e(TAG, "箭头正在绘画x==${event.x}y==${event.y}")
                    EditImageArrows.upDateArrows(event.x, event.y)
                }
            }
        }
        return onTouch(event!!)
    }

    private fun onTouch(event: MotionEvent): Boolean {

        if (isHoming) {
            // Homing
            return false
        }
        mPointerCount = event.pointerCount
        var handled = mSGDetector!!.onTouchEvent(event)
        val mode = mImage!!.mode
        handled = if (mode == EditImageMode.NONE || mode == EditImageMode.CLIP) {
            handled or onTouchNONE(event)
        } else if (mPointerCount > 1) {
            onPathDone()
            handled or onTouchNONE(event)
        } else {
            handled or onTouchPath(event)
        }
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> mImage.onTouchDown(event.x, event.y)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mImage.onTouchUp(scrollX.toFloat(), scrollY.toFloat())
                onHoming()
            }
        }
        return handled
    }

    private fun onTouchNONE(event: MotionEvent?): Boolean {
        return mGDetector!!.onTouchEvent(event)
    }

    private fun onTouchPath(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> return onPathBegin(event)
            MotionEvent.ACTION_MOVE -> return onPathMove(event)
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> return mPen.isIdentity(
                event.getPointerId(
                    0
                )
            ) && onPathDone()
        }
        return false
    }

    private fun onPathBegin(event: MotionEvent): Boolean {
        mPen.reset(event.x, event.y)
        mPen.setIdentity(event.getPointerId(0))
        return true
    }

    private fun onPathMove(event: MotionEvent): Boolean {
        if (mPen.isIdentity(event.getPointerId(0))) {
            mPen.lineTo(event.x, event.y)
            invalidate()
            return true
        }
        return false
    }

    private fun onPathDone(): Boolean {
        if (mPen.isEmpty) {
            return false
        }
        mImage!!.addPath(mPen.toPath(), scrollX.toFloat(), scrollY.toFloat())
        mPen.reset()
        invalidate()
        return true
    }

    override fun run() {
        // 稳定触发
        if (!onSteady()) {
            postDelayed(this, 500)
        }
    }

    private fun onSteady(): Boolean {
        if (DEBUG) {
            Log.d(TAG, "onSteady: isHoming=$isHoming")
        }
        if (!isHoming) {
            mImage!!.onSteady(scrollX.toFloat(), scrollY.toFloat())
            onHoming()
            return true
        }
        return false
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(this)
        mImage!!.release()
    }

    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (mPointerCount > 1) {
            mImage!!.onScale(
                detector.scaleFactor,
                scrollX + detector.focusX,
                scrollY + detector.focusY
            )
            invalidate()
            return true
        }
        return false
    }

    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        if (mPointerCount > 1) {
            mImage!!.onScaleBegin()
            return true
        }
        return false
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        mImage!!.onScaleEnd()
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        mImage!!.onHoming(animation.animatedFraction)
        toApplyHoming(animation.animatedValue as EditHoming)
    }

    private fun toApplyHoming(homing: EditHoming) {
        mImage!!.scale = homing.scale
//        mImage.rotate = homing.rotate
        if (!onScrollTo(Math.round(homing.x), Math.round(homing.y))) {
            invalidate()
        }
    }

    private fun onScrollTo(x: Int, y: Int): Boolean {
        if (scrollX != x || scrollY != y) {
            scrollTo(x, y)
            return true
        }
        return false
    }

    override fun <V> onDismiss(stickerView: V) where V : View?, V : EditSticker? {
        mImage!!.onDismiss(stickerView)
        invalidate()
    }

    override fun <V> onShowing(stickerView: V) where V : View?, V : EditSticker? {
        mImage!!.onShowing(stickerView as EditSticker)
        invalidate()
    }

    override fun <V> onRemove(stickerView: V): Boolean where V : View?, V : EditSticker? {
        mImage?.onRemoveSticker(stickerView as EditSticker)
        stickerView!!.unregisterCallback(this)
        val parent = stickerView.parent
        if (parent != null) {
            (parent as ViewGroup).removeView(stickerView)
        }
        return true
    }

    override fun onAnimationStart(animation: Animator) {
        if (DEBUG) {
            Log.d(TAG, "onAnimationStart")
        }
        mImage!!.onHomingStart(mHomingAnimator!!.isRotate)
    }

    override fun onAnimationEnd(animation: Animator) {
        if (DEBUG) {
            Log.d(TAG, "onAnimationEnd")
        }
        if (mImage!!.onHomingEnd(
                scrollX.toFloat(),
                scrollY.toFloat(),
                mHomingAnimator!!.isRotate
            )
        ) {
            toApplyHoming(mImage.clip(scrollX.toFloat(), scrollY.toFloat()))
        }
    }

    override fun onAnimationCancel(animation: Animator) {
        if (DEBUG) {
            Log.d(TAG, "onAnimationCancel")
        }
        mImage!!.onHomingCancel(mHomingAnimator!!.isRotate)
    }

    override fun onAnimationRepeat(animation: Animator) {
        // empty implementation.
    }

    private fun onScroll(dx: Float, dy: Float): Boolean {
        val homing =
            mImage!!.onScroll(scrollX.toFloat(), scrollY.toFloat(), -dx, -dy)
        if (homing != null) {
            toApplyHoming(homing)
            return true
        }
        return onScrollTo(
            scrollX + Math.round(dx),
            scrollY + Math.round(dy)
        )
    }

    private inner class MoveAdapter : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            return this@EditImageView.onScroll(distanceX, distanceY)
        }


        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            // TODO
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }

    private class Pen : EditImagePath() {
        private var identity = Int.MIN_VALUE
        fun reset() {
            path.reset()
            identity = Int.MIN_VALUE
        }

        fun reset(x: Float, y: Float) {
            path.reset()
            path.moveTo(x, y)
            identity = Int.MIN_VALUE
        }

        fun setIdentity(identity: Int) {
            this.identity = identity
        }

        fun isIdentity(identity: Int): Boolean {
            return this.identity == identity
        }

        fun lineTo(x: Float, y: Float) {
            path.lineTo(x, y)
        }

        val isEmpty: Boolean
            get() = path.isEmpty

        fun toPath(): EditImagePath {
            return EditImagePath(
                Path(path),
                mode,
                color,
                width
            )
        }
    }

    companion object {
        private const val TAG = "IMGView"
        private const val DEBUG = false
    }

    init {
        // 涂鸦画刷
        mDoodlePaint.style = Paint.Style.STROKE
        mDoodlePaint.strokeWidth = EditImagePath.BASE_DOODLE_WIDTH
        mDoodlePaint.color = Color.RED
        mDoodlePaint.pathEffect = CornerPathEffect(EditImagePath.BASE_DOODLE_WIDTH)
        mDoodlePaint.strokeCap = Paint.Cap.ROUND
        mDoodlePaint.strokeJoin = Paint.Join.ROUND

        // 马赛克画刷
        mMosaicPaint.style = Paint.Style.STROKE
        mMosaicPaint.strokeWidth = EditImagePath.BASE_MOSAIC_WIDTH
        mMosaicPaint.color = Color.BLACK
        mMosaicPaint.pathEffect = CornerPathEffect(EditImagePath.BASE_MOSAIC_WIDTH)
        mMosaicPaint.strokeCap = Paint.Cap.ROUND
        mMosaicPaint.strokeJoin = Paint.Join.ROUND
    }

    init {
        initialize(context)
    }


    init {
        EditImageArrows.init()
    }


}