package com.example.campusassistant

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.campusassistant.data.LostItem
import java.io.File

class LostItemAdapter(
    private var itemList: List<LostItem>,
    private val onItemClick: (LostItem) -> Unit
) : RecyclerView.Adapter<LostItemAdapter.LostViewHolder>() {

    // 内部类：用来持有小方块里各个组件的引用
    class LostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivCover: ImageView = view.findViewById(R.id.iv_item_cover)
        val tvCategory: TextView = view.findViewById(R.id.tv_item_category)
        val tvDescription: TextView = view.findViewById(R.id.tv_item_description)
        val tvLocation: TextView = view.findViewById(R.id.tv_item_location)
        val tvLosttime: TextView = view.findViewById(R.id.tv_item_losttime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LostViewHolder {
        // 绑定我们刚做好的卡片布局
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_lost_card, parent, false)
        return LostViewHolder(view)
    }

    override fun onBindViewHolder(holder: LostViewHolder, position: Int) {
        val item = itemList[position]

        // 绑定文字数据
        holder.tvCategory.text = item.category
        holder.tvDescription.text = item.description
        holder.tvLocation.text = "📍 地点：${item.location}"
        holder.tvLosttime.text = "⏰ 时间：${item.lost_time}"
        // 绑定图片逻辑：如果用户存了照片，我们取第一张作为封面展示
        if (!item.imagePaths.isNullOrEmpty()) {
            holder.ivCover.visibility = View.VISIBLE
            try {
                val firstPath = item.imagePaths[0]
                // 支持两种路径：本地文件路径（新）和 content:// URI（旧数据兼容）
                val uri = if (firstPath.startsWith("content://")) {
                    Uri.parse(firstPath)
                } else {
                    Uri.fromFile(File(firstPath))
                }
                holder.ivCover.setImageURI(uri)
            } catch (e: Exception) {
                Log.e("LostAdapter", "封面加载失败: ${item.imagePaths[0]}", e)
                holder.ivCover.visibility = View.GONE
            }
        } else {
            // 如果没照片，就把图片控件隐藏，文字区域会自动平铺拉伸
            holder.ivCover.visibility = View.GONE
        }

        // 设置整张卡片的点击事件 → 弹出详情弹窗
        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = itemList.size

    // 当数据库数据更新时，用来刷新页面的方法
    fun updateData(newList: List<LostItem>) {
        this.itemList = newList
        notifyDataSetChanged() // 通知列表刷新
    }
}