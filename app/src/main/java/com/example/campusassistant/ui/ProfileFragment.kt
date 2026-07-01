package com.example.campusassistant.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.campusassistant.LostandFoundActivity
import com.example.campusassistant.PublishCarpoolActivity
import com.example.campusassistant.PublishErrandActivity
import com.example.campusassistant.PublishIdleActivity
import com.example.campusassistant.R

class ProfileFragment : Fragment() {
    private var rootView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        rootView = view

        val authClick = View.OnClickListener {
            startActivity(Intent(requireContext(), AuthActivity::class.java))
        }

        view.findViewById<View>(R.id.profile_entry_area).setOnClickListener(authClick)
        view.findViewById<View>(R.id.btn_switch_account).setOnClickListener(authClick)
        view.findViewById<View>(R.id.profile_publish_idle).setOnClickListener {
            startActivity(Intent(requireContext(), PublishIdleActivity::class.java))
        }
        view.findViewById<View>(R.id.profile_publish_carpool).setOnClickListener {
            startActivity(Intent(requireContext(), PublishCarpoolActivity::class.java))
        }
        view.findViewById<View>(R.id.profile_publish_errand).setOnClickListener {
            startActivity(Intent(requireContext(), PublishErrandActivity::class.java))
        }
        view.findViewById<View>(R.id.profile_publish_lost).setOnClickListener {
            startActivity(Intent(requireContext(), LostandFoundActivity::class.java))
        }
        view.findViewById<View>(R.id.profile_orders_card).setOnClickListener {
            showToast("订单功能后续接入")
        }
        view.findViewById<View>(R.id.profile_action_settings).setOnClickListener(authClick)
        view.findViewById<TextView>(R.id.logout_button).setOnClickListener {
            if (UserSessionManager.isLoggedIn(requireContext())) {
                UserSessionManager.logout(requireContext())
                updateProfileUi()
                showToast("已退出当前账号")
            } else {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
            }
        }

        updateProfileUi()
        return view
    }

    override fun onResume() {
        super.onResume()
        updateProfileUi()
    }

    private fun updateProfileUi() {
        val view = rootView ?: return
        val loggedIn = UserSessionManager.isLoggedIn(requireContext())

        view.findViewById<TextView>(R.id.profile_name).text =
            UserSessionManager.getDisplayName(requireContext())
        view.findViewById<TextView>(R.id.profile_avatar).text =
            UserSessionManager.getAvatarLabel(requireContext())
        view.findViewById<TextView>(R.id.profile_balance).text =
            if (loggedIn) UserSessionManager.getBalanceText(requireContext()) else "¥0.00"
        view.findViewById<TextView>(R.id.profile_orders).text =
            if (loggedIn) UserSessionManager.getActiveOrdersText(requireContext()) else "0"
        view.findViewById<TextView>(R.id.btn_switch_account).text =
            if (loggedIn) "切换账号" else "立即登录"
        view.findViewById<TextView>(R.id.logout_button).text =
            if (loggedIn) "退出登录" else "去登录"
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
