package com.example.campusassistant

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.ErrandTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishErrandActivity : AppCompatActivity() {
    
    private val db by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_errand)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
        
        findViewById<TextView>(R.id.btn_publish).setOnClickListener {
            val title = findViewById<EditText>(R.id.et_title).text.toString().trim()
            val location = findViewById<EditText>(R.id.et_location).text.toString().trim()
            val desc = findViewById<EditText>(R.id.et_description).text.toString().trim()
            val rewardStr = findViewById<EditText>(R.id.et_reward).text.toString().trim()
            val deadline = findViewById<EditText>(R.id.et_deadline).text.toString().trim()
            
            if (title.isEmpty() || location.isEmpty() || desc.isEmpty() || rewardStr.isEmpty()) {
                Toast.makeText(this, "请完善任务信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val reward = rewardStr.toDoubleOrNull() ?: 0.0
            
            lifecycleScope.launch(Dispatchers.IO) {
                val task = ErrandTask(
                    userId = com.example.campusassistant.ui.UserSessionManager.getUserId(this@PublishErrandActivity),
                    title = title,
                    location = location,
                    description = desc,
                    reward = reward,
                    deadline = deadline,
                    category = "代办事项",
                    status = 0,
                    publisher = com.example.campusassistant.ui.UserSessionManager.getDisplayName(this@PublishErrandActivity)
                )
                db.errandTaskDao().insertTask(task)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PublishErrandActivity, "发布成功！", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
