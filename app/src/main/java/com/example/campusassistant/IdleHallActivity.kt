package com.example.campusassistant

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.CampusMessage
import com.example.campusassistant.data.IdleItem
import com.example.campusassistant.ui.UserSessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IdleHallActivity : AppCompatActivity() {
    private val db by lazy { AppDatabase.getDatabase(this) }
    private lateinit var placeholderLayout: LinearLayout
    private lateinit var itemListLayout: LinearLayout

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

        placeholderLayout = findViewById(R.id.layout_idle_placeholder)
        itemListLayout = findViewById(R.id.layout_idle_items)

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

    override fun onResume() {
        super.onResume()
        val selectedId = categoryIds.firstOrNull { findViewById<TextView>(it).currentTextColor == Color.WHITE }
            ?: R.id.chip_all
        loadIdleItems(categoryNames[selectedId] ?: "全部")
    }

    private fun updateSelectedCategory(selectedId: Int) {
        categoryIds.forEach { id ->
            val chip = findViewById<TextView>(id)
            val selected = id == selectedId
            chip.text = categoryNames[id]
            chip.setBackgroundResource(
                if (selected) R.drawable.bg_idle_chip_selected else R.drawable.bg_idle_chip_unselected
            )
            chip.setTextColor(getColor(if (selected) android.R.color.white else android.R.color.black))
        }

        val categoryName = categoryNames[selectedId] ?: "全部"
        findViewById<TextView>(R.id.tv_current_category).text = "当前分类：$categoryName"
        loadIdleItems(categoryName)
    }

    private fun loadIdleItems(categoryName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val items = if (categoryName == "全部") {
                db.idleItemDao().getAllItems()
            } else {
                db.idleItemDao().getItemsByCategory(categoryName)
            }
            withContext(Dispatchers.Main) {
                renderIdleItems(categoryName, items)
            }
        }
    }

    private fun renderIdleItems(categoryName: String, items: List<IdleItem>) {
        itemListLayout.removeAllViews()
        if (items.isEmpty()) {
            placeholderLayout.visibility = View.VISIBLE
            itemListLayout.visibility = View.GONE
            findViewById<TextView>(R.id.tv_idle_placeholder).text =
                "$categoryName 分类暂无闲置物品，可以先发布一件闲置。"
            return
        }

        placeholderLayout.visibility = View.GONE
        itemListLayout.visibility = View.VISIBLE
        items.forEach { item ->
            itemListLayout.addView(createIdleItemCard(item))
        }
    }

    private fun createIdleItemCard(item: IdleItem): View {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_idle_placeholder)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, dp(12))
            layoutParams = params
        }

        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        titleRow.addView(TextView(this).apply {
            text = item.title
            setTextColor(Color.parseColor("#111827"))
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        titleRow.addView(TextView(this).apply {
            text = "¥${item.price}"
            setTextColor(Color.parseColor("#F59E0B"))
            textSize = 18f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })

        card.addView(titleRow)
        card.addView(TextView(this).apply {
            text = item.description
            setTextColor(Color.parseColor("#4B5563"))
            textSize = 15f
            setPadding(0, dp(8), 0, 0)
        })
        card.addView(TextView(this).apply {
            text = "发布者：${item.publisher ?: "匿名用户"} · ${item.category}"
            setTextColor(Color.parseColor("#9CA3AF"))
            textSize = 13f
            setPadding(0, dp(8), 0, dp(12))
        })

        card.addView(TextView(this).apply {
            text = "购买"
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setBackgroundResource(R.drawable.bg_idle_chip_selected)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
            )
            setOnClickListener { buyIdleItem(item) }
        })

        return card
    }

    private fun buyIdleItem(item: IdleItem) {
        val currentUserId = UserSessionManager.getUserId(this)
        if (!UserSessionManager.isLoggedIn(this) || currentUserId <= 0L) {
            Toast.makeText(this, "请先登录后再购买", Toast.LENGTH_SHORT).show()
            return
        }
        if (currentUserId == item.userId) {
            Toast.makeText(this, "不能购买自己发布的闲置物品", Toast.LENGTH_SHORT).show()
            return
        }

        val buyerName = UserSessionManager.getDisplayName(this)
        lifecycleScope.launch(Dispatchers.IO) {
            if (item.userId > 0L) {
                db.campusMessageDao().insertMessage(
                    CampusMessage(
                        ownerUserId = item.userId,
                        type = "idle_buy",
                        title = "你发布的闲置物品被购买",
                        content = "$buyerName 购买了你发布的「${item.title}」，请及时联系对方完成交易。",
                        relatedTitle = item.title,
                        actorName = buyerName
                    )
                )
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(this@IdleHallActivity, "购买成功，已通知卖家", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    companion object {
        const val EXTRA_CATEGORY = "selected_category"
    }
}
