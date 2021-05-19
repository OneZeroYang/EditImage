# 🔥🔥🔥EditImage🔥🔥🔥

## 介绍

🔥🔥🔥基于canvas画布，实现的一款图片轻量级编辑框架，目前支持涂鸦、马赛克、剪裁、文字功能


![image](https://github.com/OneZeroYang/EdtiImageTools/blob/main/resource/051600390884_02b7403e8a39a37fa630e48cddd9909a.jpg)



## TODO
1. 小概率剪裁框偏差
2. 动画
3. 文字边框小概率缩小



## 集成

##### 1. 权限
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

在`AndroidManifest.xml`的`manifest`节点下添加：

如下:

````xml
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
````

注意：Android6.0及以上需要动态权限验证

````
private fun checkPermission() {
        //申请权限（Android6.0及以上 需要动态申请危险权限）
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //申请权限
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), checkPermissionCode
            )
        } else {
           // 获取权限成功
        }
    }
````

##### 2. 在项目根目录下`build.gradle`文件中 添加


maven { url 'https://jitpack.io' }


如下:
````
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
````



##### 3. 在`app`目录下`build.gradle`文件中添加

implementation 'com.github.OneZeroYang:EasyAccessibility:1.1.0'

如下:
````
dependencies {
	        implementation 'com.github.OneZeroYang:EasyAccessibility:1.1.0'
	}
````

## 注意

1. 项目本身最低版本为api19 android 4.4及以上（最低适配版本为4.4）
2. 如果要使用本库的UI，依赖项目必须开启dataBinding

在`app`的`build.gradle`下 `android` 节点下添加如下代码
````
 dataBinding {
        enabled = true
    }
````



## 使用

##### 1. 快速使用
kotlin:
api
````
/**
 * 开始编辑图片
 * @param src 资源 可以是本地图片  支持 url path
 * @param onError 错误的回调
 */
fun Activity.startEditImage(
    src: Any,
    requestCode: Int,
    onError: ((Exception) -> Unit)? = null
)

````

1. startEditImage(uri,requestCode)


2. 在activity实现`onActivityResult`方法

````
 override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestCode && resultCode == Activity.RESULT_OK) {
              val filePath = data?.getStringExtra("data")  //返回结果
        }
    }
````



注：目前快速使用仅支持kotlin，java请前往`定制化`


##### 2. 定制化

###### 基础

1. 核心`EditImageView`
    完全使用自定义`view`实现

2. 模式
    NONE,       //默认
    DOODLE,     //涂鸦
    MOSAIC,     //马赛克
    CLIP,       //剪裁
    ARROWS      //箭头

3. 文字/表情/贴图
    EditStickerView

###### 开始

1. 新建一个Activity/Fragment

2. 在布局文件里面添加`EditImageView`并初始化

````
  <com.zero_code.libEdImage.EditImageView
            android:id="@+id/mEditView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="#000" />
````
注意：`EditImageView` 得 `background` 必须为全黑色不透明

3. 初始化控件

````
val mEditView : EditImageView? =null
mEditView=findViewById(R.id.mEditView)
````

4. 设置编辑模式
````
 mEditView.mode = EditImageMode.NONE//预览模式
 mEditView.mode = EditImageMode.DOODLE//涂鸦模式
 mEditView.mode = EditImageMode.MOSAIC//马塞克模式
 mEditView.mode = EditImageMode.CLIP//裁剪模式
 mEditView.mode = EditImageMode.ARROWS//箭头模式

````

注意：当需要添加自定义控件得时候，需要设置模式为预览模式，并调用`addStickerView`,详情见`添加自定义View`

5. 预览模式

当模式为`EditImageMode.NONE` 时
预览模式仅支持图片得预览和添加自定义View,包括移动，放大缩小

6. 涂鸦

当模式为` EditImageMode.DOODLE` 时

设置涂鸦颜色：

````
mEditView.setPenColor(color)
````

撤销上一步涂鸦

````
mEditView.undoDoodle()
````

7. 马赛克

当模式为` EditImageMode.MOSAIC` 时

设置马赛克大小

````
 mEditView.setMosaicWidth(size)
````


撤销上一步马赛克

````
mEditView.undoMosaic()
````

马赛克大小预设0.2f-0.6f之间

````
    public static final  Float IMG_MOSAIC_SIZE_1=0.2F;
    public static final  Float IMG_MOSAIC_SIZE_2=0.3F;
    public static final  Float IMG_MOSAIC_SIZE_3=0.4F;
    public static final  Float IMG_MOSAIC_SIZE_4=0.5F;
    public static final  Float IMG_MOSAIC_SIZE_5=0.6F;

````

8. 裁剪

当模式为` EditImageMode.CLIP` 时

裁剪模式不仅包含裁剪，同时还支持旋转和镜面翻转

旋转


````
mEditView.doRotate(-90f) //left 旋转90°
mEditView.doRotate(90f)  //Right 旋转90°
````



镜面翻转

````
mEditView.doVerticalMirror()     //垂直镜面反转
mEditView.doHorizontalMirror() //水平镜像反转
````

取消裁剪

````
mEditView.cancelClip()
````

重置裁剪

````
mEditView.resetClip()
````

注意：重置裁剪只会对裁剪部分重置，不会对翻转/旋转生效，对翻转/旋转生效需要自行实现，请参考`com.zero_code.libEdImage.ui.EditImageActivity`源码


确认裁剪

````
mEditView.doClip()
````

9. 箭头模式

当模式为` EditImageMode.ARROWS` 时

设置箭头大小

````
mEditView.setArrowsSize(size)
````
注意：箭头大小内置预设为5个等级 1-5

设置箭头颜色

````
mEditView.setArrowsColor(color)
````

撤销上一步箭头

````
 mEditView.clearLastArrows()
````

10. 对保存编辑搞得图片保存为bitmap

````
mEditView.mEditView.saveBitmap()
````


## 添加自定义View

请自行下载源码查看

## 其他api

请自行下载源码查看


## 混淆

````
# editImage
-keep public class com.zero_code.libEdImage.EditImageView
-keep public class com.zero_code.libEdImage.core.EditImageMode
-keep class com.zero_code.libEdImage.widget.** { *; }
-keep class com.zero_code.libEdImage.view.** { *; }
````

## 关于
如在使用过程中出现任何问题或者BUG，欢迎各位小伙伴提供宝贵得意见
如果您喜欢这个项目，动动您得小手帮忙点一个star,您的支持，是我前进的最大的动力
QQ：1819405190（添加时，备注 "EditImageView"）



## 测试

测试机型 vivo iooq neo 855版本
Android 10
内存8G

1. 自测基本没有致命性异常
2. 内存根据图片分辨率、大小有所不同，目前编辑3264x2448 分辨率图片最大内存峰值在600M左右，连续快速旋转、翻转会有一定的内存抖动，但不影响正常使用
3. 使用Android Studio自带内存检测工具没有发现内存泄露的情况

## 感谢
felix 核心实现参考


