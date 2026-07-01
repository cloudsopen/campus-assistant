package com.example.campusassistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.campusassistant.ForumActivity
import com.example.campusassistant.IdleHallActivity
import com.example.campusassistant.LostandFoundActivity
import com.example.campusassistant.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<View>(R.id.lost_found_entry).setOnClickListener {
            startActivity(Intent(requireContext(), LostandFoundActivity::class.java))
        }

        view.findViewById<View>(R.id.forum).setOnClickListener {
            val intent = Intent(requireContext(), ForumActivity::class.java)
            startActivity(intent)
        }

        val secondHandClick = View.OnClickListener {
            startActivity(Intent(requireContext(), IdleHallActivity::class.java))
        }
        view.findViewById<View>(R.id.second_hand_entry).setOnClickListener(secondHandClick)
        view.findViewById<View>(R.id.view_more_second_hand).setOnClickListener(secondHandClick)
        view.findViewById<View>(R.id.btn_enter_idle_hall).setOnClickListener(secondHandClick)

        bindCategoryPreview(view, R.id.preview_chip_all, "全部")
        bindCategoryPreview(view, R.id.preview_chip_books, "图书教材")
        bindCategoryPreview(view, R.id.preview_chip_daily, "生活用品")

        return view
    }

    private fun bindCategoryPreview(view: View, viewId: Int, category: String) {
        view.findViewById<View>(viewId).setOnClickListener {
            val intent = Intent(requireContext(), IdleHallActivity::class.java)
            intent.putExtra(IdleHallActivity.EXTRA_CATEGORY, category)
            startActivity(intent)
        }
    }
}
