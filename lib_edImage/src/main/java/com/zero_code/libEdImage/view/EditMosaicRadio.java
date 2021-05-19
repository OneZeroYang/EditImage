package com.zero_code.libEdImage.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.zero_code.libEdImage.R;

/**
 * IMGMosaicRadio
 *
 * @author ZeroCode
 * @date 2021/5/10 : 16:05
 */
public class EditMosaicRadio extends androidx.appcompat.widget.AppCompatRadioButton implements
        ValueAnimator.AnimatorUpdateListener {

    private static final String TAG = "IMGMosaicRadio";

    private int mColor = Color.WHITE;

    private int mStrokeColor = Color.WHITE;

    private float mRadiusRatio = 0f;

    private ValueAnimator mAnimator;

    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float RADIUS_BASE = 0.1f;

    private float RADIUS_RING = 0.9f;

    private float RADIUS_BALL = 0.1f;

    public EditMosaicRadio(Context context) {
        this(context, null, 0);
    }

    public EditMosaicRadio(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public EditMosaicRadio(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IMGMosaicRadio);
        RADIUS_BASE = a.getFloat(R.styleable.IMGMosaicRadio_image_unselected_size, 0.1f);
        RADIUS_BALL = a.getFloat(R.styleable.IMGMosaicRadio_image_selected_size, 0.1f);

        a.recycle();

        setButtonDrawable(null);

        mPaint.setColor(mColor);
        mPaint.setStrokeWidth(5f);
    }

    private ValueAnimator getAnimator() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofFloat(0f, 1f);
            mAnimator.addUpdateListener(this);
            mAnimator.setDuration(200);
            mAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        return mAnimator;
    }



    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        float hw = getWidth() / 2f, hh = getHeight() / 2f;
        float radius = Math.min(hw, hh);

        canvas.save();
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(hw, hh, getBallRadius(radius), mPaint);

        mPaint.setColor(mStrokeColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(hw, hh, getRingRadius(radius), mPaint);
        canvas.restore();
    }

    private float getBallRadius(float radius) {
        return radius * ((RADIUS_BALL - RADIUS_BASE) * mRadiusRatio + RADIUS_BASE);
    }

    private float getRingRadius(float radius) {
        return radius * ((RADIUS_RING - RADIUS_BASE) * mRadiusRatio + RADIUS_BASE);
    }

    @Override
    public void setChecked(boolean checked) {
        boolean isChanged = checked != isChecked();

        super.setChecked(checked);

        if (isChanged) {
            ValueAnimator animator = getAnimator();

            if (checked) {
                animator.start();
                mStrokeColor=Color.parseColor("#007EFA");
            } else {
                animator.reverse();
                mStrokeColor=Color.WHITE;
            }
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mRadiusRatio = (float) animation.getAnimatedValue();
        invalidate();
    }

    public Float getSize() {
        return RADIUS_BASE;
    }
}
