package com.example.campusassistant

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.ui.NavAdapter // 确保引入了之前写的 NavAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LostandFoundActivity : AppCompatActivity() {

    // 模拟一些校园失物招领的数据：Pair("分类", "物品描述")
    private val lostItemsData = listOf(
        Pair("书籍", "高数上册，课本第5页写有‘张三’"),
        Pair("电子产品", "一期教学楼201拾到高配 iPad Pencil 根"),
        Pair("随身物品", "操场看台捡到一把黑色天堂晴雨伞"),
        Pair("贵重物品", "西门食堂二楼楼梯口拾到身份证和校园卡"),
        Pair("电子产品", "图书馆三楼拾到漫步者蓝牙耳机充电舱"),
        Pair("书籍", "考研英语词汇闪过，全新未做题"),
        Pair("其他", "篮球场西侧捡到一件白色耐克运动外套")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lost_and_found)

        // 1. 定义你要求的 6 个分类标签
        val categoryList = listOf("全部", "书籍", "电子产品", "随身物品", "贵重物品", "其他")

        // 2. 初始化顶部滑动导航栏
        val rvCategories: RecyclerView = findViewById(R.id.rv_lost_categories)
        // 设置水平排列（HORIZONTAL），这是能左右滑动的灵魂代码
        rvCategories.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        // 3. 绑定适配器并实现点击联动
        rvCategories.adapter = NavAdapter(categoryList) { selectedCategory ->
            // 当用户点击某一个分类（比如“电子产品”）时，会执行这里的筛选代码
            filterLostItems(selectedCategory)
        }

        // 4. 默认刚打开界面时显示“全部”物品
        filterLostItems("全部")

        // 在 LostandFoundActivity.kt 的 onCreate() 尾部修改：
        val fabAddLost: FloatingActionButton = findViewById(R.id.fab_add_lost)
        fabAddLost.setOnClickListener {
            // 目标页面已更改为 LostandFoundAddActivity
            val intent = Intent(this, LostandFoundAddActivity::class.java)
            startActivity(intent)
        }
    }



    // 核心筛选处理函数
    private fun filterLostItems(category: String) {
        // 根据点击的分类过滤出符合条件的数据
        val filteredList = if (category == "全部") {
            lostItemsData
        } else {
            lostItemsData.filter { it.first == category }
        }

        // 用 Toast 给你展示筛选成果，后续可以直接传给下方列表的 Adapter 刷新界面
        Toast.makeText(this, "已为您筛选出【$category】类物品 ${filteredList.size} 件", Toast.LENGTH_SHORT).show()

        // TODO: 这里将来写刷新下方列表的代码，例如：
        // lostItemsAdapter.submitList(filteredList)
    }
}