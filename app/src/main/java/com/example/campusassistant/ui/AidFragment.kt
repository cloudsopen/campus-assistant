package com.example.campusassistant.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.campusassistant.R
import com.example.campusassistant.aid.AppDatabase
import com.example.campusassistant.aid.EditTaskActivity
import com.example.campusassistant.aid.PublishTaskActivity
import com.example.campusassistant.aid.Task
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class AidFragment : Fragment() {
    private var rlHeroPlaza: View? = null
    private var rlAuth: View? = null

    private var llExpress: LinearLayout? = null
    private var llTakeout: LinearLayout? = null
    private var llShop: LinearLayout? = null

    private var tvRecordCount: TextView? = null
    private var tvTag: TextView? = null
    private var tvTitle: TextView? = null
    private var tvLocation: TextView? = null
    private var tvTimeLimit: TextView? = null
    private var tvMoney: TextView? = null
    private var cardContent: View? = null
    private var layoutEmpty: View? = null

    private val db by lazy { AppDatabase.getInstance(requireContext()) }
    private var currentTabIndex = 0
    private var allTaskList: List<Task> = emptyList()
    private var currentShowTask: Task? = null
    // 控制数据库监听协程，切换Tab时取消旧监听
    private var dataCollectJob: Job? = null
    // 广播接收器
    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PublishTaskActivity.ACTION_REFRESH_TASK) {
                loadTaskDataByTab(currentTabIndex)
            }
        }
    }

    private companion object {
        const val TAB_EXPRESS = 0
        const val TAB_TAKEOUT = 1
        const val TAB_SHOP = 2
        const val COLOR_NORMAL = "#333333"
        const val COLOR_SELECT = "#00C853"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_aid, container, false)
        initView(root)
        initClick()
        registerBroadcast()
        selectTab(TAB_EXPRESS)
        return root
    }

    private fun initView(root: View) {
        rlHeroPlaza = root.findViewById(R.id.rl_hero_plaza)
        rlAuth = root.findViewById(R.id.rl_auth)
        llExpress = root.findViewById(R.id.ll_express)
        llTakeout = root.findViewById(R.id.ll_takeout)
        llShop = root.findViewById(R.id.ll_shop)
        tvRecordCount = root.findViewById(R.id.tv_record_count)
        tvTag = root.findViewById(R.id.tv_tag)
        tvTitle = root.findViewById(R.id.tv_title)
        tvLocation = root.findViewById(R.id.tv_location)
        tvTimeLimit = root.findViewById(R.id.tv_time_limit)
        tvMoney = root.findViewById(R.id.tv_money)
        cardContent = root.findViewById(R.id.card_content)

    }

    private fun initClick() {
        rlHeroPlaza?.setOnClickListener {
            startActivity(Intent(requireActivity(), HeroPlazaActivity::class.java))
        }
        rlAuth?.setOnClickListener {
            startActivity(Intent(requireActivity(), PublishTaskActivity::class.java))
        }
        llExpress?.setOnClickListener { selectTab(TAB_EXPRESS) }
        llTakeout?.setOnClickListener { selectTab(TAB_TAKEOUT) }
        llShop?.setOnClickListener { selectTab(TAB_SHOP) }
        cardContent?.setOnClickListener {
            currentShowTask?.let { task ->
                val intent = Intent(requireContext(), EditTaskActivity::class.java)
                intent.putExtra("task_id", task.id)
                startActivity(intent)
            }
        }
    }

    private fun selectTab(index: Int) {
        currentTabIndex = index
        // 重置全部Tab文字颜色
        llExpress?.let { setTabTextColor(it, COLOR_NORMAL) }
        llTakeout?.let { setTabTextColor(it, COLOR_NORMAL) }
        llShop?.let { setTabTextColor(it, COLOR_SELECT) }
        // 选中Tab高亮
        when (index) {
            TAB_EXPRESS -> llExpress?.let { setTabTextColor(it, COLOR_SELECT) }
            TAB_TAKEOUT -> llTakeout?.let { setTabTextColor(it, COLOR_SELECT) }
            TAB_SHOP -> llShop?.let { setTabTextColor(it, COLOR_SELECT) }
        }
        loadTaskDataByTab(index)
    }

    private fun loadTaskDataByTab(tabIndex: Int) {
        val typeStr = when (tabIndex) {
            TAB_EXPRESS -> "快递"
            TAB_TAKEOUT -> "外卖"
            TAB_SHOP -> "代买"
            else -> "快递"
        }
        // 取消上一次数据监听，避免多流并发
        dataCollectJob?.cancel()
        dataCollectJob = lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                db.taskDao().getTaskByType(typeStr)
                    .distinctUntilChanged() // 仅数据变化才更新UI
                    .collect { list ->
                        allTaskList = list
                        if (list.isNotEmpty()) {
                            layoutEmpty?.visibility = View.GONE
                            cardContent?.visibility = View.VISIBLE
                            val first = list.first()
                            currentShowTask = first
                            updateContent(
                                tag = first.type,
                                count = "共${list.size}条记录",
                                title = first.title,
                                location = first.location,
                                time = first.timeLimit,
                                money = first.money
                            )
                        } else {
                            layoutEmpty?.visibility = View.VISIBLE
                            cardContent?.visibility = View.GONE
                            tvRecordCount?.text = "共0条记录"
                            currentShowTask = null
                        }
                    }
            }
        }
    }

    private fun setTabTextColor(tabLayout: LinearLayout, colorHex: String) {
        // 安全获取子View，避免强转崩溃
        val childCount = tabLayout.childCount
        for (i in 0 until childCount) {
            val child = tabLayout.getChildAt(i)
            if (child is TextView) {
                child.setTextColor(Color.parseColor(colorHex))
                break
            }
        }
    }

    private fun updateContent(
        tag: String,
        count: String,
        title: String,
        location: String,
        time: String,
        money: String
    ) {
        tvRecordCount?.text = count
        tvTag?.text = tag
        tvTitle?.text = title
        tvLocation?.text = location
        tvTimeLimit?.text = time
        tvMoney?.text = "报酬 $money"
    }

    // 单独封装广播注册，防止重复注册
    private fun registerBroadcast() {
        val filter = IntentFilter(PublishTaskActivity.ACTION_REFRESH_TASK)
        ContextCompat.registerReceiver(
            requireContext(),
            refreshReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onDestroyView() {
        // 取消数据监听协程
        dataCollectJob?.cancel()
        // 注销广播，防止内存泄漏
        try {
            requireContext().unregisterReceiver(refreshReceiver)
        } catch (e: IllegalArgumentException) {
            // 已注销则忽略异常
        }
        super.onDestroyView()
        // 清空视图引用，避免内存泄漏
        rlHeroPlaza = null
        rlAuth = null
        llExpress = null
        llTakeout = null
        llShop = null
        tvRecordCount = null
        tvTag = null
        tvTitle = null
        tvLocation = null
        tvTimeLimit = null
        tvMoney = null
        cardContent = null
        layoutEmpty = null
    }
}