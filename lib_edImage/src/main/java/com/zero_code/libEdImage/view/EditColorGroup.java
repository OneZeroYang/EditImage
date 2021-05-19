package com.zero_code.libEdImage.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RadioGroup;



public class EditColorGroup extends RadioGroup {

    public EditColorGroup(Context context) {
        super(context);
    }

    public EditColorGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCheckColor() {
        int checkedId = getCheckedRadioButtonId();
        EditColorRadio radio = findViewById(checkedId);
        if (radio != null) {
            return radio.getColor();
        }
        return Color.WHITE;
    }

    public void setCheckColor(int color) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            EditColorRadio radio = (EditColorRadio) getChildAt(i);
            if (radio.getColor() == color) {
                radio.setChecked(true);
                break;
            }
        }
    }
}
