package com.zero_code.libEdImage.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

/**
 * 马赛克专用
 * @author ZeroCode
 * @date 2021/5/10 : 16:04
 */
public class EditMosaicGroup extends RadioGroup {

    /**
     * 0.2-0.6
     */
    public static final  Float IMG_MOSAIC_SIZE_1=0.2F;
    public static final  Float IMG_MOSAIC_SIZE_2=0.3F;
    public static final  Float IMG_MOSAIC_SIZE_3=0.4F;
    public static final  Float IMG_MOSAIC_SIZE_4=0.5F;
    public static final  Float IMG_MOSAIC_SIZE_5=0.6F;
    public static final  Float IMG_MOSAIC_SIZE_NO=-1F;

    public EditMosaicGroup(Context context) {
        super(context);
    }

    public EditMosaicGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Float getCheckSize() {
        int checkedId = getCheckedRadioButtonId();
        EditMosaicRadio radio = findViewById(checkedId);
        if (radio != null) {
            return radio.getSize();
        }
        return IMG_MOSAIC_SIZE_NO;
    }

    public void setCheckSize(Float size) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            EditMosaicRadio radio = (EditMosaicRadio) getChildAt(i);
            if (Math.abs(radio.getSize() - size)>=0) {
                radio.setChecked(true);
                break;
            }
        }
    }
}
