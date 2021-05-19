package com.zero_code.libEdImage.anim;

import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.zero_code.libEdImage.homing.EditHoming;
import com.zero_code.libEdImage.homing.EditHomingEvaluator;

/**
 *
 * @author ZeroCode
 * @date 2021/5/17 : 14:16
 */

public class EditHomingAnimator extends ValueAnimator {

    private boolean isRotate = false;

    private EditHomingEvaluator mEvaluator;

    public EditHomingAnimator() {
        setInterpolator(new AccelerateDecelerateInterpolator());
    }

    @Override
    public void setObjectValues(Object... values) {
        super.setObjectValues(values);
        if (mEvaluator == null) {
            mEvaluator = new EditHomingEvaluator();

        }
        setEvaluator(mEvaluator);
    }

    public void setHomingValues(EditHoming sHoming, EditHoming eHoming) {
        setObjectValues(sHoming, eHoming);
        isRotate = EditHoming.isRotate(sHoming, eHoming);
    }

    public boolean isRotate() {
        return isRotate;
    }
}
