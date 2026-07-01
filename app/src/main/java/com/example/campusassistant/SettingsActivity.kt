package com.example.campusassistant

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<ImageView>(R.id.btn_back_settings).setOnClickListener { finish() }
        findViewById<LinearLayout>(R.id.item_hero_auth).setOnClickListener {
            Toast.makeText(this, "英雄认证后续接入", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.item_about_us).setOnClickListener {
            Toast.makeText(this, "关于我们后续补充", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.item_customer_service).setOnClickListener {
            Toast.makeText(this, "联系客服：请联系管理员", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.item_privacy_policy).setOnClickListener {
            Toast.makeText(this, "用户协议与隐私政策后续接入", Toast.LENGTH_SHORT).show()
        }
    }
}
