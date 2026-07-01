package com.example.campusassistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.R
import com.example.campusassistant.data.AppDatabase
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

class MyPublishHistoryActivity : AppCompatActivity() {

    private lateinit var rvHistory: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var tabLayout: TabLayout
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    
    private val allRecords = mutableListOf<PublishRecord>()
    private var currentType = "全部"
    private var currentQuery = ""
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_publish_history)

        initView()
        loadAllData()
    }

    private fun initView() {
        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }
        rvHistory = findViewById(R.id.rv_history)
        layoutEmpty = findViewById(R.id.layout_empty)
        tabLayout = findViewById(R.id.tab_layout)
        searchView = findViewById(R.id.search_view)

        rvHistory.layoutManager = LinearLayoutManager(this)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentType = tab?.text.toString()
                applyFilter()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentQuery = query ?: ""
                applyFilter()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentQuery = newText ?: ""
                applyFilter()
                return true
            }
        })
    }

    private fun loadAllData() {
        val currentUserId = UserSessionManager.getUserId(this)
        if (currentUserId == -1L) {
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                allRecords.clear()
                
                // 1. 闲置
                db.idleItemDao().getItemsByUserId(currentUserId).forEach {
                    allRecords.add(PublishRecord(it.id, "闲置", it.title, it.publishTime, color = 0xFFFFB300.toInt()))
                }
                
                // 2. 求购
                db.buyRequestDao().getRequestsByUserId(currentUserId).forEach {
                    allRecords.add(PublishRecord(it.id, "求购", it.title, it.publishTime, color = 0xFF9575CD.toInt()))
                }

                // 3. 拼车
                db.carpoolInfoDao().getInfoByUserId(currentUserId).forEach {
                    allRecords.add(PublishRecord(it.id, "拼车", "${it.departure} -> ${it.destination}", it.publishTime, color = 0xFF26A69A.toInt()))
                }

                // 4. 跑腿
                db.errandTaskDao().getTasksByUserId(currentUserId).forEach {
                    val statusText = when(it.status) {
                        0 -> "待接单"
                        1 -> "待完成"
                        2 -> "已完成"
                        else -> "已发布"
                    }
                    allRecords.add(PublishRecord(it.id, "跑腿", it.title, it.publishTime, statusText, color = 0xFF42A5F5.toInt()))
                }

                // 5. 寻物
                db.lostItemDao().getItemsByUserId(currentUserId).forEach {
                    allRecords.add(PublishRecord(it.id, "寻物", it.title, it.publishTime, color = 0xFFEF5350.toInt()))
                }

                allRecords.sortByDescending { it.time }
                applyFilter()

            } catch (e: Exception) {
                Toast.makeText(this@MyPublishHistoryActivity, "加载数据失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyFilter() {
        var filteredList = if (currentType == "全部") {
            allRecords
        } else {
            allRecords.filter { it.type == currentType }
        }

        if (currentQuery.isNotEmpty()) {
            filteredList = filteredList.filter { 
                it.title.contains(currentQuery, ignoreCase = true) 
            }
        }

        updateUI(filteredList)
    }

    private fun updateUI(list: List<PublishRecord>) {
        if (list.isEmpty()) {
            rvHistory.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
        } else {
            rvHistory.visibility = View.VISIBLE
            layoutEmpty.visibility = View.GONE
            rvHistory.adapter = PublishRecordAdapter(list) { record ->
                if (record.type == "跑腿") {
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", record.id)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "${record.type}详情开发中", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
