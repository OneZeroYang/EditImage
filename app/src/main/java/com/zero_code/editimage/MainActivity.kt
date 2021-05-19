package com.zero_code.editimage

import android.R.attr
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.huantansheng.easyphotos.EasyPhotos
import com.huantansheng.easyphotos.models.album.entity.Photo
import com.zero_code.libEdImage.IS_DEBUG
import com.zero_code.libEdImage.startEditImage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.withContext


/**
 * 测试用的app
 * @author ZeroCode
 * @date 2021/5/18 : 11:34
 */
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val test =
        "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fyouimg1.c-ctrip.com%2Ftarget%2Ftg%2F035%2F063%2F726%2F3ea4031f045945e1843ae5156749d64c.jpg&refer=http%3A%2F%2Fyouimg1.c-ctrip.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=jpeg?sec=1623049590&t=d510aeb8f12616210691111148795c78".trim()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        launch {
//            val bitmap = getBitmap()
//            image_canvas.setImageBitmap(bitmap)
//            image_canvas.mode= IMGMode.MOSAIC
//        }


    }


    fun selectImage(view: View) {
        selectFromAlbum()
    }


    private suspend fun getBitmap(src: String): Bitmap {
        return withContext(Dispatchers.IO) {
            Glide.with(this@MainActivity)
                .asBitmap()
                .load(src)
                .submit().get()
        }
    }

    /**
     * 从相册选择
     */
    private fun selectFromAlbum() {
        EasyPhotos.createAlbum(
            this,
            false,
            true,
            GlideEngine.createGlideEngine()
        )//参数说明：上下文，是否显示相机按钮,是否使用宽高数据（false时宽高数据为0，扫描速度更快），[配置Glide为图片加载引擎](https://github.com/HuanTanSheng/EasyPhotos/wiki/12-%E9%85%8D%E7%BD%AEImageEngine%EF%BC%8C%E6%94%AF%E6%8C%81%E6%89%80%E6%9C%89%E5%9B%BE%E7%89%87%E5%8A%A0%E8%BD%BD%E5%BA%93)
            .start(12)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 22 && resultCode == Activity.RESULT_OK) {
            //表示返回成功
            val filePath = data?.getStringExtra("data")
            filePath?.let {
                if (IS_DEBUG) Log.e("", it)
                Glide.with(this).load(it).into(image)
            }

        } else if (requestCode == 12 && resultCode == Activity.RESULT_OK) {

            val resultPhotos: ArrayList<Photo> =
                data?.getParcelableArrayListExtra<Photo>(EasyPhotos.RESULT_PHOTOS) as ArrayList<Photo>


            if (resultPhotos.isNullOrEmpty()) {
                return
            }
            startEditImage(resultPhotos[0].path, 22)


//            launch {
//                val bitmap = getBitmap(selectPic.path)
//                image_canvas.setImageBitmap(bitmap)
//                image_canvas.mode = IMGMode.NONE
//            }
        }
    }
}