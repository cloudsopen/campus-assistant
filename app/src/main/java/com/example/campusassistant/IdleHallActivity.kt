package com.example.campusassistant

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class IdleHallActivity : AppCompatActivity() {
    private val categoryIds = listOf(
        R.id.chip_all,
        R.id.chip_clothes,
        R.id.chip_books,
        R.id.chip_daily,
        R.id.chip_sports,
        R.id.chip_digital
    )

    private val categoryNames = mapOf(
        R.id.chip_all to "全部",
        R.id.chip_clothes to "服装鞋包",
        R.id.chip_books to "图书教材",
        R.id.chip_daily to "生活用品",
        R.id.chip_sports to "运动健身",
        R.id.chip_digital to "数码电器"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_idle_hall)

        findViewById<View>(R.id.btn_back_idle).setOnClickListener { finish() }
        findViewById<View>(R.id.idle_search_box).setOnClickListener {
            Toast.makeText(this, "搜索功能后续接入商品数据", Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.btn_publish_idle).setOnClickListener {
            startActivity(Intent(this, PublishIdleActivity::class.java))
        }

        categoryIds.forEach { id ->
            findViewById<TextView>(id).setOnClickListener { updateSelectedCategory(id) }
        }

        val initialCategory = intent.getStringExtra(EXTRA_CATEGORY)
        val targetId = categoryNames.entries.firstOrNull { it.value == initialCategory }?.key ?: R.id.chip_all
        updateSelectedCategory(targetId)
    }

    private fun updateSelectedCategory(selectedId: Int) {
        categoryIds.forEach { id ->
            val chip = findViewById<TextView>(id)
            val selected = id == selectedId
            chip.setBackgroundResource(
                if (selected) R.drawable.bg_idle_chip_selected else R.drawable.bg_idle_chip_unselected
            )
            chip.setTextColor(getColor(if (selected) android.R.color.white else android.R.color.black))
        }

        val categoryName = categoryNames[selectedId] ?: "全部"
        findViewById<TextView>(R.id.tv_current_category).text = "当前分类：$categoryName"
        findViewById<TextView>(R.id.tv_idle_placeholder).text =
            "$categoryName 分类的闲置物品后续会展示在这里，目前可以先完善顶部分类和发布流程。"
    }

    companion object {
        const val EXTRA_CATEGORY = "selected_category"
    }
}
