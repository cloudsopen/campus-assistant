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
import com.example.campusassistant.data.CampusMessage
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
            Toast.makeText(this, "错误：任务 ID 无效", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            task = db.errandTaskDao().getTaskById(taskId)
            withContext(Dispatchers.Main) {
                task?.let { bindTask(it) } ?: run {
                    Toast.makeText(this@TaskDetailActivity, "任务已不存在", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun bindTask(t: ErrandTask) {
        tvCategory.text = t.category
        tvTitle.text = t.title
        tvMoney.text = "¥${t.reward}"
        tvLocation.text = t.location ?: "暂无地点"
        tvTime.text = "截止：${t.deadline ?: "不限时"}"
        tvDesc.text = t.description
        tvPublisher.text = t.publisher ?: "匿名用户"
        tvStatus.text = when (t.status) {
            0 -> "待接单"
            1 -> "进行中"
            else -> "已完成"
        }

        btnAccept.isEnabled = t.status == 0
        btnAccept.alpha = if (t.status == 0) 1f else 0.5f
        btnAccept.text = if (t.status == 0) "接取任务" else "已被接单"
    }

    private fun initClick() {
        btnBack.setOnClickListener { finish() }

        btnAccept.setOnClickListener {
            val currentTask = task ?: return@setOnClickListener
            val currentUserId = UserSessionManager.getUserId(this)
            if (!UserSessionManager.isLoggedIn(this) || currentUserId <= 0L) {
                Toast.makeText(this, "请先登录后再接取任务", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (currentUserId == currentTask.userId) {
                Toast.makeText(this, "不能接取自己发布的任务", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val acceptorName = UserSessionManager.getDisplayName(this)
            lifecycleScope.launch(Dispatchers.IO) {
                val updatedTask = currentTask.copy(status = 1, acceptor = acceptorName)
                db.errandTaskDao().updateTask(updatedTask)
                if (currentTask.userId > 0L) {
                    db.campusMessageDao().insertMessage(
                        CampusMessage(
                            ownerUserId = currentTask.userId,
                            type = "task_accept",
                            title = "你发布的帖子被接取",
                            content = "$acceptorName 接取了你发布的「${currentTask.title}」，请及时查看并联系对方。",
                            relatedTitle = currentTask.title,
                            actorName = acceptorName
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TaskDetailActivity, "接单成功，已通知发布者", Toast.LENGTH_LONG).show()
                    loadData()
                }
            }
        }
    }
}
