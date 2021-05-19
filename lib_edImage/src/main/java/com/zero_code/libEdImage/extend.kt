package com.zero_code.libEdImage

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.zero_code.libEdImage.core.EditImageArrows
import com.zero_code.libEdImage.ui.EditImageActivity
import com.zero_code.libEdImage.view.EditColorGroup
import com.zero_code.libEdImage.view.PathArrows
import java.io.File
import java.io.FileOutputStream
import java.lang.reflect.Array
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * 扩展方法
 * @author ZeroCode
 * @date 2021/5/8 : 13:46
 */


/**
 * 开始编辑图片
 * @param src 资源 可以是一个文件路径 也可以是一个 uri
 * @param requestCode 返回辨识
 */
fun Activity.startEditImage(
    src: String,
    requestCode: Int,
    onError: ((Exception) -> Unit)? = null
) {
    val intent = Intent(this, EditImageActivity::class.java)
    if (src.toString().isNullOrBlank()) {
        onError?.invoke(RuntimeException("资源错误"))
    }
    if (src.toString()!!.startsWith("http") || src.startsWith("https")) {
        onError?.invoke(RuntimeException("资源错误，目前不支持网络图片"))
        return
    }
    intent.putExtra("data", src.toString())
    this.startActivityForResult(intent, requestCode)
}


/**
 * 开始编辑图片
 * @param src 资源 可以是一个文件路径 也可以是一个 uri 也可以是一个 文件
 * @param requestCode 返回辨识
 */
fun Activity.startEditImage(
    src: Any,
    requestCode: Int,
    onError: ((Exception) -> Unit)? = null
) {
    when (src) {
        is String -> startEditImage(src.toString(), requestCode, onError)
        is File -> startEditImage((src as? File)?.absolutePath ?: "", requestCode, onError)
        is Uri -> startEditImage(src.path ?: "", requestCode, onError)
        else -> throw java.lang.RuntimeException("暂时不支持改资源类型")
    }
}

/**
 * 开始编辑图片
 *
 * @param src 资源 可以是本地图片  目前只支持 uri
 * @param onError 错误的回调
 */
@Deprecated(
    "请使用" +
            "startEditImage(\n" +
            "    src: Any,\n" +
            "    requestCode: Int,\n" +
            "    onError: ((Exception) -> Unit)? = null\n" +
            ")"
)
fun Activity.startEditImage(
    src: Uri,
    requestCode: Int,
    onError: ((Exception) -> Unit)? = null
) {
    val intent = Intent(this, EditImageActivity::class.java)
    if (src.toString().isNullOrBlank()) {
        onError?.invoke(RuntimeException("资源错误"))
    }
    if (src.toString()!!.startsWith("http")) {
        onError?.invoke(RuntimeException("资源错误，目前不支持网络图片"))
        return
    }
    when (src.toString()) {  //考虑后期会增加多种类型
        is String -> intent.putExtra("data", src.toString())
        else -> {
            onError?.invoke(RuntimeException("资源错误，目前只支持 String"))
            return
        }
    }
    this.startActivityForResult(intent, requestCode)
}


/**
 * 设置 setChangeListener
 */
fun EditColorGroup.setChangeListener(callBack: (RadioGroup) -> Unit) {
    this.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
        callBack.invoke(radioGroup)
    }
}


/**
 * // 绘制这个三角形,你可以绘制任意多边形
Path path = new Path();
path.moveTo(80, 200);// 此点为多边形的起点
path.lineTo(120, 250);
path.lineTo(80, 250);
path.close(); // 使这些点构成封闭的多边形
canvas.drawPath(path, p);

 */
fun Canvas.triangle(
    it: PathArrows
) {
    EditImageArrows.path.reset()
    drawArrow(it.start.x, it.start.y, it.end.x, it.end.y, it.size, it.mPaint, EditImageArrows.path)
}

/**
 * 画箭头
 *
 * @param sx
 * @param sy
 * @param ex
 * @param ey
 * @param paint
 */
private fun Canvas.drawArrow(
    sx: Float,
    sy: Float,
    ex: Float,
    ey: Float,
    width: Int,
    paint: Paint,
    triangle: Path
) {
    var size = 5
    var count = 20
    when (width) {
        1 -> {
            size = 5
            count = 20
        }
        2 -> {
            size = 8
            count = 30
        }
        3 -> {
            size = 11
            count = 40
        }
        4 -> {
            size = 15
            count = 50
        }
        5 -> {
            size = 20
            count = 60
        }
    }
    val x = ex - sx
    val y = ey - sy
    val d = x * x + y * y.toDouble()
    val r = sqrt(d)
    val zx = (ex - count * x / r).toFloat()
    val zy = (ey - count * y / r).toFloat()
    val xz = zx - sx
    val yz = zy - sy
    val zd = xz * xz + yz * yz.toDouble()
    val zr = sqrt(zd)

    triangle.moveTo(sx, sy)
    triangle.lineTo((zx + size * yz / zr).toFloat(), (zy - size * xz / zr).toFloat())
    triangle.lineTo(
        (zx + size * 2 * yz / zr).toFloat(),
        (zy - size * 2 * xz / zr).toFloat()
    )
    triangle.lineTo(ex, ey)
    triangle.lineTo(
        (zx - size * 2 * yz / zr).toFloat(),
        (zy + size * 2 * xz / zr).toFloat()
    )
    triangle.lineTo((zx - size * yz / zr).toFloat(), (zy + size * xz / zr).toFloat())
    triangle.close()
    drawPath(triangle, paint)
}


/**
 * 保存 Btimap 到本地
 * @param rec contentResolver
 * @param bitmap 资源
 * @param name 要保存为得资源 名字
 * @param description 文件名
 * @return 文件得路径 如果为null  或者  字符串 则保存失败
 */
fun Activity.saveBitMap(
    rec: ContentResolver = this.contentResolver,
    bitmap: Bitmap,
    name: String,
    description: String = "Edit_image"
): String {
    return MediaStore.Images.Media.insertImage(rec, bitmap, name, description)
}


/**
 * 保存 Btimap 到本地
 * @param rec contentResolver
 * @param bitmap 资源
 * @param name 要保存为得资源 名字
 * @param description 文件名
 * @return 文件得路径 如果为null  或者  字符串 则保存失败
 */
fun androidx.fragment.app.Fragment.saveImg(
    rec: ContentResolver = this.activity!!.contentResolver,
    bitmap: Bitmap,
    name: String,
    description: String = "Edit_image"
): String {
    return this.activity!!.saveBitMap(
        rec = rec,
        bitmap = bitmap,
        name = name,
        description = description
    )
}

/**
 * 通过LayoutId便捷获取ViewDataBinding的方法
 * 主要还是为了在 Fragment 里面写R.layout.xxxxxx,方便点击跳转
 */
fun <T : ViewDataBinding> androidx.fragment.app.Fragment.getDataBinding(
    @LayoutRes id: Int,
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null
): T {
    val binding =
        DataBindingUtil.inflate<T>(LayoutInflater.from(context), id, parent, attachToParent)
    binding.lifecycleOwner = this
    return binding
}

/**
 * 通过LayoutId便捷获取ViewDataBinding的方法
 * 主要还是为了在 Activity 里面写R.layout.xxxxxx,方便点击跳转
 */
fun <T : ViewDataBinding> AppCompatActivity.getDataBinding(
    @LayoutRes id: Int,
    parent: ViewGroup? = null,
    attachToParent: Boolean = parent != null
): T {
    val binding = DataBindingUtil.inflate<T>(layoutInflater, id, parent, attachToParent)
    binding.lifecycleOwner = this
    return binding
}

/**
 * 加载 uri 为 bitmap
 */
fun Context.loadBitmap(src: String): Bitmap {
    when {
        src.startsWith("content://") -> {   //是一个uri
            val parse = Uri.parse(src)
            val uriToFile = uriToFile(this, parse)
            if (uriToFile != null) return BitmapFactory.decodeFile(uriToFile.absolutePath) else throw RuntimeException(
                "传递资源文件错误"
            )
        }
        src.endsWith(".png") -> {           //是一个 .png 文件
            return BitmapFactory.decodeFile(src) ?: throw  RuntimeException(
                "传递资源文件错误"
            )
        }
        src.endsWith(".JPEG") -> {           //是一个 .JPEG 文件
            return BitmapFactory.decodeFile(src) ?: throw  RuntimeException(
                "传递资源文件错误"
            )
        }
//        src.endsWith(".webp") -> {           //是一个 .webp 文件
//            return BitmapFactory.decodeFile(src) ?: throw  RuntimeException(
//                "传递资源文件错误"
//            )
//        }
        src.endsWith(".jpg") -> {           //是一个 .jpg 文件
            return BitmapFactory.decodeFile(src) ?: throw  RuntimeException(
                "传递资源文件错误"
            )
        }
        else -> throw java.lang.RuntimeException("不支持的图片格式")

    }

}


fun uriToFile(context: Context, uri: Uri) =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        uriToFileQ(context, uri)
    } else uriToFileN(context, uri)


@RequiresApi(Build.VERSION_CODES.Q)
private fun uriToFileQ(context: Context, uri: Uri): File? =
    if (uri.scheme == ContentResolver.SCHEME_FILE)
        uri.toFile()
    else if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        //把文件保存到沙盒
        val contentResolver = context.contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val ois = context.contentResolver.openInputStream(uri)
                val displayName =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                ois?.let {
                    File(
                        context.externalCacheDir!!.absolutePath,
                        "${Random.nextInt(0, 9999)}$displayName"
                    ).apply {
                        val fos = FileOutputStream(this)
                        android.os.FileUtils.copy(ois, fos)
                        fos.close()
                        it.close()
                    }
                }
            } else null

        }

    } else null

@SuppressLint("ServiceCast")
private fun uriToFileN(context: Context, uri: Uri): File? {
    val authority = uri.authority
    val scheme = uri.scheme
    val path = uri.path
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && path != null) {
        val externals = arrayOf("/external", "/external_path")
        externals.forEach {
            if (path.startsWith(it + "/")) {
                val file = File(
                    Environment.getExternalStorageDirectory().absolutePath + path.replace(it, "")
                )
                if (file.exists()) {
                    return file
                }
            }
        }
    }
    if (scheme == ContentResolver.SCHEME_FILE)
        return uri.toFile()
    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(
            context,
            uri
        )
    ) {
        return if ("com.android.externalstorage.documents" == authority) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            if ("primary".equals(type, ignoreCase = true)) {
                return File(
                    Environment.getExternalStorageDirectory()
                        .toString() + "/" + split[1]
                )
            } else {
                val mStorageManager =
                    context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
                val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
                val getVolumeList = mStorageManager.javaClass.getMethod("getVolumeList")
                val getUuid = storageVolumeClazz.getMethod("getUuid")
                val getState = storageVolumeClazz.getMethod("getState")
                val getPath = storageVolumeClazz.getMethod("getPath")
                val isPrimary = storageVolumeClazz.getMethod("isPrimary")
                val isEmulated = storageVolumeClazz.getMethod("isEmulated")
                val result = getVolumeList.invoke(mStorageManager)
                val length = Array.getLength(result)
                for (i in 0 until length) {
                    val storageVolumeElement = Array.get(result, i)
                    val mounted = Environment.MEDIA_MOUNTED == getState.invoke(storageVolumeElement)
                            || Environment.MEDIA_MOUNTED_READ_ONLY == getState.invoke(
                        storageVolumeElement
                    )

                    if (!mounted) continue
                    if (isPrimary.invoke(storageVolumeElement) as Boolean
                        && isEmulated.invoke(storageVolumeElement) as Boolean
                    ) continue

                    val uuid = getUuid.invoke(storageVolumeElement) as String
                    if (uuid == type) {
                        return File(
                            getPath.invoke(storageVolumeElement).toString() + "/" + split[1]
                        )
                    }
                }

            }
            null
        } else if ("com.android.providers.downloads.documents" == authority) {
            val id = DocumentsContract.getDocumentId(uri)
            if (!TextUtils.isEmpty(id)) {
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getFileFromUri(context, contentUri)
            }
            null
        } else if ("com.android.providers.media.documents" == authority) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":").toTypedArray()
            val type = split[0]
            val contentUri: Uri
            contentUri = if ("image" == type) {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if ("video" == type) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else if ("audio" == type) {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            } else return null
            val selection = "_id=?"
            val selectionArgs = arrayOf(split[1])
            getFileFromUri(context, contentUri, selection, selectionArgs)
        } else if (ContentResolver.SCHEME_CONTENT == scheme)
            getFileFromUri(context, uri)
        else
            null

    }

    return null
}


private fun getFileFromUri(
    context: Context, uri: Uri, selection: String? = null,
    selectionArgs: kotlin.Array<String>? = null
): File? =
    context.contentResolver.query(uri, arrayOf("_data"), selection, selectionArgs, null)?.let {
        if (it.moveToFirst()) {
            it.getColumnIndex(MediaStore.Images.Media.DATA).let { index ->
                if (index == -1) null else File(it.getString(index))
            }
        } else null
    }


