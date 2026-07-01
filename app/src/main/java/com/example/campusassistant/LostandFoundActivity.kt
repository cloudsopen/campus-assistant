package com.example.campusassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import androidx.lifecycle.lifecycleScope // 引入协程生命周期作用域
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.data.AppDatabase // 引入你的数据库
import com.example.campusassistant.data.LostItem
import com.example.campusassistant.data.LostItemDao  // 引入你的数据接口
import com.example.campusassistant.ui.NavAdapter      // 保持你写好的顶部导航适配器
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LostandFoundActivity : AppCompatActivity() {

    // 1. 声明真实的数据库操作 DAO 和 物品卡片列表的适配器
    private lateinit var lostItemDao: LostItemDao
    private lateinit var itemAdapter: LostItemAdapter

    // 2. 声明一个状态变量，用来记录当前用户点选的是哪一个分类（默认一进来是“全部”）
    private var currentCategory: String = "全部"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lost_and_found)

        // 返回按钮（与论坛模块风格一致）
        findViewById<TextView>(R.id.btn_back).setOnClickListener { finish() }

        // 3. 初始化 Room 数据库组件
        lostItemDao = AppDatabase.getDatabase(this).lostItemDao()

        // 4. 初始化【顶部水平滑动】的分类导航栏（保持你原本优秀的逻辑）
        val categoryList = listOf("全部", "书籍", "电子产品", "随身物品", "贵重物品", "其他")
        val rvCategories: RecyclerView = findViewById(R.id.rv_lost_categories)
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCategories.adapter = NavAdapter(categoryList) { selectedCategory ->
            // 当用户点击某一个分类时，不弹单纯的模拟框了，而是改变变量，并直接去查数据库
            currentCategory = selectedCategory
            loadDataFromDatabase()
        }

        // 5. 【新增】初始化【下方垂直滑动】的真实物品卡片列表
        val rvLostItems: RecyclerView = findViewById(R.id.rv_lost_items)
        rvLostItems.layoutManager = LinearLayoutManager(this) // 垂直小方块排列
        itemAdapter = LostItemAdapter(emptyList()) { clickedItem ->
            showDetailDialog(clickedItem)
        }
        rvLostItems.adapter = itemAdapter

        // 6. 悬浮加号按钮跳转逻辑（保持你原本的逻辑）
        val fabAddLost: FloatingActionButton = findViewById(R.id.fab_add_lost)
        fabAddLost.setOnClickListener {
            val intent = Intent(this, LostandFoundAddActivity::class.java)
            startActivity(intent)
        }
    }

    // 7. 【核心改造】：利用 Activity 的生命周期
    // 无论是刚打开页面，还是从“发布页面”成功保存返回，都会触发 onResume()，在这里刷新数据体验最好！
    override fun onResume() {
        super.onResume()
        loadDataFromDatabase() // 触发读取
    }

    // 8. 【核心改造】：取代你之前的模拟 filterLostItems 逻辑，实现真正的模板化数据读取
    private fun loadDataFromDatabase() {
        // 开启协程，派发给后台线程（Dispatchers.IO）去查本地数据库文件
        lifecycleScope.launch(Dispatchers.IO) {

            // 根据当前选中的分类标签，决定调用 DAO 里的哪一个查询规则
            val databaseResult = if (currentCategory == "全部") {
                lostItemDao.getAllItems() // 捞出全部
            } else {
                lostItemDao.getItemsByCategory(currentCategory) // 仅捞出对应分类
            }

            // 数据捞完了，切回主线程（Dispatchers.Main）去更新手机界面上的方块卡片
            withContext(Dispatchers.Main) {
                // 将最新查出来的数据库列表喂给卡片适配器，方块会瞬间刷新！
                itemAdapter.updateData(databaseResult)

                // 保留你的 Toast 提示习惯，可以清晰看到真实查出了多少条
                Toast.makeText(
                    this@LostandFoundActivity,
                    "已为您筛选出【$currentCategory】类真实物品 ${databaseResult.size} 件",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 9. 点击卡片后弹出弹窗，展示物品完整详情
    private fun showDetailDialog(item: LostItem) {
        try {
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_lost_item_detail, null)

            // 绑定各个控件
            val tvClose: TextView = view.findViewById(R.id.tv_dialog_close)
            val hsvImages: HorizontalScrollView = view.findViewById(R.id.hsv_images)
            val layoutImages: LinearLayout = view.findViewById(R.id.layout_detail_images)
            val tvCategory: TextView = view.findViewById(R.id.tv_detail_category)
            val tvDescription: TextView = view.findViewById(R.id.tv_detail_description)
            val tvLocation: TextView = view.findViewById(R.id.tv_detail_location)
            val tvLosttime: TextView = view.findViewById(R.id.tv_detail_losttime)
            val tvContact: TextView = view.findViewById(R.id.tv_detail_contact)
            val layoutPublisher: LinearLayout = view.findViewById(R.id.layout_detail_publisher)
            val tvPublisher: TextView = view.findViewById(R.id.tv_detail_publisher)

            // 填充图片：展示全部照片（带异常保护）
            if (!item.imagePaths.isNullOrEmpty()) {
                hsvImages.visibility = View.VISIBLE
                val density = resources.displayMetrics.density
                val imageWidthPx = (280 * density).toInt()
                val imageHeightPx = (200 * density).toInt()
                val marginPx = (12 * density).toInt()
                for (path in item.imagePaths) {
                    try {
                        val imageView = ImageView(this)
                        val params = LinearLayout.LayoutParams(imageWidthPx, imageHeightPx)
                        params.setMargins(0, 0, marginPx, 0)
                        imageView.layoutParams = params
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        // 兼容本地文件路径（新）和 content:// URI（旧数据）
                        val uri = if (path.startsWith("content://")) {
                            Uri.parse(path)
                        } else {
                            Uri.fromFile(File(path))
                        }
                        imageView.setImageURI(uri)
                        layoutImages.addView(imageView)
                    } catch (e: Exception) {
                        Log.e("LostDetail", "加载图片失败: $path", e)
                    }
                }
            } else {
                hsvImages.visibility = View.GONE
            }

            // 填充文字信息
            tvCategory.text = item.category
            tvDescription.text = item.description
            tvLocation.text = item.location
            tvLosttime.text = item.lost_time
            tvContact.text = item.contact_information

            // 发布者信息（如果有）
            if (!item.publisher.isNullOrBlank()) {
                layoutPublisher.visibility = View.VISIBLE
                tvPublisher.text = item.publisher
            } else {
                layoutPublisher.visibility = View.GONE
            }

            // 构建并显示 AlertDialog
            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create()

            // 关闭按钮
            tvClose.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        } catch (e: Exception) {
            Log.e("LostDetail", "弹窗崩溃", e)
            Toast.makeText(this, "打开详情失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}