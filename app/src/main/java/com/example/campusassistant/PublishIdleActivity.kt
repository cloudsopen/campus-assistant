package com.example.campusassistant

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.IdleItem
import com.example.campusassistant.ui.AuthActivity
import com.example.campusassistant.ui.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishIdleActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 登录检查
        if (!UserSessionManager.isLoggedIn(this)) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            startActivity(android.content.Intent(this, AuthActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_publish_idle)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btn_publish).setOnClickListener {
            val title = findViewById<EditText>(R.id.et_title).text.toString().trim()
            val desc = findViewById<EditText>(R.id.et_description).text.toString().trim()
            val priceStr = findViewById<EditText>(R.id.et_price).text.toString().trim()
            val category = findViewById<Spinner>(R.id.sp_category).selectedItem.toString()

            if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "请完善物品信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val price = priceStr.toDoubleOrNull() ?: 0.0

            lifecycleScope.launch(Dispatchers.IO) {
                val item = IdleItem(
                    userId = com.example.campusassistant.ui.UserSessionManager.getUserId(this@PublishIdleActivity),
                    title = title,
                    description = desc,
                    price = price,
                    category = category,
                    publisher = com.example.campusassistant.ui.UserSessionManager.getDisplayName(this@PublishIdleActivity)
                )
                db.idleItemDao().insertItem(item)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PublishIdleActivity, "发布成功！", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
