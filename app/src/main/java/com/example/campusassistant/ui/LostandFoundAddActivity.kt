package com.example.campusassistant

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.LostItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.DatePickerDialog
import java.util.Calendar
import android.app.TimePickerDialog
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class LostandFoundAddActivity : AppCompatActivity() {

    // 用一个列表来存放图片的本地文件路径（已复制到内部存储，永久可读）
    private val selectedImagePaths = mutableListOf<String>()
    private var selectedTimeStr: String = ""

    // 将 content:// URI 复制到 App 内部存储，返回永久可读的本地文件路径
    private fun copyImageToInternal(uri: Uri): String? {
        return try {
            val dir = File(filesDir, "lost_images")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "img_${UUID.randomUUID()}.jpg")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            Toast.makeText(this, "图片保存失败: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun showDatePicker(tvSelectedTime: TextView) {
        val calendar = java.util.Calendar.getInstance()
        android.app.DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // 月份从 0 开始，所以要 +1
            showTimePicker(month + 1, dayOfMonth, tvSelectedTime)
        }, calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH)).show()
    }

    // 6. 弹出时间选择器
    private fun showTimePicker(month: Int, day: Int, tvSelectedTime: TextView) {
        val calendar = java.util.Calendar.getInstance()
        android.app.TimePickerDialog(this, android.R.style.Theme_Holo_Light_Dialog, { _, hourOfDay, minute ->

            // 🎉 用户选完全部时间了！在这里把结果组装成 String
            val timeResult = "${month}月${day}日 ${hourOfDay}:${String.format("%02d", minute)}"

            // 🌟 核心：给全局变量赋值，这样 btnSubmit 就能拿到了！
            selectedTimeStr = timeResult

            // 同时给前台 TextView 赋值，让用户能看见自己选的时间
            tvSelectedTime.text = "已选时间：$timeResult"

        }, calendar.get(java.util.Calendar.HOUR_OF_DAY), calendar.get(java.util.Calendar.MINUTE), true).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lostandfound_add_publish)

        val spinnerCategory: Spinner = findViewById(R.id.spinner_category)
        val etLocation: EditText = findViewById(R.id.et_location)
        val etDescription: EditText = findViewById(R.id.et_description)
        val btnSelectTime: Button = findViewById(R.id.btn_select_time)
        val tvSelectedTime: TextView = findViewById(R.id.tv_selected_time)
        val btnSelectImage: Button = findViewById(R.id.btn_select_image)
        val layoutImagePreview: LinearLayout = findViewById(R.id.layout_image_preview)
        val btnSubmit: Button = findViewById(R.id.btn_submit)


        // 1. 初始化分类 Spinner
        val categoryOptions = listOf("书籍", "电子产品", "随身物品", "贵重物品", "其他")
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryOptions)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = spinnerAdapter

        // 2. 核心：注册照片选择器的”大组件意图”契约
        // 这里直接限制最大张数为 5 张！
        val pickMultipleMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(5)) { uris ->
            if (uris.isNotEmpty()) {
                // 清空之前的选择
                selectedImagePaths.clear()
                layoutImagePreview.removeAllViews()

                // 遍历用户选中的每一张图片，复制到内部存储
                for (uri in uris) {
                    // 将临时 URI 复制到 App 内部目录，获取永久路径
                    val localPath = copyImageToInternal(uri)
                    if (localPath != null) {
                        selectedImagePaths.add(localPath)

                        // 动态创建 ImageView 并展示在预览区域（用本地路径显示）
                        val imageView = ImageView(this)
                        val params = LinearLayout.LayoutParams(160, LinearLayout.LayoutParams.MATCH_PARENT)
                        params.setMargins(0, 0, 16, 0) // 图片右边距
                        imageView.layoutParams = params
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        imageView.setImageURI(Uri.parse("file://$localPath"))

                        layoutImagePreview.addView(imageView)
                    }
                }
            } else {
                Toast.makeText(this, "未选择任何照片", Toast.LENGTH_SHORT).show()
            }
        }

        btnSelectTime.setOnClickListener {
            showDatePicker(tvSelectedTime)
        }

        // 3. 点击“从相册选择图片”按钮
        btnSelectImage.setOnClickListener {
            // 触发意图：只筛选图片文件
            pickMultipleMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        val lostItemDao = AppDatabase.getDatabase(this).lostItemDao()

        // 4. 点击提交按钮
        btnSubmit.setOnClickListener {
            val category = spinnerCategory.selectedItem.toString()
            val location = etLocation.text.toString().trim()
            val description = etDescription.text.toString().trim()
            val losttime = selectedTimeStr
            if (location.isEmpty() || description.isEmpty() || losttime.isEmpty()) {
                Toast.makeText(this, "请把信息（包括时间）填写完整哦！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val finalLocation = if (location.length > 20) location.take(20) else location

            // 将图片本地路径列表封装进数据模型
            val newItem = LostItem(
                publisher = null,
                location = finalLocation,
                category = category,
                description = description,
                losttime = losttime,
                imagePaths = if (selectedImagePaths.isEmpty()) null else selectedImagePaths.toList()
            )

            lifecycleScope.launch(Dispatchers.IO) {
                lostItemDao.insertItem(newItem)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LostandFoundAddActivity, "发布成功！包含 ${selectedImagePaths.size} 张照片", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}