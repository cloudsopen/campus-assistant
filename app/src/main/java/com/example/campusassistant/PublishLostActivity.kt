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
import com.example.campusassistant.data.LostItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishLostActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_lost)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btn_publish).setOnClickListener {
            val title = findViewById<EditText>(R.id.et_title).text.toString().trim()
            val location = findViewById<EditText>(R.id.et_location).text.toString().trim()
            val timeStr = findViewById<EditText>(R.id.et_time).text.toString().trim()
            val desc = findViewById<EditText>(R.id.et_description).text.toString().trim()
            val contact = findViewById<EditText>(R.id.et_contact).text.toString().trim()
            val category = findViewById<Spinner>(R.id.sp_lost_category).selectedItem.toString()

            if (title.isEmpty() || location.isEmpty() || contact.isEmpty()) {
                Toast.makeText(this, "请填写关键信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                val item = LostItem(
                    userId = com.example.campusassistant.ui.UserSessionManager.getUserId(this@PublishLostActivity),
                    title = title,
                    location = location,
                    category = category, // "寻物" 或 "拾物"
                    lost_time = timeStr,
                    description = desc,
                    contact_information = contact,
                    publisher = com.example.campusassistant.ui.UserSessionManager.getDisplayName(this@PublishLostActivity)
                )
                db.lostItemDao().insertItem(item)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PublishLostActivity, "发布成功！", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
