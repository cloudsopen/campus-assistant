package com.example.campusassistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.campusassistant.R
import com.example.campusassistant.PublishIdleActivity
import com.example.campusassistant.PublishBuyActivity
import com.example.campusassistant.PublishCarpoolActivity
import com.example.campusassistant.PublishErrandActivity
import com.example.campusassistant.PublishLostActivity

class PublishFragment : Fragment() {

    // 第一步：onCreateView 只负责“把布局画出来”
    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_publish, container, false)
    }

    // 第二步：onViewCreated 负责“处理界面逻辑”
    // 此时 View 已经完全创建好了，在这里设置点击事件更安全、更专业
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // 初始化所有的点击跳转逻辑
        initClickListeners(view)
    }

    private fun initClickListeners(view: View) {
        // 1. 闲置跳转
        view.findViewById<View>(R.id.layout_publish_idle).setOnClickListener {
            startActivity(Intent(requireContext(), PublishIdleActivity::class.java))
        }

        // 2. 求购跳转
        view.findViewById<View>(R.id.layout_publish_buy).setOnClickListener {
            startActivity(Intent(requireContext(), PublishBuyActivity::class.java))
        }

        // 3. 拼车跳转
        view.findViewById<View>(R.id.layout_publish_carpool).setOnClickListener {
            startActivity(Intent(requireContext(), PublishCarpoolActivity::class.java))
        }

        // 4. 跑腿跳转
        view.findViewById<View>(R.id.layout_publish_errand).setOnClickListener {
            startActivity(Intent(requireContext(), PublishErrandActivity::class.java))
        }

        // 5. 寻物跳转
        view.findViewById<View>(R.id.layout_publish_lost).setOnClickListener {
            startActivity(Intent(requireContext(), PublishLostActivity::class.java))
        }
    }
}