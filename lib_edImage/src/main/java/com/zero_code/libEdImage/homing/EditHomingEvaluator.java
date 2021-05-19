package com.zero_code.libEdImage.homing;

import android.animation.TypeEvaluator;

/**
 *
 * @author ZeroCode
 * @date 2021/5/17 : 14:16
 */

public class EditHomingEvaluator implements TypeEvaluator<EditHoming> {

    private EditHoming homing;

    public EditHomingEvaluator() {

    }

    public EditHomingEvaluator(EditHoming homing) {
        this.homing = homing;
    }

    @Override
    public EditHoming evaluate(float fraction, EditHoming startValue, EditHoming endValue) {
        float x = startValue.x + fraction * (endValue.x - startValue.x);
        float y = startValue.y + fraction * (endValue.y - startValue.y);
        float scale = startValue.scale + fraction * (endValue.scale - startValue.scale);
        float rotate = startValue.rotate + fraction * (endValue.rotate - startValue.rotate);

        if (homing == null) {
            homing = new EditHoming(x, y, scale, rotate);
        } else {
            homing.set(x, y, scale, rotate);
        }

        return homing;
    }
}
