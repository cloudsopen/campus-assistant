package com.example.campusassistant

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.CarpoolInfo
import com.example.campusassistant.ui.AuthActivity
import com.example.campusassistant.ui.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishCarpoolActivity : AppCompatActivity() {

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

        setContentView(R.layout.activity_publish_carpool)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btn_publish).setOnClickListener {
            val departure = findViewById<EditText>(R.id.et_departure).text.toString().trim()
            val destination = findViewById<EditText>(R.id.et_destination).text.toString().trim()
            val timeStr = findViewById<EditText>(R.id.et_time).text.toString().trim()
            val seatsStr = findViewById<EditText>(R.id.et_seats).text.toString().trim()
            val priceStr = findViewById<EditText>(R.id.et_price).text.toString().trim()
            val desc = findViewById<EditText>(R.id.et_description).text.toString().trim()

            if (departure.isEmpty() || destination.isEmpty() || timeStr.isEmpty() || seatsStr.isEmpty()) {
                Toast.makeText(this, "请完善拼车信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val seats = seatsStr.toIntOrNull() ?: 0
            val price = priceStr.toDoubleOrNull() ?: 0.0
            // 为简化，这里暂时直接存当前时间戳，实际应解析 timeStr
            val departureTimestamp = System.currentTimeMillis() 

            lifecycleScope.launch(Dispatchers.IO) {
                val info = CarpoolInfo(
                    userId = com.example.campusassistant.ui.UserSessionManager.getUserId(this@PublishCarpoolActivity),
                    departure = departure,
                    destination = destination,
                    departureTime = departureTimestamp,
                    seats = seats,
                    price = price,
                    description = desc,
                    publisher = com.example.campusassistant.ui.UserSessionManager.getDisplayName(this@PublishCarpoolActivity)
                )
                db.carpoolInfoDao().insertInfo(info)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PublishCarpoolActivity, "拼车信息发布成功！", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
