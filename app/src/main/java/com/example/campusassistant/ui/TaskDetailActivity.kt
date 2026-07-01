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
import com.example.campusassistant.ui.UserSessionManager
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
                        1 -> "待完成"
                        else -> "已完成"
                    }
                    
                    val currentUserId = UserSessionManager.getUserId(this@TaskDetailActivity)
                    val isPublisher = currentUserId == t.userId
                    
                    if (t.status == 0) {
                        if (isPublisher) {
                            btnAccept.isEnabled = false
                            btnAccept.text = "等待接单"
                            btnAccept.alpha = 0.7f
                        } else {
                            btnAccept.isEnabled = true
                            btnAccept.text = "点击接单"
                            btnAccept.alpha = 1.0f
                        }
                    } else if (t.status == 1) {
                        if (isPublisher) {
                            btnAccept.isEnabled = true
                            btnAccept.text = "确认完成（审核）"
                            btnAccept.alpha = 1.0f
                        } else {
                            btnAccept.isEnabled = false
                            btnAccept.text = "进行中/待完成"
                            btnAccept.alpha = 0.5f
                        }
                    } else {
                        btnAccept.isEnabled = false
                        btnAccept.text = "任务已完成"
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
            val currentUserId = UserSessionManager.getUserId(this)
            if (currentUserId == -1L) {
                Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            task?.let { t ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val isPublisher = currentUserId == t.userId
                    val updatedTask = when {
                        t.status == 0 && !isPublisher -> {
                            // 接单逻辑
                            t.copy(status = 1, acceptorId = currentUserId, acceptor = "校友") 
                        }
                        t.status == 1 && isPublisher -> {
                            // 审核完成逻辑
                            t.copy(status = 2)
                        }
                        else -> null
                    }

                    if (updatedTask != null) {
                        db.errandTaskDao().updateTask(updatedTask)
                        withContext(Dispatchers.Main) {
                            val msg = if (updatedTask.status == 1) "接单成功！" else "已确认任务完成"
                            Toast.makeText(this@TaskDetailActivity, msg, Toast.LENGTH_LONG).show()
                            loadData() // 刷新页面状态
                        }
                    }
                }
            }
        }
    }
}
