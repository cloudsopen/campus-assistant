package com.example.campusassistant.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.campusassistant.R
class AidFragment : Fragment() {
    private var rlHeroPlaza: View? = null
    private var rlAuth: View? = null

    private var llExpress: LinearLayout? = null
    private var llTakeout: LinearLayout? = null
    private var llShop: LinearLayout? = null

    private var tvRecordCount: TextView? = null
    private var tvTag: TextView? = null
    private var tvTitle: TextView? = null
    private var tvLocation: TextView? = null
    private var tvTimeLimit: TextView? = null
    private var tvMoney: TextView? = null // 提前绑定报酬控件

    private companion object {
        const val TAB_EXPRESS = 0
        const val TAB_TAKEOUT = 1
        const val TAB_SHOP = 2
        const val COLOR_NORMAL = "#333333"
        const val COLOR_SELECT = "#00C853"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_aid, container, false)
        initView(root)
        initClick()
        selectTab(TAB_EXPRESS)
        return root
    }

    private fun initView(root: View) {
        rlHeroPlaza = root.findViewById(R.id.rl_hero_plaza)
        rlAuth = root.findViewById(R.id.rl_auth)

        llExpress = root.findViewById(R.id.ll_express)
        llTakeout = root.findViewById(R.id.ll_takeout)
        llShop = root.findViewById(R.id.ll_shop)

        tvRecordCount = root.findViewById(R.id.tv_record_count)
        tvTag = root.findViewById(R.id.tv_tag)
        tvTitle = root.findViewById(R.id.tv_title)
        tvLocation = root.findViewById(R.id.tv_location)
        tvTimeLimit = root.findViewById(R.id.tv_time_limit)
        tvMoney = root.findViewById(R.id.tv_money) // 一次性绑定
    }

    private fun initClick() {
        rlHeroPlaza?.setOnClickListener {
            startActivity(Intent(requireActivity(), HeroPlazaActivity::class.java))
        }
        rlAuth?.setOnClickListener {
            startActivity(Intent(requireActivity(), AuthActivity::class.java))
        }

        llExpress?.setOnClickListener { selectTab(TAB_EXPRESS) }
        llTakeout?.setOnClickListener { selectTab(TAB_TAKEOUT) }
        llShop?.setOnClickListener { selectTab(TAB_SHOP) }
    }

    private fun selectTab(index: Int) {
        llExpress?.let { setTabTextColor(it, COLOR_NORMAL) }
        llTakeout?.let { setTabTextColor(it, COLOR_NORMAL) }
        llShop?.let { setTabTextColor(it, COLOR_NORMAL) }

        when (index) {
            TAB_EXPRESS -> {
                llExpress?.let { setTabTextColor(it, COLOR_SELECT) }
                updateContent("快递", "共3条记录", "取快递，西区菜鸟驿站，送到宿舍2号楼", "西区菜鸟驿站 → 2号宿舍楼", "截止：今日18:00", "¥3")
            }
            TAB_TAKEOUT -> {
                llTakeout?.let { setTabTextColor(it, COLOR_SELECT) }
                updateContent("外卖", "共5条记录", "帮忙取校门口奶茶店外卖，送到5栋宿舍", "校门口奶茶店 → 5号宿舍楼", "截止：中午12:30", "¥2")
            }
            TAB_SHOP -> {
                llShop?.let { setTabTextColor(it, COLOR_SELECT) }
                updateContent("代买", "共2条记录", "超市代买矿泉水+面包，送到3栋", "校内超市 → 3号宿舍楼", "截止：今晚21:00", "¥4")
            }
        }
    }

    private fun setTabTextColor(tabLayout: LinearLayout, colorHex: String) {
        val text = tabLayout.getChildAt(1) as TextView
        text.setTextColor(Color.parseColor(colorHex))
    }

    private fun updateContent(
        tag: String,
        count: String,
        title: String,
        location: String,
        time: String,
        money: String
    ) {
        tvRecordCount?.text = count
        tvTag?.text = tag
        tvTitle?.text = title
        tvLocation?.text = location
        tvTimeLimit?.text = time
        tvMoney?.text = "报酬 $money"
    }
}