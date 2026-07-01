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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.campusassistant.aid.AppDatabase
import com.example.campusassistant.aid.PublishTaskActivity
import com.example.campusassistant.aid.EditTaskActivity
import com.example.campusassistant.R
import com.example.campusassistant.aid.Task
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

    private val db by lazy { AppDatabase.getInstance(requireContext()) }
    private var currentTabIndex = 0
    private var allTaskList: List<Task> = emptyList()
    private var currentShowTask: Task? = null

    private companion object {
        const val TAB_EXPRESS = 0
        const val TAB_TAKEOUT = 1
        const val TAB_SHOP = 2
        const val COLOR_NORMAL = "#333333"
        const val COLOR_SELECT = "#00C853"
    }

    // 广播接收器：接收发布/修改/删除后刷新信号（BroadcastReceiver组件）
    private val refreshReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PublishTaskActivity.ACTION_REFRESH_TASK) {
                loadTaskDataByTab(currentTabIndex)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_aid, container, false)
        initView(root)
        initClick()
        // 注册广播
        val filter = IntentFilter(PublishTaskActivity.ACTION_REFRESH_TASK)
        requireContext().registerReceiver(refreshReceiver, filter)
        // 初始化加载快递分类数据
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
        // 英雄广场
        rlHeroPlaza?.setOnClickListener {
            startActivity(Intent(requireActivity(), HeroPlazaActivity::class.java))
        }
        // 发布需求按钮（原校园认证按钮）
        rlAuth?.setOnClickListener {
            startActivity(Intent(requireActivity(), PublishTaskActivity::class.java))
        }
        // Tab切换
        llExpress?.setOnClickListener { selectTab(TAB_EXPRESS) }
        llTakeout?.setOnClickListener { selectTab(TAB_TAKEOUT) }
        llShop?.setOnClickListener { selectTab(TAB_SHOP) }
        // 点击需求卡片跳转编辑页面
        cardContent?.setOnClickListener {
            currentShowTask?.let { task ->
                val intent = Intent(requireContext(), EditTaskActivity::class.java)
                intent.putExtra("task_id", task.id)
                startActivity(intent)
            } ?: run {
                Toast.makeText(requireContext(), "暂无需求可编辑", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectTab(index: Int) {
        currentTabIndex = index
        llExpress?.let { setTabTextColor(it, COLOR_NORMAL) }
        llTakeout?.let { setTabTextColor(it, COLOR_NORMAL) }
        llShop?.let { setTabTextColor(it, COLOR_NORMAL) }
        when (index) {
            TAB_EXPRESS -> llExpress?.let { setTabTextColor(it, COLOR_SELECT) }
            TAB_TAKEOUT -> llTakeout?.let { setTabTextColor(it, COLOR_SELECT) }
            TAB_SHOP -> llShop?.let { setTabTextColor(it, COLOR_SELECT) }
        }
        loadTaskDataByTab(index)
    }

    // 根据Tab类型读取数据库数据
    private fun loadTaskDataByTab(tabIndex: Int) {
        val typeStr = when(tabIndex) {
            TAB_EXPRESS -> "快递"
            TAB_TAKEOUT -> "外卖"
            TAB_SHOP -> "代买"
            else -> "快递"
        }
        // 监听数据库数据流
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                db.taskDao().getTaskByType(typeStr).collect { list ->
                    allTaskList = list
                    if (list.isNotEmpty()) {
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
                        // 无数据时清空卡片
                        tvRecordCount?.text = "共0条记录"
                        tvTag?.text = ""
                        tvTitle?.text = "暂无该类型需求，快去发布吧！"
                        tvLocation?.text = ""
                        tvTimeLimit?.text = ""
                        tvMoney?.text = ""
                        currentShowTask = null
                    }
                }
            }
        }
    }

    private fun setTabTextColor(tabLayout: LinearLayout, colorHex: String) {
        val text = tabLayout.getChildAt(1) as TextView
        text.setTextColor(Color.parseColor(colorHex))
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

    // 页面销毁注销广播，防止内存泄漏
    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(refreshReceiver)
    }
}