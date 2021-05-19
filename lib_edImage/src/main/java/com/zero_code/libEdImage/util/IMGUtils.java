package com.zero_code.libEdImage.util;

import android.graphics.Matrix;
import android.graphics.RectF;

import com.zero_code.libEdImage.homing.EditHoming;

/**
 * 
 * @author ZeroCode
 * @date 2021/5/8 : 13:36
 */

public class IMGUtils {

    private static final Matrix M = new Matrix();

    private IMGUtils() {

    }

    public static void center(RectF win, RectF frame) {
        frame.offset(win.centerX() - frame.centerX(), win.centerY() - frame.centerY());
    }

    public static void fitCenter(RectF win, RectF frame) {
        fitCenter(win, frame, 0);
    }

    public static void fitCenter(RectF win, RectF frame, float padding) {
        fitCenter(win, frame, padding, padding, padding, padding);
    }

    public static void fitCenter(RectF win, RectF frame, float paddingLeft, float paddingTop, float paddingRight, float paddingBottom) {
        if (win.isEmpty() || frame.isEmpty()) {
            return;
        }

        if (win.width() < paddingLeft + paddingRight) {
            paddingLeft = paddingRight = 0;
            // 忽略Padding 值
        }

        if (win.height() < paddingTop + paddingBottom) {
            paddingTop = paddingBottom = 0;
            // 忽略Padding 值
        }

        float w = win.width() - paddingLeft - paddingRight;
        float h = win.height() - paddingTop - paddingBottom;

        float scale = Math.min(w / frame.width(), h / frame.height());

        // 缩放FIT
        frame.set(0, 0, frame.width() * scale, frame.height() * scale);

        // 中心对齐
        frame.offset(
                win.centerX() + (paddingLeft - paddingRight) / 2 - frame.centerX(),
                win.centerY() + (paddingTop - paddingBottom) / 2 - frame.centerY()
        );
    }

    public static EditHoming fitHoming(RectF win, RectF frame) {
        EditHoming dHoming = new EditHoming(0, 0, 1, 0);

        if (frame.contains(win)) {
            // 不需要Fit
            return dHoming;
        }

        // 宽高都小于Win，才有必要放大
        if (frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.scale = Math.min(win.width() / frame.width(), win.height() / frame.height());
        }

        RectF rect = new RectF();
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        if (rect.width() < win.width()) {
            dHoming.x += win.centerX() - rect.centerX();
        } else {
            if (rect.left > win.left) {
                dHoming.x += win.left - rect.left;
            } else if (rect.right < win.right) {
                dHoming.x += win.right - rect.right;
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y += win.centerY() - rect.centerY();
        } else {
            if (rect.top > win.top) {
                dHoming.y += win.top - rect.top;
            } else if (rect.bottom < win.bottom) {
                dHoming.y += win.bottom - rect.bottom;
            }
        }

        return dHoming;
    }

    public static EditHoming fitHoming(RectF win, RectF frame, float centerX, float centerY) {
        EditHoming dHoming = new EditHoming(0, 0, 1, 0);

        if (frame.contains(win)) {
            // 不需要Fit
            return dHoming;
        }

        // 宽高都小于Win，才有必要放大
        if (frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.scale = Math.min(win.width() / frame.width(), win.height() / frame.height());
        }

        RectF rect = new RectF();
        M.setScale(dHoming.scale, dHoming.scale, centerX, centerY);
        M.mapRect(rect, frame);

        if (rect.width() < win.width()) {
            dHoming.x += win.centerX() - rect.centerX();
        } else {
            if (rect.left > win.left) {
                dHoming.x += win.left - rect.left;
            } else if (rect.right < win.right) {
                dHoming.x += win.right - rect.right;
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y += win.centerY() - rect.centerY();
        } else {
            if (rect.top > win.top) {
                dHoming.y += win.top - rect.top;
            } else if (rect.bottom < win.bottom) {
                dHoming.y += win.bottom - rect.bottom;
            }
        }

        return dHoming;
    }


    public static EditHoming fitHoming(RectF win, RectF frame, boolean isJustInner) {
        EditHoming dHoming = new EditHoming(0, 0, 1, 0);

        if (frame.contains(win) && !isJustInner) {
            // 不需要Fit
            return dHoming;
        }

        // 宽高都小于Win，才有必要放大
        if (isJustInner || frame.width() < win.width() && frame.height() < win.height()) {
            dHoming.scale = Math.min(win.width() / frame.width(), win.height() / frame.height());
        }

        RectF rect = new RectF();
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        if (rect.width() < win.width()) {
            dHoming.x += win.centerX() - rect.centerX();
        } else {
            if (rect.left > win.left) {
                dHoming.x += win.left - rect.left;
            } else if (rect.right < win.right) {
                dHoming.x += win.right - rect.right;
            }
        }

        if (rect.height() < win.height()) {
            dHoming.y += win.centerY() - rect.centerY();
        } else {
            if (rect.top > win.top) {
                dHoming.y += win.top - rect.top;
            } else if (rect.bottom < win.bottom) {
                dHoming.y += win.bottom - rect.bottom;
            }
        }

        return dHoming;
    }

    public static EditHoming fillHoming(RectF win, RectF frame) {
        EditHoming dHoming = new EditHoming(0, 0, 1, 0);
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming;
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.scale = Math.max(win.width() / frame.width(), win.height() / frame.height());
        }

        RectF rect = new RectF();
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        if (rect.left > win.left) {
            dHoming.x += win.left - rect.left;
        } else if (rect.right < win.right) {
            dHoming.x += win.right - rect.right;
        }

        if (rect.top > win.top) {
            dHoming.y += win.top - rect.top;
        } else if (rect.bottom < win.bottom) {
            dHoming.y += win.bottom - rect.bottom;
        }

        return dHoming;
    }

    public static EditHoming fillHoming(RectF win, RectF frame, float pivotX, float pivotY) {
        EditHoming dHoming = new EditHoming(0, 0, 1, 0);
        if (frame.contains(win)) {
            // 不需要Fill
            return dHoming;
        }

        if (frame.width() < win.width() || frame.height() < win.height()) {
            dHoming.scale = Math.max(win.width() / frame.width(), win.height() / frame.height());
        }

        RectF rect = new RectF();
        M.setScale(dHoming.scale, dHoming.scale, pivotX, pivotY);
        M.mapRect(rect, frame);

        if (rect.left > win.left) {
            dHoming.x += win.left - rect.left;
        } else if (rect.right < win.right) {
            dHoming.x += win.right - rect.right;
        }

        if (rect.top > win.top) {
            dHoming.y += win.top - rect.top;
        } else if (rect.bottom < win.bottom) {
            dHoming.y += win.bottom - rect.bottom;
        }

        return dHoming;
    }

    public static EditHoming fill(RectF win, RectF frame) {
        EditHoming dHoming = new EditHoming(0, 0, 1, 0);

        if (win.equals(frame)) {
            return dHoming;
        }

        // 第一次时缩放到裁剪区域内
        dHoming.scale = Math.max(win.width() / frame.width(), win.height() / frame.height());

        RectF rect = new RectF();
        M.setScale(dHoming.scale, dHoming.scale, frame.centerX(), frame.centerY());
        M.mapRect(rect, frame);

        dHoming.x += win.centerX() - rect.centerX();
        dHoming.y += win.centerY() - rect.centerY();

        return dHoming;
    }

    public static int inSampleSize(int rawSampleSize) {
        int raw = rawSampleSize, ans = 1;
        while (raw > 1) {
            ans <<= 1;
            raw >>= 1;
        }

        if (ans != rawSampleSize) {
            ans <<= 1;
        }

        return ans;
    }

    public static void rectFill(RectF win, RectF frame) {
        if (win.equals(frame)) {
            return;
        }

        float scale = Math.max(win.width() / frame.width(), win.height() / frame.height());

        M.setScale(scale, scale, frame.centerX(), frame.centerY());
        M.mapRect(frame);

        if (frame.left > win.left) {
            frame.left = win.left;
        } else if (frame.right < win.right) {
            frame.right = win.right;
        }

        if (frame.top > win.top) {
            frame.top = win.top;
        } else if (frame.bottom < win.bottom) {
            frame.bottom = win.bottom;
        }
    }
}
