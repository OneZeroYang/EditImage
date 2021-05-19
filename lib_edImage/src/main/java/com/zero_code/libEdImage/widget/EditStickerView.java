package com.zero_code.libEdImage.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.zero_code.libEdImage.R;
import com.zero_code.libEdImage.sticker.EditSticker;
import com.zero_code.libEdImage.sticker.EditStickerHelper;
import com.zero_code.libEdImage.sticker.EditStickerMoveHelper;


/**
 *
 * @author ZeroCode
 * @date 2021/5/17 : 14:16
 */

@SuppressLint("ResourceAsColor")
public abstract class EditStickerView extends ViewGroup implements EditSticker, View.OnClickListener {

    private static final String TAG = "EditStickerView";

    private View mContentView;

    private float mScale = 1f;

    // TODO
    private int mDownShowing = 0;

    private EditStickerMoveHelper mMoveHelper;

    private EditStickerHelper<EditStickerView> mStickerHelper;

    private ImageView mRemoveView, mAdjustView;

    private float mMaxScaleValue = MAX_SCALE_VALUE;

    private Paint PAINT;

    private Matrix mMatrix = new Matrix();

    private RectF mFrame = new RectF();

    private Rect mTempFrame = new Rect();

    private static final float MAX_SCALE_VALUE = 4f;

    private static final int ANCHOR_SIZE = 48;

    private static final int ANCHOR_SIZE_HALF = ANCHOR_SIZE >> 1;

    private static final float STROKE_WIDTH = 3f;

    {
        PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
        PAINT.setColor(R.color.title_font_color_right);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(STROKE_WIDTH);
    }

    public EditStickerView(Context context) {
        this(context, null, 0);
    }

    public EditStickerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditStickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        onInitialize(context);
    }

    public void onInitialize(Context context) {
        setBackgroundColor(Color.TRANSPARENT);

        mContentView = onCreateContentView(context);
        addView(mContentView, getContentLayoutParams());

        mRemoveView = new ImageView(context);
        mRemoveView.setScaleType(ImageView.ScaleType.FIT_XY);
        mRemoveView.setImageResource(R.mipmap.image_ic_delete);
        addView(mRemoveView, getAnchorLayoutParams());
        mRemoveView.setOnClickListener(this);

        mAdjustView = new ImageView(context);
        mAdjustView.setScaleType(ImageView.ScaleType.FIT_XY);
        mAdjustView.setImageResource(R.mipmap.image_ic_adjust);
        addView(mAdjustView, getAnchorLayoutParams());

        new EditStickerAdjustHelper(this, mAdjustView);

        mStickerHelper = new EditStickerHelper<>(this);
        mMoveHelper = new EditStickerMoveHelper(this);
    }

    public abstract View onCreateContentView(Context context);

    @Override
    public float getScale() {
        return mScale;
    }

    @Override
    public void setScale(float scale) {
        mScale = scale;

        mContentView.setScaleX(mScale);
        mContentView.setScaleY(mScale);

        int pivotX = (getLeft() + getRight()) >> 1;
        int pivotY = (getTop() + getBottom()) >> 1;

        mFrame.set(pivotX, pivotY, pivotX, pivotY);
        mFrame.inset(-(mContentView.getMeasuredWidth() >> 1), -(mContentView.getMeasuredHeight() >> 1));

        mMatrix.setScale(mScale, mScale, mFrame.centerX(), mFrame.centerY());
        mMatrix.mapRect(mFrame);

        mFrame.round(mTempFrame);

        layout(mTempFrame.left, mTempFrame.top, mTempFrame.right, mTempFrame.bottom);
    }

    @Override
    public void addScale(float scale) {
        setScale(getScale() * scale);
    }

    private LayoutParams getContentLayoutParams() {
        return new LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
    }

    private LayoutParams getAnchorLayoutParams() {
        return new LayoutParams(ANCHOR_SIZE, ANCHOR_SIZE);
    }

    @Override
    public void draw(Canvas canvas) {
        if (isShowing()) {
            canvas.drawRect(ANCHOR_SIZE_HALF, ANCHOR_SIZE_HALF,
                    getWidth() - ANCHOR_SIZE_HALF,
                    getHeight() - ANCHOR_SIZE_HALF, PAINT);
        }
        super.draw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                child.measure(widthMeasureSpec, heightMeasureSpec);

                maxWidth = Math.round(Math.max(maxWidth, child.getMeasuredWidth() * child.getScaleX()));
                maxHeight = Math.round(Math.max(maxHeight, child.getMeasuredHeight() * child.getScaleY()));

                childState = combineMeasuredStates(childState, child.getMeasuredState());
            }
        }

        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        mFrame.set(left, top, right, bottom);

        int count = getChildCount();
        if (count == 0) {
            return;
        }

        mRemoveView.layout(0, 0, mRemoveView.getMeasuredWidth(), mRemoveView.getMeasuredHeight());
        mAdjustView.layout(
                right - left - mAdjustView.getMeasuredWidth(),
                bottom - top - mAdjustView.getMeasuredHeight(),
                right - left, bottom - top
        );

        int centerX = (right - left) >> 1, centerY = (bottom - top) >> 1;
        int hw = mContentView.getMeasuredWidth() >> 1;
        int hh = mContentView.getMeasuredHeight() >> 1;

        mContentView.layout(centerX - hw, centerY - hh, centerX + hw, centerY + hh);
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        return isShowing() && super.drawChild(canvas, child, drawingTime);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!isShowing() && ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDownShowing = 0;
            show();
            return true;
        }
        return isShowing() && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        boolean handled = mMoveHelper.onTouch(this, event);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownShowing++;
                break;
            case MotionEvent.ACTION_UP:
                if (mDownShowing > 1 && event.getEventTime() - event.getDownTime() < ViewConfiguration.getTapTimeout()) {
                    onContentTap();
                    return true;
                }
                break;
        }

        return handled | super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        if (v == mRemoveView) {
            onRemove();
        }
    }

    public void onRemove() {
        mStickerHelper.remove();
    }

    public void onContentTap() {

    }

    @Override
    public boolean show() {
        return mStickerHelper.show();
    }

    @Override
    public boolean remove() {
        return mStickerHelper.remove();
    }

    @Override
    public boolean dismiss() {
        return mStickerHelper.dismiss();
    }

    @Override
    public boolean isShowing() {
        return mStickerHelper.isShowing();
    }

    @Override
    public RectF getFrame() {
        return mStickerHelper.getFrame();
    }

    @Override
    public void onSticker(Canvas canvas) {
        canvas.translate(mContentView.getX(), mContentView.getY());
        mContentView.draw(canvas);
    }

    @Override
    public void registerCallback(Callback callback) {
        mStickerHelper.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mStickerHelper.unregisterCallback(callback);
    }
}
