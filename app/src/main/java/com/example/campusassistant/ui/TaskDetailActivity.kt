package com.example.campusassistant.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.R
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.ErrandTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var tvCategory: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvTitle: TextView
    private lateinit var tvMoney: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvPublisher: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnBack: ImageButton

    private var taskId: Long = -1
    private var task: ErrandTask? = null
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_task)
        
        taskId = intent.getLongExtra("task_id", -1)
        
        initView()
        loadData()
        initClick()
    }

    private fun initView() {
        tvCategory = findViewById(R.id.tv_detail_category)
        tvStatus = findViewById(R.id.tv_detail_status)
        tvTitle = findViewById(R.id.tv_detail_title)
        tvMoney = findViewById(R.id.tv_detail_money)
        tvLocation = findViewById(R.id.tv_detail_location)
        tvTime = findViewById(R.id.tv_detail_time)
        tvDesc = findViewById(R.id.tv_detail_desc)
        tvPublisher = findViewById(R.id.tv_detail_publisher)
        btnAccept = findViewById(R.id.btn_accept_task)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun loadData() {
        if (taskId == -1L) {
            Toast.makeText(this, "错误：任务ID无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            task = db.errandTaskDao().getTaskById(taskId)
            withContext(Dispatchers.Main) {
                task?.let { t ->
                    tvCategory.text = t.category
                    tvTitle.text = t.title
                    tvMoney.text = "¥${t.reward}"
                    tvLocation.text = t.location ?: "暂无地点"
                    tvTime.text = "截止：${t.deadline ?: "不限时"}"
                    tvDesc.text = t.description
                    tvPublisher.text = t.publisher ?: "匿名用户"
                    
                    tvStatus.text = when(t.status) {
                        0 -> "待接单"
                        1 -> "进行中"
                        else -> "已完成"
                    }
                    
                    if (t.status != 0) {
                        btnAccept.isEnabled = false
                        btnAccept.text = "已被接单"
                        btnAccept.alpha = 0.5f
                    }
                } ?: run {
                    Toast.makeText(this@TaskDetailActivity, "任务已不存在", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun initClick() {
        btnBack.setOnClickListener { finish() }
        
        btnAccept.setOnClickListener {
            task?.let { t ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val updatedTask = t.copy(status = 1, acceptor = "当前用户") // 模拟当前用户
                    db.errandTaskDao().updateTask(updatedTask)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@TaskDetailActivity, "接单成功！请尽快完成", Toast.LENGTH_LONG).show()
                        loadData() // 刷新页面状态
                    }
                }
            }
        }
    }
}
