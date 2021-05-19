package com.zero_code.libEdImage.core

import android.graphics.*
import android.util.Log
import com.zero_code.libEdImage.IS_DEBUG
import com.zero_code.libEdImage.clip.EditClip.Anchor
import com.zero_code.libEdImage.clip.EditClipWindow
import com.zero_code.libEdImage.homing.EditHoming
import com.zero_code.libEdImage.sticker.EditSticker
import com.zero_code.libEdImage.util.IMGUtils
import java.util.*

/**
 * EditImage
 * @author ZeroCode
 * @date 2021/5/8 : 13:35
 */
class EditImage {
    private var mImage: Bitmap?
    private var mMosaicImage: Bitmap? = null

    /**
     * 完整图片边框
     */
    val frame = RectF()

    /**
     * 裁剪图片边框（显示的图片区域）
     */
    val clipFrame = RectF()
    private val mTempClipFrame = RectF()

    /**
     * 裁剪模式前状态备份
     */
    private val mBackupClipFrame = RectF()

    //    private var mBackupClipRotate = 0f
    var rotate = 0f

    var mRotate = 0f

    //    var targetRotate = 0f
    private var isRequestToBaseFitting = false
    private var isAnimCanceled = false

    /**
     * 裁剪模式时当前触摸锚点
     */
    private var mAnchor: Anchor? = null
    private var isSteady = true
    private val mShade = Path()

    /**
     * 裁剪窗口
     */
    private val mClipWin = EditClipWindow()
    private var isDrawClip = false

    /**
     * 编辑模式
     */
    private var mMode = EditImageMode.NONE
    internal var isFreezing = mMode == EditImageMode.CLIP

    /**
     * 可视区域，无Scroll 偏移区域
     */
    private val mWindow = RectF()

    /**
     * 是否初始位置
     */
    private var isInitialHoming = false

    /**
     * 当前选中贴片
     */
    private var mForeSticker: EditSticker? = null
    private var foreSticker: EditSticker? = null

    /**
     * 为被选中贴片
     */
    private val mBackStickers: MutableList<EditSticker> =
        ArrayList()

    /**
     * 涂鸦路径
     */
    private val mDoodles: MutableList<EditImagePath> = ArrayList()

    /**
     * 马赛克路径
     */
    private val mMosaics: MutableList<EditImagePath> = ArrayList()
    private var mPaint: Paint? = null
    private var mMosaicPaint: Paint? = null
    private var mShadePaint: Paint? = null
    private val M = Matrix()

    companion object {
        private const val TAG = "EditImage"
        private const val MIN_SIZE = 500
        private const val MAX_SIZE = 10000
        private const val DEBUG = false
        private var DEFAULT_IMAGE: Bitmap? = null
        private const val COLOR_SHADE = -0x34000000
//        private const val COLOR_SHADE = Color.RED

        init {
            DEFAULT_IMAGE =
                Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        }
    }

    fun setBitmap(bitmap: Bitmap?) {
        if (bitmap == null || bitmap.isRecycled) {
            return
        }
        mImage = bitmap

//        // 清空马赛克图层
//        if (mMosaicImage != null) {
//            mMosaicImage!!.recycle()
//        }
//        mMosaicImage = null
        makeMosaicBitmap()
        onImageChanged()
    }

    // 初始化Shade 画刷

    // 备份裁剪前Clip 区域

    // 重置裁剪区域
    var mode: EditImageMode
        get() = mMode
        set(mode) {
            if (mMode == mode) return
            moveToBackground(mForeSticker)
            if (mode == EditImageMode.CLIP) {
                setFreezing(true)
            }
            mMode = mode
            if (mMode == EditImageMode.CLIP) {

                // 初始化Shade 画刷
                initShadePaint()

                // 备份裁剪前Clip 区域
//                mBackupClipRotate = rotate
                mBackupClipFrame.set(clipFrame)
                val scale = 1 / scale
                M.setTranslate(-frame.left, -frame.top)
                M.postScale(scale, scale)
                M.mapRect(mBackupClipFrame)

                // 重置裁剪区域
                mClipWin.reset(clipFrame, rotate)
            } else {
                if (mMode == EditImageMode.MOSAIC) {
                    makeMosaicBitmap()
                }
                mClipWin.isClipping = false
            }
        }

    // TODO
    private fun rotateStickers(rotate: Float) {
        M.setRotate(rotate, clipFrame.centerX(), clipFrame.centerY())
        for (sticker in mBackStickers) {
            M.mapRect(sticker.frame)
            sticker.rotation = sticker.rotation + rotate
            sticker.x = sticker.frame.centerX() - sticker.pivotX
            sticker.y = sticker.frame.centerY() - sticker.pivotY
        }
    }

    private fun initShadePaint() {
        if (mShadePaint == null) {
            mShadePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mShadePaint!!.color =
                COLOR_SHADE
            mShadePaint!!.style = Paint.Style.FILL
        }
    }

    val isMosaicEmpty: Boolean
        get() = mMosaics.isEmpty()

    val isDoodleEmpty: Boolean
        get() = mDoodles.isEmpty()

    fun undoDoodle() {
        if (!mDoodles.isEmpty()) {
            mDoodles.removeAt(mDoodles.size - 1)
        }
    }

    fun undoMosaic() {
        if (!mMosaics.isEmpty()) {
            mMosaics.removeAt(mMosaics.size - 1)
        }
    }


    fun recycle(){
        mImage?.recycle()
        mMosaicImage?.recycle()
        DEFAULT_IMAGE?.recycle()
    }

    /**
     * 裁剪区域旋转回原始角度后形成新的裁剪区域，旋转中心发生变化，
     * 因此需要将视图窗口平移到新的旋转中心位置。
     */
    fun clip(scrollX: Float, scrollY: Float): EditHoming {
        val frame = mClipWin.getOffsetFrame(scrollX, scrollY)

        M.mapRect(clipFrame, frame)
        return EditHoming(
            scrollX,
            scrollY,
            scale, 0f
        )
    }

    fun toBackupClip() {
        M.setScale(scale, scale)
        M.postTranslate(frame.left, frame.top)
        M.mapRect(clipFrame, mBackupClipFrame)

        isRequestToBaseFitting = true
    }

    fun resetClip() {
        clipFrame.set(frame)
        mClipWin.reset(clipFrame, rotate)
    }


    /**
     * 水平镜像翻转
     */
    fun toHorizontalMirror() {
        if (mImage != null && !mImage!!.isRecycled) {
            val w = mImage!!.width
            val h = mImage!!.height
            val matrix = Matrix()
            matrix.postScale(-1f, 1f) // 水平镜像翻转
            mImage = Bitmap.createBitmap(mImage!!, 0, 0, w, h, matrix, true)
        }

    }


    /**
     * 在当前基础上旋转
     */
    fun rotate(rotate: Float) {

        if (mImage != null && !mImage!!.isRecycled) {
            val w = mImage!!.width
            val h = mImage!!.height
            val matrix = Matrix()
            matrix.setTranslate(frame.centerX(), frame.centerY())
            matrix.setRotate(rotate)
            matrix.mapRect(frame)
            mImage = Bitmap.createBitmap(mImage!!, 0, 0, w, h, matrix, false)
        }
    }


    fun getRotateBitmap(rotate: Float): Bitmap? {
        if (mImage != null && !mImage!!.isRecycled) {
            val w = mImage!!.width
            val h = mImage!!.height
            val matrix = Matrix()
            matrix.setTranslate(frame.centerX(), frame.centerY())
            matrix.setRotate(rotate)
            return Bitmap.createBitmap(mImage!!, 0, 0, w, h, matrix, false)
        }
        return null
    }


    /**
     *
     * @param bitmap
     * @param orientationDegree 0 - 360 范围
     * @return
     */
    fun adjustPhotoRotation(bitmap: Bitmap, orientationDegree: Int): Bitmap? {
        val matrix = Matrix()
        matrix.setRotate(
            orientationDegree.toFloat(), bitmap.width.toFloat() / 2,
            bitmap.height.toFloat() / 2
        )
        val targetX: Float
        val targetY: Float
        if (orientationDegree == 90) {
            targetX = bitmap.height.toFloat()
            targetY = 0f
        } else {
            targetX = bitmap.height.toFloat()
            targetY = bitmap.width.toFloat()
        }
        val values = FloatArray(9)
        matrix.getValues(values)
        val x1 = values[Matrix.MTRANS_X]
        val y1 = values[Matrix.MTRANS_Y]
        matrix.postTranslate(targetX - x1, targetY - y1)
        val canvasBitmap = Bitmap.createBitmap(
            bitmap.height, bitmap.width,
            Bitmap.Config.ARGB_8888
        )
        val paint = Paint()
        val canvas = Canvas(canvasBitmap)
        canvas.drawBitmap(bitmap, matrix, paint)
        return canvasBitmap
    }


    /**
     * 垂直镜像翻转
     */
    fun toVerticalMirror() {
        if (mImage != null && !mImage!!.isRecycled) {
            val w = mImage!!.width
            val h = mImage!!.height
            val matrix = Matrix()
            matrix.postScale(1f, -1f) // 垂直镜像翻转
            mImage = Bitmap.createBitmap(mImage!!, 0, 0, w, h, matrix, true)
        }

    }


    /**
     * 清除镜像翻转
     */
    fun clearMirror() {
        if (mImage != null && !mImage!!.isRecycled) {
            val w = mImage!!.width
            val h = mImage!!.height
            val matrix = Matrix()
            matrix.postScale(1f, 1f) // 垂直镜像翻转
            mImage = Bitmap.createBitmap(mImage!!, 0, 0, w, h, matrix, true)
        }

    }


    // TODO 这里可以设置马赛克模糊块的大小
    private fun makeMosaicBitmap() {
        if (mMosaicImage != null || mImage == null) {
            return
        }
        if (mMode == EditImageMode.MOSAIC) {
            //控制马赛克模糊程度的
            var w = Math.round(mImage!!.width / 32F)
            var h = Math.round(mImage!!.height / 32F)
            w = Math.max(w, 16)
            h = Math.max(h, 16)

            // 马赛克画刷
            if (mMosaicPaint == null) {
                mMosaicPaint = Paint(Paint.ANTI_ALIAS_FLAG)
                mMosaicPaint!!.isFilterBitmap = false
                mMosaicPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            }
            mMosaicImage = Bitmap.createScaledBitmap(mImage!!, w, h, false)
        }
    }

    private fun onImageChanged() {
        isInitialHoming = false
        onWindowChanged(mWindow.width(), mWindow.height())
        if (mMode == EditImageMode.CLIP) {
            mClipWin.reset(clipFrame, rotate)
        }
    }

    fun onClipHoming(): Boolean {
        return mClipWin.homing()
    }

    fun getStartHoming(scrollX: Float, scrollY: Float): EditHoming {
        return EditHoming(
            scrollX,
            scrollY,
            scale,
            0f
        )
    }

    fun getEndHoming(scrollX: Float, scrollY: Float): EditHoming {
        val homing = EditHoming(
            scrollX,
            scrollY,
            scale,
            0f
        )
        if (mMode == EditImageMode.CLIP) {
            val frame = RectF(mClipWin.targetFrame)
            frame.offset(scrollX, scrollY)
            if (mClipWin.isResetting) {
                val clipFrame = RectF()
                M.setRotate(0f, clipFrame.centerX() / 2, clipFrame.centerY() / 2)
                M.mapRect(clipFrame, this.clipFrame)
                homing.rConcat(IMGUtils.fill(frame, clipFrame))
            } else {
                val cFrame = RectF()

                // cFrame要是一个暂时clipFrame
                if (mClipWin.isHoming) {
//
//                    M.mapRect(cFrame, mClipFrame);

//                    mClipWin
                    // TODO 偏移中心
                    M.setRotate(
                        0f,
                        clipFrame.centerX(),
                        clipFrame.centerY()
                    )
                    M.mapRect(cFrame, mClipWin.getOffsetFrame(scrollX, scrollY))
                    homing.rConcat(
                        IMGUtils.fitHoming(
                            frame,
                            cFrame,
                            clipFrame.centerX(),
                            clipFrame.centerY()
                        )
                    )
                } else {
                    M.setRotate(0f, clipFrame.centerX(), clipFrame.centerY())
                    M.mapRect(cFrame, this.frame)
                    homing.rConcat(
                        IMGUtils.fillHoming(
                            frame,
                            cFrame,
                            clipFrame.centerX(),
                            clipFrame.centerY()
                        )
                    )
                }
            }
        } else {
            val clipFrame = RectF()
            M.setRotate(0f, clipFrame.centerX(), clipFrame.centerY())
            M.mapRect(clipFrame, this.clipFrame)
            val win = RectF(mWindow)
            win.offset(scrollX, scrollY)
            homing.rConcat(IMGUtils.fitHoming(win, clipFrame, isRequestToBaseFitting))
            isRequestToBaseFitting = false
        }
        return homing
    }

    fun <S : EditSticker?> addSticker(sticker: S?) {
        sticker?.let { moveToForeground(it) }
    }

    fun addPath(path: EditImagePath?, sx: Float, sy: Float) {
        if (path == null) return
        val scale = 1f / scale
        M.setTranslate(sx, sy)
        M.postRotate(0f, clipFrame.centerX(), clipFrame.centerY())
        M.postTranslate(-frame.left, -frame.top)
        M.postScale(scale, scale)
        path.transform(M)
        when (path.mode) {
            EditImageMode.DOODLE -> mDoodles.add(path)
            EditImageMode.MOSAIC -> {
                path.width = path.width * scale
                mMosaics.add(path)
            }
        }
    }

    private fun moveToForeground(sticker: EditSticker?) {
        if (sticker == null) return
        moveToBackground(mForeSticker)
        if (sticker.isShowing) {
//            mForeSticker = sticker
            foreSticker = sticker
            // 从BackStickers中移除
            mBackStickers.remove(sticker)
        } else {
            sticker.show()
        }
    }

    private fun moveToBackground(sticker: EditSticker?) {
        if (sticker == null) return
        if (!sticker.isShowing) {
            // 加入BackStickers中
            if (!mBackStickers.contains(sticker)) {
                mBackStickers.add(sticker)
            }
            if (mForeSticker == sticker) {
                mForeSticker = null
            }
        } else {
            sticker.dismiss()
        }
    }

    fun stickAll() {
        moveToBackground(foreSticker)
    }

    fun onDismiss(sticker: EditSticker?) {
        moveToBackground(sticker)
    }

    fun onShowing(sticker: EditSticker) {
        if (mForeSticker != sticker) {
            moveToForeground(sticker)
        }
    }

    fun onRemoveSticker(sticker: EditSticker) {
        if (mForeSticker == sticker) {
            mForeSticker = null
        } else {
            mBackStickers.remove(sticker)
        }
    }

    fun onWindowChanged(width: Float, height: Float) {
        if (width == 0f || height == 0f) {
            return
        }
        mWindow[0f, 0f, width] = height
        if (!isInitialHoming) {
            onInitialHoming(width, height)
        } else {

            // Pivot to fit window.
            M.setTranslate(
                mWindow.centerX() - clipFrame.centerX(),
                mWindow.centerY() - clipFrame.centerY()
            )
            M.mapRect(frame)
            M.mapRect(clipFrame)
        }
        mClipWin.setClipWinSize(width, height)
    }

    private fun onInitialHoming(width: Float, height: Float) {
        frame[0f, 0f, mImage!!.width.toFloat()] = mImage!!.height.toFloat()
        clipFrame.set(frame)
        mClipWin.setClipWinSize(width, height)
        if (clipFrame.isEmpty) {
            return
        }
        toBaseHoming()
        isInitialHoming = true
        onInitialHomingDone()
    }

    private fun toBaseHoming() {
        if (clipFrame.isEmpty) {
            // Bitmap invalidate.
            return
        }
        val scale = Math.min(
            mWindow.width() / clipFrame.width(),
            mWindow.height() / clipFrame.height()
        )

        // Scale to fit window.
        M.setScale(scale, scale, clipFrame.centerX(), clipFrame.centerY())
        M.postTranslate(
            mWindow.centerX() - clipFrame.centerX(),
            mWindow.centerY() - clipFrame.centerY()
        )
        M.mapRect(frame)
        M.mapRect(clipFrame)
    }

    private fun onInitialHomingDone() {
        if (mMode == EditImageMode.CLIP) {
            mClipWin.reset(clipFrame, rotate)
        }
    }

    fun onDrawImage(canvas: Canvas) {

        // 裁剪区域
        canvas.clipRect(if (mClipWin.isClipping) frame else clipFrame)

        // 绘制图片

        canvas.drawBitmap(mImage!!, null, frame, null)
        if (DEBUG) {
            // Clip 区域
            mPaint!!.color = Color.RED
            mPaint!!.strokeWidth = 6f
            canvas.drawRect(frame, mPaint!!)
            canvas.drawRect(clipFrame, mPaint!!)
        }
    }

    fun onDrawMosaicsPath(canvas: Canvas): Int {
        val layerCount = canvas.saveLayer(frame, null, Canvas.ALL_SAVE_FLAG)
        if (!isMosaicEmpty) {
            canvas.save()
            val scale = scale
            canvas.translate(frame.left, frame.top)
            canvas.scale(scale, scale)
            for (path in mMosaics) {
                path.onDrawMosaic(canvas, mPaint!!)
            }
            canvas.restore()
        }
        return layerCount
    }

    fun onDrawMosaic(canvas: Canvas, layerCount: Int) {
        //将马赛克绘制到图片上
        canvas.drawBitmap(mMosaicImage!!, null, frame, mMosaicPaint)
        canvas.restoreToCount(layerCount)
    }

    fun onDrawDoodles(canvas: Canvas) {
        if (!isDoodleEmpty) {
            canvas.save()
            val scale = scale
            canvas.translate(frame.left, frame.top)
            canvas.scale(scale, scale)
            for (path in mDoodles) {
                path.onDrawDoodle(canvas, mPaint!!)
            }
            canvas.restore()
        }
    }

    fun onDrawStickerClip(canvas: Canvas) {
        M.setRotate(0f, clipFrame.centerX(), clipFrame.centerY())
        M.mapRect(mTempClipFrame, if (mClipWin.isClipping) frame else clipFrame)
        canvas.clipRect(mTempClipFrame)
    }

    fun onDrawStickers(canvas: Canvas) {
        if (mBackStickers.isEmpty()) return
        canvas.save()
        for (sticker in mBackStickers) {
            if (!sticker.isShowing) {
                val tPivotX = sticker.x + sticker.pivotX
                val tPivotY = sticker.y + sticker.pivotY
                canvas.save()
                M.setTranslate(sticker.x, sticker.y)
                M.postScale(sticker.scale, sticker.scale, tPivotX, tPivotY)
                M.postRotate(sticker.rotation, tPivotX, tPivotY)
                canvas.concat(M)
                sticker.onSticker(canvas)
                canvas.restore()
            }
        }
        canvas.restore()
    }


    /**
     * 剪裁后 画出 剪裁得 区域  基于 图片
     */
    fun onDrawShade(canvas: Canvas) {
        if (mMode == EditImageMode.CLIP && isSteady) {
            mShade.reset()
            mShade.addRect(
                frame.left - 2,
                frame.top - 2,
                frame.right + 2,
                frame.bottom + 2,
                Path.Direction.CW
            )
            mShade.addRect(clipFrame, Path.Direction.CCW)

            canvas.drawPath(mShade, mShadePaint!!)
        }
    }

    fun onDrawClip(
        canvas: Canvas?,
        scrollX: Float,
        scrollY: Float
    ) {
        if (mMode == EditImageMode.CLIP) {
            mClipWin.onDraw(canvas)
        }
    }

    fun onTouchDown(x: Float, y: Float) {
        isSteady = false
        moveToBackground(mForeSticker)
        if (mMode == EditImageMode.CLIP) {
            mAnchor = mClipWin.getAnchor(x, y)
        }
    }

    fun onTouchUp(scrollX: Float, scrollY: Float) {
        if (mAnchor != null) {
            mAnchor = null
        }
    }

    fun onSteady(scrollX: Float, scrollY: Float) {
        isSteady = true
        onClipHoming()
        mClipWin.isShowShade = true
    }

    fun onScaleBegin() {}
    fun onScroll(
        scrollX: Float,
        scrollY: Float,
        dx: Float,
        dy: Float
    ): EditHoming? {
        if (mMode == EditImageMode.CLIP) {
            mClipWin.isShowShade = false
            if (mAnchor != null) {
                mClipWin.onScroll(mAnchor, dx, dy)
                val clipFrame = RectF()
                M.setRotate(0f, clipFrame.centerX(), clipFrame.centerY())
                M.mapRect(clipFrame, frame)
                val frame = mClipWin.getOffsetFrame(scrollX, scrollY)
                val homing = EditHoming(
                    scrollX,
                    scrollY,
                    scale,
                    0f
                )
                homing.rConcat(
                    IMGUtils.fillHoming(
                        frame,
                        clipFrame,
                        clipFrame.centerX(),
                        clipFrame.centerY()
                    )
                )
                return homing
            }
        }
        return null
    }


    var scale: Float
        get() = 1f * frame.width() / mImage!!.width
        set(scale) {
            setScale(scale, clipFrame.centerX(), clipFrame.centerY())
        }

    fun setScale(scale: Float, focusX: Float, focusY: Float) {
        onScale(scale / scale, focusX, focusY)
    }

    fun onScale(factor: Float, focusX: Float, focusY: Float) {
        var factor = factor
        if (factor == 1f) return
        if (Math.max(
                clipFrame.width(),
                clipFrame.height()
            ) >= MAX_SIZE
            || Math.min(
                clipFrame.width(),
                clipFrame.height()
            ) <= MIN_SIZE
        ) {
            factor += (1 - factor) / 2
        }
        M.setScale(factor, factor, focusX, focusY)
        M.mapRect(frame)
        M.mapRect(clipFrame)

        // 修正clip 窗口
        if (!frame.contains(clipFrame)) {
            // TODO
//            mClipFrame.intersect(mFrame);
        }
        for (sticker in mBackStickers) {
            M.mapRect(sticker.frame)
            val tPivotX = sticker.x + sticker.pivotX
            val tPivotY = sticker.y + sticker.pivotY
            sticker.addScale(factor)
            sticker.x = sticker.x + sticker.frame.centerX() - tPivotX
            sticker.y = sticker.y + sticker.frame.centerY() - tPivotY
        }
    }

    fun onScaleEnd() {}
    fun onHomingStart(isRotate: Boolean) {
        isAnimCanceled = false
        isDrawClip = true
    }

    fun onHoming(fraction: Float) {
        mClipWin.homing(fraction)
    }

    fun onHomingEnd(
        scrollX: Float,
        scrollY: Float,
        isRotate: Boolean
    ): Boolean {
        isDrawClip = true
        if (mMode == EditImageMode.CLIP) {
            // 开启裁剪模式
            val clip = !isAnimCanceled
            mClipWin.isHoming = false
            mClipWin.isClipping = true
            mClipWin.isResetting = false
            return clip
        } else {
            if (isFreezing && !isAnimCanceled) {
                setFreezing(false)
            }
        }
        return false
    }

    fun isFreezing(): Boolean {
        return isFreezing
    }

    private fun setFreezing(freezing: Boolean) {
        if (freezing != isFreezing) {
            rotateStickers(if (freezing) 0f else 0f)
            isFreezing = freezing
        }
    }

    fun onHomingCancel(isRotate: Boolean) {
        isAnimCanceled = true
        if (IS_DEBUG)
            Log.d(TAG, "Homing cancel")
    }

    fun release() {
        if (mImage != null && !mImage!!.isRecycled) {
            mImage!!.recycle()
        }
    }

    @Throws(Throwable::class)
    protected fun finalize() {
        if (DEFAULT_IMAGE != null) {
            DEFAULT_IMAGE!!.recycle()
        }
    }

    init {
        mShade.fillType = Path.FillType.WINDING

        // Doodle&Mosaic 's paint
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint?.style = Paint.Style.STROKE
        mPaint?.strokeWidth =
            EditImagePath.BASE_DOODLE_WIDTH
        mPaint?.color = Color.RED
        mPaint?.pathEffect = CornerPathEffect(EditImagePath.BASE_DOODLE_WIDTH)
        mPaint?.strokeCap = Paint.Cap.ROUND
        mPaint?.strokeJoin = Paint.Join.ROUND
    }

    init {
        mImage = DEFAULT_IMAGE
        if (mMode == EditImageMode.CLIP) {
            initShadePaint()
        }
    }
}