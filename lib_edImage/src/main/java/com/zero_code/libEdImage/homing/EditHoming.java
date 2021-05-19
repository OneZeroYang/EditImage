package com.zero_code.libEdImage.homing;

/**
 *
 * @author ZeroCode
 * @date 2021/5/17 : 14:16
 */

public class EditHoming {

    public float x, y;

    public float scale;

    public float rotate;

    public EditHoming(float x, float y, float scale, float rotate) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.rotate = rotate;
    }

    public void set(float x, float y, float scale, float rotate) {
        this.x = x;
        this.y = y;
        this.scale = scale;
        this.rotate = rotate;
    }

    public void concat(EditHoming homing) {
        this.scale *= homing.scale;
        this.x += homing.x;
        this.y += homing.y;
    }

    public void rConcat(EditHoming homing) {
        this.scale *= homing.scale;
        this.x -= homing.x;
        this.y -= homing.y;
    }

    public static boolean isRotate(EditHoming sHoming, EditHoming eHoming) {
        return Float.compare(sHoming.rotate, eHoming.rotate) != 0;
    }

    @Override
    public String toString() {
        return "IMGHoming{" +
                "x=" + x +
                ", y=" + y +
                ", scale=" + scale +
                ", rotate=" + rotate +
                '}';
    }
}
