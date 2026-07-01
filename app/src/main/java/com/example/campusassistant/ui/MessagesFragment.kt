package com.example.campusassistant.ui

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.MainActivity
import com.example.campusassistant.R
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.CampusMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessagesFragment : Fragment() {
    private lateinit var listContainer: LinearLayout
    private lateinit var emptyView: TextView
    private lateinit var unreadCountView: TextView
    private lateinit var markAllReadButton: TextView
    private val db by lazy { AppDatabase.getDatabase(requireContext()) }
    private var hasShownUnreadDialog = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        listContainer = view.findViewById(R.id.layout_message_list)
        emptyView = view.findViewById(R.id.tv_messages_empty)
        unreadCountView = view.findViewById(R.id.tv_unread_count)
        markAllReadButton = view.findViewById(R.id.btn_mark_all_read)

        markAllReadButton.setOnClickListener { markAllMessagesRead() }
        loadMessages()
        return view
    }

    override fun onResume() {
        super.onResume()
        loadMessages()
        refreshBottomBadge()
    }

    private fun loadMessages() {
        val ownerUserId = UserSessionManager.getUserId(requireContext())
        if (!UserSessionManager.isLoggedIn(requireContext()) || ownerUserId <= 0L) {
            listContainer.removeAllViews()
            unreadCountView.text = "请先登录"
            emptyView.text = "登录后可以查看属于当前账号的消息"
            emptyView.visibility = View.VISIBLE
            markAllReadButton.visibility = View.GONE
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val messages = db.campusMessageDao().getMessagesByOwner(ownerUserId)
            val unreadCount = db.campusMessageDao().countUnread(ownerUserId)
            withContext(Dispatchers.Main) {
                renderMessages(messages, unreadCount)
            }
        }
    }

    private fun renderMessages(messages: List<CampusMessage>, unreadCount: Int) {
        listContainer.removeAllViews()
        markAllReadButton.visibility = if (messages.isEmpty()) View.GONE else View.VISIBLE
        unreadCountView.text = if (unreadCount > 0) {
            "$unreadCount 条未读消息"
        } else {
            "暂无未读消息"
        }

        if (messages.isEmpty()) {
            emptyView.text = "暂无消息"
            emptyView.visibility = View.VISIBLE
            return
        }

        emptyView.visibility = View.GONE
        messages.forEach { message ->
            listContainer.addView(createMessageView(message))
        }

        if (unreadCount > 0 && !hasShownUnreadDialog) {
            hasShownUnreadDialog = true
            AlertDialog.Builder(requireContext())
                .setTitle("新的消息提醒")
                .setMessage("你有 $unreadCount 条未读消息，请及时查看。")
                .setPositiveButton("知道了", null)
                .show()
        }
    }

    private fun createMessageView(message: CampusMessage): View {
        val card = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_idle_placeholder)
            setPadding(dp(16), dp(14), dp(16), dp(14))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, dp(12))
            layoutParams = params
            isClickable = true
            isFocusable = true
        }

        val titleRow = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val title = TextView(requireContext()).apply {
            text = message.title
            setTextColor(Color.parseColor("#111827"))
            textSize = 17f
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val status = TextView(requireContext()).apply {
            text = if (message.isRead) "已读" else "未读"
            setTextColor(Color.parseColor(if (message.isRead) "#9CA3AF" else "#2563EB"))
            textSize = 13f
        }

        titleRow.addView(title)
        titleRow.addView(status)
        card.addView(titleRow)

        card.addView(TextView(requireContext()).apply {
            text = message.content
            setTextColor(Color.parseColor("#4B5563"))
            textSize = 15f
            setPadding(0, dp(8), 0, 0)
        })

        val footer = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, 0)
        }

        footer.addView(TextView(requireContext()).apply {
            text = formatTime(message.createdAt)
            setTextColor(Color.parseColor("#9CA3AF"))
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        footer.addView(TextView(requireContext()).apply {
            text = "删除"
            setTextColor(Color.parseColor("#DC2626"))
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(6), dp(10), dp(6))
            setOnClickListener { deleteMessage(message.id) }
        })

        card.addView(footer)
        card.setOnClickListener {
            if (!message.isRead) {
                markMessageRead(message.id)
            } else {
                Toast.makeText(requireContext(), message.content, Toast.LENGTH_SHORT).show()
            }
        }
        return card
    }

    private fun markMessageRead(messageId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.campusMessageDao().markRead(messageId)
            withContext(Dispatchers.Main) {
                loadMessages()
                refreshBottomBadge()
            }
        }
    }

    private fun markAllMessagesRead() {
        val ownerUserId = UserSessionManager.getUserId(requireContext())
        if (ownerUserId <= 0L) return
        lifecycleScope.launch(Dispatchers.IO) {
            db.campusMessageDao().markAllRead(ownerUserId)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "已全部标记为已读", Toast.LENGTH_SHORT).show()
                loadMessages()
                refreshBottomBadge()
            }
        }
    }

    private fun deleteMessage(messageId: Long) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.campusMessageDao().deleteMessage(messageId)
            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext(), "消息已删除", Toast.LENGTH_SHORT).show()
                loadMessages()
                refreshBottomBadge()
            }
        }
    }

    private fun refreshBottomBadge() {
        (activity as? MainActivity)?.refreshUnreadMessageBadge()
    }

    private fun formatTime(time: Long): String {
        return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(time))
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
