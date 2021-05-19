package com.zero_code.libEdImage.ui

import android.Manifest
import android.R.attr.data
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager

import com.zero_code.libEdImage.*
import com.zero_code.libEdImage.adapter.BaseAdapter
import com.zero_code.libEdImage.core.EditImageMode
import com.zero_code.libEdImage.ui.text.EditImageText
import com.zero_code.libEdImage.databinding.ActivityEditImageBinding
import com.zero_code.libEdImage.databinding.ItemEditImageToolsLayoutBinding
import com.zero_code.libEdImage.model.ClipViewModel
import com.zero_code.libEdImage.util.StatusBarUtil
import com.zero_code.libEdImage.view.EditImageToolsBean
import com.zero_code.libEdImage.view.EditMosaicGroup.*
import kotlinx.android.synthetic.main.activity_edit_image.*
import kotlinx.android.synthetic.main.image_arrows_layout.*
import kotlinx.android.synthetic.main.image_color_layout.*
import kotlinx.android.synthetic.main.image_mosaic_layout.*


/**
 * 编辑图片activity
 * @author ZeroCode
 * @date 2021/5/8 : 11:09
 */

//TODO 1.剪裁模块，小概率还原得时候图片定位不在中心点得问题，会导致旋转中心也不会在中心点得位置，目前触发机制还不明确

class EditImageActivity : AppCompatActivity() {


    companion object {
        private const val checkPermissionCode: Int = 0xf11
    }

    /**
     * 资源
     */
    private val src by lazy {
        intent.getStringExtra("data")
    }

    /**
     * 选中的工具下标
     */
    private var selectedToolsIndex = -1


    /**
     * 工具数据
     */
    private val toolsList = arrayListOf(
        EditImageToolsBean(                     // 画笔
            intArrayOf(
                R.mipmap.btn_ed_freed,
                R.mipmap.btn_ed_free
            )
        ),
        EditImageToolsBean(                  // 马赛克
            intArrayOf(
                R.mipmap.btn_ed_mosaiced,
                R.mipmap.btn_ed_mosaic
            )
        ),
        EditImageToolsBean(                  // 剪裁
            intArrayOf(
                R.mipmap.btn_ed_cuted,
                R.mipmap.btn_ed_cut
            )
        ),
        EditImageToolsBean(                  // 文字
            intArrayOf(
                R.mipmap.btn_ed_texted,
                R.mipmap.btn_ed_text
            )
        ),
        EditImageToolsBean(                     // 箭头
            intArrayOf(
                R.mipmap.btn_ed_arrowed,
                R.mipmap.btn_ed_arrow
            )
        )
    )


    /**
     * 编辑工具的适配器
     */
    private val toolsAdapter by lazy {
        BaseAdapter<EditImageToolsBean, ItemEditImageToolsLayoutBinding>(
            R.layout.item_edit_image_tools_layout,
            toolsList
        ).apply {
            onBind { itemBingding, position, data ->

                itemBingding.itemToolsImage.setImageResource(data.getImage())


                itemBingding.root.setOnClickListener {
                    if (cg_colors.visibility == View.VISIBLE) cg_colors.visibility = View.GONE
                    if (cg_mosaic.visibility == View.VISIBLE) cg_mosaic.visibility = View.GONE
                    if (cg_arrows.visibility == View.VISIBLE) cg_arrows.visibility = View.GONE
                    ed_tools.visibility = View.VISIBLE
                    when (position) {
                        0 -> {                           // 涂鸦
                            mIMGView.mode = EditImageMode.DOODLE
                            cg_colors.visibility = View.VISIBLE
                            if (cg_colors.checkedRadioButtonId == null) cg_colors.checkColor =
                                resources.getColor(R.color.image_color_red)
                        }
                        1 -> {                           // 马赛克
                            mIMGView.mode = EditImageMode.MOSAIC
                            cg_mosaic.visibility = View.VISIBLE
                            if (cg_mosaic.checkSize == IMG_MOSAIC_SIZE_NO) cg_mosaic.checkSize =
                                IMG_MOSAIC_SIZE_1
                        }
                        2 -> {                           // 剪裁
                            mIMGView.mode = EditImageMode.CLIP
                            ed_tools.visibility = View.GONE
                            clip_view.visibility = View.VISIBLE
//                            mIMGView?.resetClip()
                        }
                        3 -> {                           // 文字
                            mIMGView.mode = EditImageMode.NONE
                            mIMGView.addStickerText(
                                EditImageText(
                                    "文字",
                                    Color.RED
                                )
                            )
                        }
                        4 -> {                           // 箭头
                            mIMGView.mode = EditImageMode.ARROWS
                            cg_arrows.visibility = View.VISIBLE
                            if (cg_arrows.checkSize == IMG_MOSAIC_SIZE_NO) cg_arrows.checkSize =
                                IMG_MOSAIC_SIZE_1
                        }
                    }
                    if (selectedToolsIndex != -1) toolsList[selectedToolsIndex].isSelected =
                        false
                    data.isSelected = true
                    selectedToolsIndex = position
                    notifyDataSetChanged()
                }
            }
        }
    }


    private val binding by lazy {
        getDataBinding<ActivityEditImageBinding>(R.layout.activity_edit_image)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        this.window.setFlags(
//            WindowManager.LayoutParams.FLAG_FULLSCREEN,
//            WindowManager.LayoutParams.FLAG_FULLSCREEN
//        )
        setContentView(binding.root)
        binding.clipViewModel = ClipViewModel(
            lifecycle,
            mIMGView,
            binding
        )
        if (src.isNullOrBlank()) throw RuntimeException("请指定资源和返回标识！")
        checkPermission()
    }


    fun complete(view: View) {
        val saveBitmap = mIMGView.saveBitmap()
        val path = saveBitMap(
            bitmap = saveBitmap,
            name = System.currentTimeMillis().toString(),
            description = "batchat_app_image"
        )
        if (path.isNullOrBlank()) {
            throw java.lang.RuntimeException("保存文件错误！")
        }
        saveBitmap.recycle()
        intent.putExtra("data", path)
        setResult(Activity.RESULT_OK, intent)
        finish()


    }

    fun cancel(view: View) {
        setResult(Activity.RESULT_CANCELED, intent)
        finish()

    }





    /**
     * 检查权限
     */
    private fun checkPermission() {
        //申请权限（Android6.0及以上 需要动态申请危险权限）
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), checkPermissionCode
            )
        } else {
            initView()
        }
    }

    /**
     * 权限申请回掉
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == checkPermissionCode) {
            if (permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initView()
            } else {
                Toast.makeText(this, "权限已拒绝", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    /**
     * 初始化View
     */
    private fun initView() {
        //初始化状态栏
        changeStatusBarColor(resources.getColor(R.color.ed_image_title_bar_bg_color), false)
        //初始化工具栏
        edit_image_tools_list.apply {
            layoutManager =
                LinearLayoutManager(this@EditImageActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = toolsAdapter
        }
        //适配状态兰入侵
        status_bar.apply {
            val lp = layoutParams
            lp.height = StatusBarUtil.getStatusBarHeight(context)
            layoutParams = lp
        }
        //初始化编辑View

        val bitmap = loadBitmap(src!!)
        mIMGView.setImageBitmap(bitmap)

        //设置涂鸦颜色选择监听
        cg_colors.setChangeListener {
            mIMGView.setPenColor(cg_colors.checkColor)
        }

        //设置马赛克大小
        cg_mosaic.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            mIMGView.setMosaicWidth(cg_mosaic.checkSize)
        }

        //设置箭头大小
        cg_arrows.setOnCheckedChangeListener { radioGroup: RadioGroup, i: Int ->
            when (cg_arrows.checkSize) {
                IMG_MOSAIC_SIZE_1 -> mIMGView.setArrowsSize(1)
                IMG_MOSAIC_SIZE_2 -> mIMGView.setArrowsSize(2)
                IMG_MOSAIC_SIZE_3 -> mIMGView.setArrowsSize(3)
                IMG_MOSAIC_SIZE_4 -> mIMGView.setArrowsSize(4)
                IMG_MOSAIC_SIZE_5 -> mIMGView.setArrowsSize(5)
            }
        }
        //设置返回上一步
        edit_image_last_step.setOnClickListener {
            val mode: EditImageMode? = mIMGView.mode
            if (mode === EditImageMode.DOODLE) {
                mIMGView.undoDoodle()
            } else if (mode === EditImageMode.MOSAIC) {
                mIMGView.undoMosaic()
            } else if (mode == EditImageMode.ARROWS) {
                mIMGView.clearLastArrows()
            }
        }

        //初始化 剪裁相关得View
        initClipView()


    }


    override fun onDestroy() {
        mIMGView.getImage()?.recycle()
        System.gc()
        System.gc()
        super.onDestroy()
    }

    /**
     * 初始化 剪裁相关得View
     */
    private fun initClipView() {

    }


    /**
     * 改变状态栏颜色
     * @param color
     * @param isCilp 是否需要padding状态栏高度，如果需要自己实现状态栏逻辑就传入false
     * @param dl 如果要兼容DrawerLayout则传入
     */
    private fun changeStatusBarColor(
        @ColorInt color: Int,
        isCilp: Boolean = true,
        dl: androidx.drawerlayout.widget.DrawerLayout? = null
    ) {
        //如果dl不为空则都使用半透明，因为dl可能拉出白色背景
        if (dl != null) {
            StatusBarUtil.setStatusBarLightMode(this, false)
            StatusBarUtil.setColorTranslucentForDrawerLayout(this, dl, color)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //如果版本号大于等于M，则必然可以修改状态栏颜色
            StatusBarUtil.setColor(this, color, isCilp)
            StatusBarUtil.setStatusBarLightModeByColor(this, color)
            return
        }
        //这里处理的是版本号低于M的系统
        //判断设置的颜色是深色还是浅色，然后设置statusBar的文字颜色
        val status = StatusBarUtil.setStatusBarLightModeByColor(this, color)
        //fixme 如果手机机型不能改状态栏颜色就不允许开启沉浸式,如果业务需求请修改代码逻辑
        if (!status) {//如果状态栏的文字颜色改变失败了则设置为半透明
            StatusBarUtil.setColorTranslucent(this, color, isCilp)
        } else {//如果状态栏的文字颜色改变成功了则设置为全透明
            StatusBarUtil.setColor(this, color, isCilp)
            //改变了状态栏后需要重新设置一下状态栏文字颜色
            StatusBarUtil.setStatusBarLightModeByColor(this, color)
        }

    }


}