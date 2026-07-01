package com.example.campusassistant.aid

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.campusassistant.R

class PublishTaskActivity : AppCompatActivity() {
    private lateinit var spType: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etLocation: EditText
    private lateinit var etTime: EditText
    private lateinit var etMoney: EditText
    private lateinit var btnSubmit: Button
    private val db by lazy { AppDatabase.getInstance(this) }
    // 广播Action：发布/修改/删除需求后通知大厅刷新
    companion object {
        const val ACTION_REFRESH_TASK = "com.example.campusassistant.REFRESH_TASK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publish_task)
        initView()
        initClick()
    }

    private fun initView() {
        spType = findViewById(R.id.sp_type)
        etTitle = findViewById(R.id.et_title)
        etLocation = findViewById(R.id.et_location)
        etTime = findViewById(R.id.et_time)
        etMoney = findViewById(R.id.et_money)
        btnSubmit = findViewById(R.id.btn_submit)
    }

    private fun initClick() {
        btnSubmit.setOnClickListener {
            val type = spType.selectedItem.toString()
            val title = etTitle.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val time = etTime.text.toString().trim()
            val money = etMoney.text.toString().trim()

            // 简单非空校验
            if (title.isEmpty() || location.isEmpty() || time.isEmpty() || money.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 协程插入数据库
            lifecycleScope.launch(Dispatchers.IO) {
                val newTask = Task(type = type, title = title, location = location, timeLimit = time, money = "¥$money")
                db.taskDao().insertTask(newTask)
                // 切主线程弹窗
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PublishTaskActivity, "发布成功！", Toast.LENGTH_SHORT).show()
                    // 发送广播，通知AidFragment刷新列表
                    sendBroadcast(Intent(ACTION_REFRESH_TASK))
                    finish()
                }
            }
        }
    }
}