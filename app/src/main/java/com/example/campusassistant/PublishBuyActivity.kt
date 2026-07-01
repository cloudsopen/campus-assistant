package com.example.campusassistant

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.BuyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PublishBuyActivity : AppCompatActivity() {

    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_buy)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }

        findViewById<TextView>(R.id.btn_publish).setOnClickListener {
            val title = findViewById<EditText>(R.id.et_title).text.toString().trim()
            val desc = findViewById<EditText>(R.id.et_description).text.toString().trim()
            val priceStr = findViewById<EditText>(R.id.et_price).text.toString().trim()

            if (title.isEmpty() || desc.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(this, "请完善求购需求", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val budget = priceStr.toDoubleOrNull() ?: 0.0

            lifecycleScope.launch(Dispatchers.IO) {
                val request = BuyRequest(
                    userId = com.example.campusassistant.ui.UserSessionManager.getUserId(this@PublishBuyActivity),
                    title = title,
                    description = desc,
                    budget = budget,
                    publisher = com.example.campusassistant.ui.UserSessionManager.getDisplayName(this@PublishBuyActivity)
                )
                db.buyRequestDao().insertRequest(request)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PublishBuyActivity, "求购发布成功！", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}
