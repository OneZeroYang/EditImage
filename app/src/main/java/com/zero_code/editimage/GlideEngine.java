package com.zero_code.editimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.huantansheng.easyphotos.engine.ImageEngine;


/**
 * @author：luck
 * @date：2019-11-13 17:02
 * @describe：Glide加载引擎
 */
public class GlideEngine implements ImageEngine {






    private GlideEngine() {
    }

    private static  GlideEngine instance;

    public static  GlideEngine createGlideEngine() {
        if (null == instance) {
            synchronized ( GlideEngine.class) {
                if (null == instance) {
                    instance = new  GlideEngine();
                }
            }
        }
        return instance;
    }

    @Override
    public void loadPhoto(@NonNull Context context, @NonNull Uri uri, @NonNull ImageView imageView) {
        Glide.with(context)
                .load(uri)
                .override(200, 200)
                .centerCrop()

                .into(imageView);
    }

    @Override
    public void loadGifAsBitmap(@NonNull Context context, @NonNull Uri gifUri, @NonNull ImageView imageView) {
        Glide.with(context)
                .load(gifUri)
                .override(200, 200)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public void loadGif(@NonNull Context context, @NonNull Uri gifUri, @NonNull ImageView imageView) {
        Glide.with(context)
                .load(gifUri)
                .override(200, 200)
                .centerCrop()
                .into(imageView);
    }

    @Override
    public Bitmap getCacheBitmap(@NonNull Context context, @NonNull Uri uri, int width, int height) throws Exception {
        return Glide.with(context).asBitmap().submit().get();
    }
}
