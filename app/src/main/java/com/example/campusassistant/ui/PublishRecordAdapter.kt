package com.example.campusassistant.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PublishRecord(
    val id: Long,
    val type: String,
    val title: String,
    val time: Long,
    val status: String = "已发布",
    val color: Int = 0xFF42A5F5.toInt()
)

class PublishRecordAdapter(
    private val records: List<PublishRecord>,
    private val onItemClick: (PublishRecord) -> Unit
) : RecyclerView.Adapter<PublishRecordAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvType: TextView = view.findViewById(R.id.tv_record_type)
        val tvTitle: TextView = view.findViewById(R.id.tv_record_title)
        val tvTime: TextView = view.findViewById(R.id.tv_record_time)
        val tvStatus: TextView = view.findViewById(R.id.tv_record_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_publish_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        holder.itemView.setOnClickListener { onItemClick(record) }
        holder.tvType.text = record.type
        holder.tvType.backgroundTintList = android.content.res.ColorStateList.valueOf(record.color)
        holder.tvTitle.text = record.title
        holder.tvStatus.text = record.status
        
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        holder.tvTime.text = sdf.format(Date(record.time))
    }

    override fun getItemCount() = records.size
}