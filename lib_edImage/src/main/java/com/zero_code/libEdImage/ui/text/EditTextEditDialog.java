package com.zero_code.libEdImage.ui.text;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zero_code.libEdImage.R;
import com.zero_code.libEdImage.view.EditColorGroup;


/**
 *
 * @author ZeroCode
 * @date 2021/5/17 : 14:16
 */

public class EditTextEditDialog extends Dialog implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener, TextWatcher {

    private static final String TAG = "IMGTextEditDialog";

    private EditText mEditText;

    private int textBgColor = Color.TRANSPARENT;

    private Callback mCallback;

    private EditImageText mDefaultText;

    private EditColorGroup mColorGroup;

    private TextView mTextView;

    private ImageView imageEd;

    private Boolean isBackground = false;

    public EditTextEditDialog(Context context, Callback callback) {
        super(context, R.style.ImageTextDialog);
        setContentView(R.layout.image_text_dialog);
        mCallback = callback;
        Window window = getWindow();
        if (window != null) {
            window.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mColorGroup = findViewById(R.id.cg_colors);
        mTextView = findViewById(R.id.tv_text);
        mColorGroup.setVisibility(View.VISIBLE);
        mColorGroup.setOnCheckedChangeListener(this);
        mEditText = findViewById(R.id.et_text);
        imageEd = findViewById(R.id.image_is_ed_prospect);

        findViewById(R.id.tv_cancel).setOnClickListener(this);
        findViewById(R.id.tv_done).setOnClickListener(this);
        findViewById(R.id.tv_empty).setOnClickListener(this);
        imageEd.setOnClickListener(this);
        //R.color.title_font_color_right
        mEditText.addTextChangedListener(this);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mDefaultText != null) {
            mEditText.setText(mDefaultText.getText());
            mEditText.setTextColor(mDefaultText.getColor());
            mTextView.setTextColor(mDefaultText.getColor());
            if (!mDefaultText.isEmpty()) {
                mEditText.setSelection(mEditText.length());
            }
            mDefaultText = null;
        } else mEditText.setText("");


//        mColorGroup.setCheckColor(mEditText.getCurrentTextColor());

    }

    public void setText(EditImageText text) {
        mDefaultText = text;
    }

    public void reset() {
        setText(new EditImageText(null, Color.WHITE));
    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();

        if (vid == R.id.tv_done) {
            onDone();
        } else if (vid == R.id.tv_cancel) {
            dismiss();
        } else if (vid == R.id.tv_empty) {
            mEditText.setText("");
        } else if (vid == R.id.image_is_ed_prospect){
            isBackground=!isBackground;
            mColorGroup.setCheckColor(Color.RED);
        }
    }

    private void onDone() {
        String text = mEditText.getText().toString();
        if (mCallback != null) {
            EditImageText imgText = new EditImageText(text, mEditText.getCurrentTextColor());
            imgText.setBackgroundColor(textBgColor);
            mCallback.onText(imgText);
        }
        dismiss();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (!isBackground) {
            mEditText.setTextColor(mColorGroup.getCheckColor());
            mTextView.setTextColor(mColorGroup.getCheckColor());
        } else {
            textBgColor=mColorGroup.getCheckColor();
            String s = mTextView.getText().toString();
            SpannableStringBuilder style = new SpannableStringBuilder(s);
            style.setSpan(new BackgroundColorSpan(textBgColor), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextView.setText(style);
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        SpannableStringBuilder style = new SpannableStringBuilder(s);
        style.setSpan(new BackgroundColorSpan(textBgColor), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mTextView.setText(style);
    }

    public interface Callback {

        void onText(EditImageText text);
    }
}
