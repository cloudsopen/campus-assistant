package com.example.campusassistant.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.R
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.ErrandTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AidFragment : Fragment() {

    private lateinit var rvTasks: RecyclerView
    private lateinit var layoutEmpty: View
    private lateinit var tvRecordCount: TextView
    
    // Categories
    private lateinit var catAll: LinearLayout
    private lateinit var catExpress: LinearLayout
    private lateinit var catTakeout: LinearLayout
    private lateinit var catShop: LinearLayout
    private lateinit var catErrand: LinearLayout

    private var adapter: ErrandTaskAdapter? = null
    private var currentCategory = "全部"
    
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_aid, container, false)
        initView(root)
        setupRecyclerView()
        initClick(root)
        refreshData()
        return root
    }

    private fun initView(root: View) {
        rvTasks = root.findViewById(R.id.rv_tasks)
        layoutEmpty = root.findViewById(R.id.layout_empty)
        tvRecordCount = root.findViewById(R.id.tv_record_count)
        
        catAll = root.findViewById(R.id.cat_all)
        catExpress = root.findViewById(R.id.cat_express)
        catTakeout = root.findViewById(R.id.cat_takeout)
        catShop = root.findViewById(R.id.cat_shop)
        catErrand = root.findViewById(R.id.cat_errand)
    }

    private fun setupRecyclerView() {
        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        adapter = ErrandTaskAdapter(emptyList()) { task ->
            val intent = Intent(requireContext(), TaskDetailActivity::class.java)
            intent.putExtra("task_id", task.id)
            startActivity(intent)
        }
        rvTasks.adapter = adapter
    }

    private fun initClick(root: View) {
        root.findViewById<View>(R.id.rl_hero_plaza).setOnClickListener {
            startActivity(Intent(requireContext(), HeroPlazaActivity::class.java))
        }

        catAll.setOnClickListener { switchTab("全部") }
        catExpress.setOnClickListener { switchTab("代取快递") }
        catTakeout.setOnClickListener { switchTab("外卖代取") }
        catShop.setOnClickListener { switchTab("代买代送") }
        catErrand.setOnClickListener { switchTab("代办事项") }
    }

    private fun switchTab(category: String) {
        if (currentCategory == category) return
        currentCategory = category
        
        updateCategoryUI()
        refreshData()
    }

    private fun updateCategoryUI() {
        val categories = listOf(catAll, catExpress, catTakeout, catShop, catErrand)
        val names = listOf("全部", "代取快递", "外卖代取", "代买代送", "代办事项")
        val activeColor = Color.parseColor("#00C853")
        val inactiveColor = Color.parseColor("#666666")
        val activeBg = Color.parseColor("#00C853")
        val inactiveBg = Color.parseColor("#EEEEEE")

        for (i in categories.indices) {
            val layout = categories[i]
            val icon = layout.getChildAt(0) as ImageView
            val text = layout.getChildAt(1) as TextView
            
            if (names[i] == currentCategory) {
                text.setTextColor(activeColor)
                text.paint.isFakeBoldText = true
                icon.backgroundTintList = android.content.res.ColorStateList.valueOf(activeBg)
                icon.setColorFilter(Color.WHITE)
            } else {
                text.setTextColor(inactiveColor)
                text.paint.isFakeBoldText = false
                icon.backgroundTintList = null
                icon.setBackgroundResource(0) // Clear background
                icon.setColorFilter(Color.parseColor("#999999"))
            }
        }
    }

    private fun refreshData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val tasks = if (currentCategory == "全部") {
                db.errandTaskDao().getTasksByStatus(0) // Only pending tasks
            } else {
                db.errandTaskDao().getPendingTasksByCategory(currentCategory)
            }
            
            withContext(Dispatchers.Main) {
                tvRecordCount.text = "${tasks.size} 条记录"
                if (tasks.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    rvTasks.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    rvTasks.visibility = View.VISIBLE
                    adapter?.updateData(tasks)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshData()
    }
}
