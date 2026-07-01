package com.example.campusassistant.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.R
import com.example.campusassistant.data.ErrandTask

class ErrandTaskAdapter(
    private var tasks: List<ErrandTask>,
    private val onItemClick: (ErrandTask) -> Unit
) : RecyclerView.Adapter<ErrandTaskAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTag: TextView = view.findViewById(R.id.tv_tag)
        val tvTitle: TextView = view.findViewById(R.id.tv_title)
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvTimeLimit: TextView = view.findViewById(R.id.tv_time_limit)
        val tvMoney: TextView = view.findViewById(R.id.tv_money)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val tvPublisher: TextView = view.findViewById(R.id.tv_publisher_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_errand_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.tvTag.text = task.category
        holder.tvTitle.text = task.title
        holder.tvLocation.text = task.location ?: "地点不详"
        holder.tvTimeLimit.text = task.deadline ?: "无截止时间"
        holder.tvMoney.text = "¥${task.reward}"
        holder.tvPublisher.text = task.publisher ?: "匿名用户"
        
        holder.tvStatus.text = when(task.status) {
            0 -> "待接单"
            1 -> "进行中"
            else -> "已完成"
        }
        
        holder.itemView.setOnClickListener { onItemClick(task) }
    }

    override fun getItemCount() = tasks.size

    fun updateData(newTasks: List<ErrandTask>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
