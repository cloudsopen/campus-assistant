package com.example.campusassistant

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            navView.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }



        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        // 获取大橙色按钮的引用
        val fab: FloatingActionButton = findViewById(R.id.fab_add)
        fab.setOnClickListener {
            // 关键点 1：必须使用 navController 跳转，不要用 FragmentTransaction
            // 关键点 2：跳转的 ID (navigation_publish) 必须在 nav_graph.xml 中有对应定义的 fragment
            navController.navigate(R.id.navigation_publish)
        }

        // 关键点 3：添加监听，解决“点不回来”的问题
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_publish) {
                // 当跳转到“发布中心”时，强制让底部导航栏“取消选中”当前的任何项
                navView.menu.setGroupCheckable(0, true, false)
                for (i in 0 until navView.menu.size()) {
                    val item = navView.menu.getItem(i)
                    item.isChecked = false
                }
            } else {
                // 回到其他主标签页时，恢复正常的选中效果
                navView.menu.setGroupCheckable(0, true, true)
            }
        }

        // 1. 设置点击监听（处理从未选中到选中的情况）
        navView.setOnItemSelectedListener { item ->
            // 强制跳转，不论当前在哪
            navController.navigate(item.itemId)
            true
        }

        // 2. 设置重新选中监听（处理“点不动”的核心：已经选中了还要点的情况）
        navView.setOnItemReselectedListener { item ->
            // 如果当前在发布页，哪怕图标是选中的，点击也要强制跳回去
            if (navController.currentDestination?.id == R.id.navigation_publish) {
                navController.navigate(item.itemId)
            }
        }

        // 3. 优化大按钮点击
        fab.setOnClickListener {
            // 使用带配置的跳转，确保不会重复堆叠页面
            navController.navigate(R.id.navigation_publish)
        }


    }
}