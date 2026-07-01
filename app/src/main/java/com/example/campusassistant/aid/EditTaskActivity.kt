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
class EditTaskActivity : AppCompatActivity() {
    private lateinit var spType: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etLocation: EditText
    private lateinit var etTime: EditText
    private lateinit var etMoney: EditText
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button
    private var targetTask: Task? = null
    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)
        initView()
        // 接收卡片传递过来的需求id
        val taskId = intent.getIntExtra("task_id", -1)
        if (taskId != -1) loadTaskData(taskId)
        initClick()
    }

    private fun initView() {
        spType = findViewById(R.id.sp_type)
        etTitle = findViewById(R.id.et_title)
        etLocation = findViewById(R.id.et_location)
        etTime = findViewById(R.id.et_time)
        etMoney = findViewById(R.id.et_money)
        btnUpdate = findViewById(R.id.btn_update)
        btnDelete = findViewById(R.id.btn_delete)
    }

    // 根据id查询数据，回填输入框
    private fun loadTaskData(taskId: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            targetTask = db.taskDao().getTaskById(taskId)
            targetTask?.let { task ->
                withContext(Dispatchers.Main) {
                    // spinner选中对应类型
                    val typeList = resources.getStringArray(R.array.task_type_arr).toList()
                    spType.setSelection(typeList.indexOf(task.type))
                    etTitle.setText(task.title)
                    etLocation.setText(task.location)
                    etTime.setText(task.timeLimit)
                    // 去掉¥符号
                    etMoney.setText(task.money.replace("¥", ""))
                }
            }
        }
    }

    private fun initClick() {
        // 保存修改
        btnUpdate.setOnClickListener {
            val type = spType.selectedItem.toString()
            val title = etTitle.text.toString().trim()
            val location = etLocation.text.toString().trim()
            val time = etTime.text.toString().trim()
            val money = etMoney.text.toString().trim()
            if (title.isEmpty() || location.isEmpty() || time.isEmpty() || money.isEmpty()) {
                Toast.makeText(this, "信息不能为空", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            targetTask?.let { oldTask ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val updateTask = oldTask.copy(
                        type = type,
                        title = title,
                        location = location,
                        timeLimit = time,
                        money = "¥$money"
                    )
                    db.taskDao().updateTask(updateTask)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditTaskActivity, "修改成功", Toast.LENGTH_SHORT).show()
                        sendBroadcast(Intent(PublishTaskActivity.ACTION_REFRESH_TASK))
                        finish()
                    }
                }
            }
        }

        // 删除需求
        btnDelete.setOnClickListener {
            targetTask?.let { task ->
                lifecycleScope.launch(Dispatchers.IO) {
                    db.taskDao().deleteTask(task)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@EditTaskActivity, "已删除", Toast.LENGTH_SHORT).show()
                        sendBroadcast(Intent(PublishTaskActivity.ACTION_REFRESH_TASK))
                        finish()
                    }
                }
            }
        }
    }
}