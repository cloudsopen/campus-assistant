package com.example.campusassistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.campusassistant.ForumActivity
import com.example.campusassistant.LostandFoundActivity
import com.example.campusassistant.R

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<View>(R.id.lost_found_entry).setOnClickListener {
            val intent = Intent(requireContext(), LostandFoundActivity::class.java)
            startActivity(intent)
        }

        view.findViewById<View>(R.id.forum).setOnClickListener {
            val intent = Intent(requireContext(), ForumActivity::class.java)
            startActivity(intent)
        }

        val secondHandClick = View.OnClickListener {
            Toast.makeText(requireContext(), "二手闲置列表开发中", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.second_hand_entry).setOnClickListener(secondHandClick)
        view.findViewById<View>(R.id.view_more_second_hand).setOnClickListener(secondHandClick)

        return view
    }
}
