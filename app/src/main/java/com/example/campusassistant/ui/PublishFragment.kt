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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_publish, container, false)

        // 1. 闲置跳转
        view.findViewById<View>(R.id.layout_publish_idle).setOnClickListener {
            val intent = Intent(requireContext(), PublishIdleActivity::class.java)
            startActivity(intent)
        }

        // 2. 求购跳转
        view.findViewById<View>(R.id.layout_publish_buy).setOnClickListener {
            val intent = Intent(requireContext(), PublishBuyActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.layout_publish_carpool).setOnClickListener {
            val intent = Intent(requireContext(), PublishCarpoolActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.layout_publish_errand).setOnClickListener {
            val intent = Intent(requireContext(), PublishErrandActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.layout_publish_lost).setOnClickListener {
            val intent = Intent(requireContext(), PublishLostActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}