package com.example.campusassistant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.LostItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 1. 类名更改为 LostandFoundAddActivity
class LostandFoundAddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 2. 核心：这里更改为你新命名的布局文件 lostandfound_add_publish
        setContentView(R.layout.lostandfound_add_publish)

        val etCategory: EditText = findViewById(R.id.et_category)
        val etLocation: EditText = findViewById(R.id.et_location)
        val etDescription: EditText = findViewById(R.id.et_description)
        val btnSubmit: Button = findViewById(R.id.btn_submit)

        val lostItemDao = AppDatabase.getDatabase(this).lostItemDao()

        btnSubmit.setOnClickListener {
            val category = etCategory.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (category.isEmpty() || location.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "请把信息填写完整哦！", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val finalLocation = if (location.length > 20) location.take(20) else location

            val newItem = LostItem(
                publisher = null,
                location = finalLocation,
                category = category,
                description = description,
                imagePaths = null
            )

            lifecycleScope.launch(Dispatchers.IO) {
                lostItemDao.insertItem(newItem)

                withContext(Dispatchers.Main) {
                    // 3. 上下文提示更改为 this@LostandFoundAddActivity
                    Toast.makeText(this@LostandFoundAddActivity, "发布成功！数据已入库", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}