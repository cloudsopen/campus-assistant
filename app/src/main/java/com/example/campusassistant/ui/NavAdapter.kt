package com.example.campusassistant.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.R

class NavAdapter(
    private val categories: List<String>,
    private val onItemClick: (category: String) -> Unit // 点击之后的回调函数
) : RecyclerView.Adapter<NavAdapter.NavViewHolder>() {

    private var selectedPosition = 0

    class NavViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tv_nav_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_nav, parent, false)
        return NavViewHolder(view)
    }

    override fun onBindViewHolder(holder: NavViewHolder, position: Int) {
        val category = categories[position]
        holder.tvTitle.text = category

        // 高亮选中项：如果是当前选中的位置，文字变红/变粗，否则变黑
        if (position == selectedPosition) {
            holder.tvTitle.setTextColor(Color.parseColor("#FF4081")) // 比如粉色
            holder.tvTitle.paint.isFakeBoldText = true
        } else {
            holder.tvTitle.setTextColor(Color.parseColor("#333333"))
            holder.tvTitle.paint.isFakeBoldText = false
        }

        // 设置点击事件
        holder.itemView.setOnClickListener {
            // 刷新选中位置的高亮状态
            val lastSelected = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(lastSelected)
            notifyItemChanged(selectedPosition)

            // 触发回调，把选中的分类名字传出去
            onItemClick(category)
        }
    }

    override fun getItemCount(): Int = categories.size
}