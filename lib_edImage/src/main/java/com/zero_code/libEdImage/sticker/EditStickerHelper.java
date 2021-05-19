package com.zero_code.libEdImage.sticker;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.View;

/**
 *
 * @author ZeroCode
 * @date 2021/5/17 : 14:16
 */

public class EditStickerHelper<StickerView extends View & EditSticker> implements
        EditStickerPortrait, EditStickerPortrait.Callback {

    private RectF mFrame;

    private StickerView mView;

    private Callback mCallback;

    private boolean isShowing = false;

    public EditStickerHelper(StickerView view) {
        mView = view;
    }

    @Override
    public boolean show() {
        if (!isShowing()) {
            isShowing = true;
            onShowing(mView);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove() {
        return onRemove(mView);
    }

    @Override
    public boolean dismiss() {
        if (isShowing()) {
            isShowing = false;
            onDismiss(mView);
            return true;
        }
        return false;
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public RectF getFrame() {
        if (mFrame == null) {
            mFrame = new RectF(0, 0, mView.getWidth(), mView.getHeight());
            float pivotX = mView.getX() + mView.getPivotX();
            float pivotY = mView.getY() + mView.getPivotY();

            Matrix matrix = new Matrix();
            matrix.setTranslate(mView.getX(), mView.getY());
            matrix.postScale(mView.getScaleX(), mView.getScaleY(), pivotX, pivotY);
            matrix.mapRect(mFrame);
        }
        return mFrame;
    }

    @Override
    public void onSticker(Canvas canvas) {
        // empty
    }

    @Override
    public void registerCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mCallback = null;
    }

    @Override
    public <V extends View & EditSticker> boolean onRemove(V stickerView) {
        return mCallback != null && mCallback.onRemove(stickerView);
    }

    @Override
    public <V extends View & EditSticker> void onDismiss(V stickerView) {
        mFrame = null;
        stickerView.invalidate();
        if (mCallback != null) {
            mCallback.onDismiss(stickerView);
        }
    }

    @Override
    public <V extends View & EditSticker> void onShowing(V stickerView) {
        stickerView.invalidate();
        if (mCallback != null) {
            mCallback.onShowing(stickerView);
        }
    }
}
