package com.example.campusassistant.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.R
import com.example.campusassistant.ApiService
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.PublishIdleActivity
import com.example.campusassistant.PublishBuyActivity
import com.example.campusassistant.PublishCarpoolActivity
import com.example.campusassistant.PublishErrandActivity
import com.example.campusassistant.PublishLostActivity
import com.example.campusassistant.ui.UserSessionManager
import com.example.campusassistant.ui.AuthActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PublishFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_publish, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initClickListeners(view)

        // “查看全部”点击事件
        view.findViewById<View>(R.id.tv_view_all_publish).setOnClickListener {
            startActivity(Intent(requireContext(), MyPublishHistoryActivity::class.java))
        }

        // 每日一句逻辑
        val tvDailyQuote = view.findViewById<TextView>(R.id.tv_daily_quote)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://v1.hitokoto.cn/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getDailyQuote()
                tvDailyQuote.text = "“${response.hitokoto}” —— 出自《${response.from}》"
            } catch (e: Exception) {
                Log.e("NetworkError", "请求网络失败", e)
                tvDailyQuote.text = "今天也要加油鸭！（网络加载失败）"
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // 关键点：每次页面回到前台（例如发布完成后返回）时，刷新数据
        view?.let {
            initDatabaseCounts(it)
            initMyPublishList(it)
        }
    }

    private fun initMyPublishList(view: View) {
        val rvMyPublish = view.findViewById<RecyclerView>(R.id.rv_my_publish)
        val layoutEmpty = view.findViewById<View>(R.id.layout_empty_publish)
        
        rvMyPublish.layoutManager = LinearLayoutManager(requireContext())
        
        val currentUserId = UserSessionManager.getUserId(requireContext())
        if (currentUserId == -1L) {
            rvMyPublish.visibility = View.GONE
            layoutEmpty.visibility = View.VISIBLE
            return
        }

        val db = AppDatabase.getDatabase(requireContext())
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val records = mutableListOf<PublishRecord>()
                
                // 1. 闲置
                val idleItems = db.idleItemDao().getItemsByUserId(currentUserId)
                Log.d("PublishFragment", "Found ${idleItems.size} idle items")
                idleItems.forEach { 
                    records.add(PublishRecord(it.id, "闲置", it.title, it.publishTime, color = 0xFFFFB300.toInt())) 
                }
                
                // 2. 求购
                val buyRequests = db.buyRequestDao().getRequestsByUserId(currentUserId)
                Log.d("PublishFragment", "Found ${buyRequests.size} buy requests")
                buyRequests.forEach { 
                    records.add(PublishRecord(it.id, "求购", it.title, it.publishTime, color = 0xFF9575CD.toInt())) 
                }
                
                // 3. 拼车
                val carpoolInfos = db.carpoolInfoDao().getInfoByUserId(currentUserId)
                Log.d("PublishFragment", "Found ${carpoolInfos.size} carpool infos")
                carpoolInfos.forEach { 
                    records.add(PublishRecord(it.id, "拼车", "${it.departure} -> ${it.destination}", it.publishTime, color = 0xFF26A69A.toInt())) 
                }
                
                // 4. 跑腿
                val errandTasks = db.errandTaskDao().getTasksByUserId(currentUserId)
                Log.d("PublishFragment", "Found ${errandTasks.size} errand tasks")
                errandTasks.forEach { 
                    val statusText = when(it.status) {
                        0 -> "待接单"
                        1 -> "待完成"
                        2 -> "已完成"
                        else -> "已发布"
                    }
                    records.add(PublishRecord(it.id, "跑腿", it.title, it.publishTime, statusText, color = 0xFF42A5F5.toInt())) 
                }
                
                // 5. 寻物
                val lostItems = db.lostItemDao().getItemsByUserId(currentUserId)
                Log.d("PublishFragment", "Found ${lostItems.size} lost items")
                lostItems.forEach { 
                    records.add(PublishRecord(it.id, "寻物", it.title, it.publishTime, color = 0xFFEF5350.toInt()))
                }

                // 按时间倒序排序
                records.sortByDescending { it.time }

                if (records.isEmpty()) {
                    rvMyPublish.visibility = View.GONE
                    layoutEmpty.visibility = View.VISIBLE
                } else {
                    rvMyPublish.visibility = View.VISIBLE
                    layoutEmpty.visibility = View.GONE
                    val displayRecords = if (records.size > 5) records.take(5) else records
                    rvMyPublish.adapter = PublishRecordAdapter(displayRecords) { record ->
                        if (record.type == "跑腿") {
                            val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                            intent.putExtra("task_id", record.id)
                            startActivity(intent)
                        } else {
                            android.widget.Toast.makeText(requireContext(), "${record.type}详情开发中", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("PublishFragment", "Error loading my publish list", e)
            }
        }
    }

    private fun initClickListeners(view: View) {
        view.findViewById<View>(R.id.layout_publish_idle).setOnClickListener {
            checkLoginAndNavigate(PublishIdleActivity::class.java)
        }
        view.findViewById<View>(R.id.layout_publish_buy).setOnClickListener {
            checkLoginAndNavigate(PublishBuyActivity::class.java)
        }
        view.findViewById<View>(R.id.layout_publish_carpool).setOnClickListener {
            checkLoginAndNavigate(PublishCarpoolActivity::class.java)
        }
        view.findViewById<View>(R.id.layout_publish_errand).setOnClickListener {
            checkLoginAndNavigate(PublishErrandActivity::class.java)
        }
        view.findViewById<View>(R.id.layout_publish_lost).setOnClickListener {
            checkLoginAndNavigate(PublishLostActivity::class.java)
        }
    }

    private fun checkLoginAndNavigate(targetActivity: Class<*>) {
        if (UserSessionManager.isLoggedIn(requireContext())) {
            startActivity(Intent(requireContext(), targetActivity))
        } else {
            android.widget.Toast.makeText(requireContext(), "请先登录后再发布内容", android.widget.Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }
    }

    private fun initDatabaseCounts(view: View) {
        val tvCountIdle = view.findViewById<TextView>(R.id.tv_count_idle)
        val tvCountBuy = view.findViewById<TextView>(R.id.tv_count_buy)
        val tvCountCarpool = view.findViewById<TextView>(R.id.tv_count_carpool)
        val tvCountErrand = view.findViewById<TextView>(R.id.tv_count_errand)
        val tvCountLost = view.findViewById<TextView>(R.id.tv_count_lost)

        val db = AppDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val idleCount = db.idleItemDao().getCount()
                val buyCount = db.buyRequestDao().getCount()
                val carpoolCount = db.carpoolInfoDao().getCount()
                val errandCount = db.errandTaskDao().getCount()
                val lostCount = db.lostItemDao().getCount()

                tvCountIdle.text = idleCount.toString()
                tvCountBuy.text = buyCount.toString()
                tvCountCarpool.text = carpoolCount.toString()
                tvCountErrand.text = errandCount.toString()
                tvCountLost.text = lostCount.toString()
            } catch (e: Exception) {
                Log.e("PublishFragment", "Error loading database counts", e)
            }
        }
    }
}