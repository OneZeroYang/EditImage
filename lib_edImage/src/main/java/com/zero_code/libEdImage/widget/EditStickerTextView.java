package com.zero_code.libEdImage.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.zero_code.libEdImage.ui.text.EditImageText;
import com.zero_code.libEdImage.ui.text.EditTextEditDialog;


/**
 *
 * @author ZeroCode
 * @date 2021/5/17 : 14:16
 */
public class EditStickerTextView extends EditStickerView implements EditTextEditDialog.Callback {

    private static final String TAG = "IMGStickerTextView";

    private TextView mTextView;

    private EditImageText mText;

    private EditTextEditDialog mDialog;

    private static float mBaseTextSize = -1f;

    private static final int PADDING = 26;

    private static final float TEXT_SIZE_SP = 24f;

    public EditStickerTextView(Context context) {
        this(context, null, 0);
    }

    public EditStickerTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EditStickerTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onInitialize(Context context) {
        if (mBaseTextSize <= 0) {
            mBaseTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    TEXT_SIZE_SP, context.getResources().getDisplayMetrics());
        }
        super.onInitialize(context);
    }

    @Override
    public View onCreateContentView(Context context) {
        mTextView = new TextView(context);
        mTextView.setTextSize(mBaseTextSize);
        mTextView.setPadding(PADDING, PADDING, PADDING, PADDING);
        mTextView.setTextColor(Color.WHITE);

        return mTextView;
    }

    public void setText(EditImageText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setTextColor(mText.getColor());
        }
    }

    public EditImageText getText() {
        return mText;
    }

    @Override
    public void onContentTap() {
        EditTextEditDialog dialog = getDialog();
        dialog.setText(mText);
        dialog.show();
    }

    private EditTextEditDialog getDialog() {
        if (mDialog == null) {
            mDialog = new EditTextEditDialog(getContext(), this);
        }
        return mDialog;
    }

    @Override
    public void onText(EditImageText text) {
        mText = text;
        if (mText != null && mTextView != null) {
            mTextView.setText(mText.getText());
            mTextView.setBackgroundColor(text.getBackgroundColor());
            mTextView.setTextColor(mText.getColor());
        }
    }
}