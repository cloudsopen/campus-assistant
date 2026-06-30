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
    // 第二层跳转按钮
    private var rlHeroPlaza: View? = null
    private var rlAuth: View? = null

    // 第三层分类按钮
    private var llExpress: LinearLayout? = null
    private var llTakeout: LinearLayout? = null
    private var llShop: LinearLayout? = null

    // 第四层动态文本
    private var tvRecordCount: TextView? = null
    private var tvTag: TextView? = null
    private var tvTitle: TextView? = null
    private var tvLocation: TextView? = null
    private var tvTimeLimit: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root: View = inflater.inflate(R.layout.fragment_aid, container, false)
        initView(root)
        initClick()
        // 默认选中代取快递
        selectTab(0)
        return root
    }

    // 绑定控件
    private fun initView(root: View) {
        rlHeroPlaza = root.findViewById<View?>(R.id.rl_hero_plaza)
        rlAuth = root.findViewById<View?>(R.id.rl_auth)

        llExpress = root.findViewById<LinearLayout?>(R.id.ll_express)
        llTakeout = root.findViewById<LinearLayout?>(R.id.ll_takeout)
        llShop = root.findViewById<LinearLayout?>(R.id.ll_shop)

        tvRecordCount = root.findViewById<TextView?>(R.id.tv_record_count)
        tvTag = root.findViewById<TextView?>(R.id.tv_tag)
        tvTitle = root.findViewById<TextView?>(R.id.tv_title)
        tvLocation = root.findViewById<TextView?>(R.id.tv_location)
        tvTimeLimit = root.findViewById<TextView?>(R.id.tv_time_limit)
    }

    // 绑定点击事件
    private fun initClick() {
        // 跳转页面按钮
        rlHeroPlaza!!.setOnClickListener(View.OnClickListener { v: View? ->
            // 跳转到英雄广场页面
            val intent: Intent = Intent(getActivity(), HeroPlazaActivity::class.java)
            startActivity(intent)
        })
        rlAuth!!.setOnClickListener(View.OnClickListener { v: View? ->
            // 跳转到校园认证页面
            val intent: Intent = Intent(getActivity(), AuthActivity::class.java)
            startActivity(intent)
        })

        // 分类切换Tab
        llExpress!!.setOnClickListener(View.OnClickListener { v: View? -> selectTab(0) })
        llTakeout!!.setOnClickListener(View.OnClickListener { v: View? -> selectTab(1) })
        llShop!!.setOnClickListener(View.OnClickListener { v: View? -> selectTab(2) })
    }

    /**
     * 切换分类Tab，修改文字颜色 + 更新第四层内容
     * @param index 0快递 1外卖 2代买
     */
    private fun selectTab(index: Int) {
        // 重置全部文字为黑色
        setTabTextColor(llExpress!!, "#333333")
        setTabTextColor(llTakeout!!, "#333333")
        setTabTextColor(llShop!!, "#333333")

        // 选中项文字改为绿色
        when (index) {
            0 -> {
                setTabTextColor(llExpress!!, "#00C853")
                updateContent(
                    "快递",
                    "共3条记录",
                    "取快递，西区菜鸟驿站，送到宿舍2号楼",
                    "西区菜鸟驿站 → 2号宿舍楼",
                    "截止：今日18:00",
                    "¥3"
                )
            }

            1 -> {
                setTabTextColor(llTakeout!!, "#00C853")
                updateContent(
                    "外卖",
                    "共5条记录",
                    "帮忙取校门口奶茶店外卖，送到5栋宿舍",
                    "校门口奶茶店 → 5号宿舍楼",
                    "截止：中午12:30",
                    "¥2"
                )
            }

            2 -> {
                setTabTextColor(llShop!!, "#00C853")
                updateContent(
                    "代买",
                    "共2条记录",
                    "超市代买矿泉水+面包，送到3栋",
                    "校内超市 → 3号宿舍楼",
                    "截止：今晚21:00",
                    "¥4"
                )
            }
        }
    }

    // 修改Tab内部文字颜色
    private fun setTabTextColor(tabLayout: LinearLayout, colorHex: String?) {
        val text = tabLayout.getChildAt(1) as TextView
        text.setTextColor(Color.parseColor(colorHex))
    }

    // 更新第四层卡片所有内容
    private fun updateContent(
        tag: String?,
        count: String?,
        title: String?,
        location: String?,
        time: String?,
        money: String?
    ) {
        tvRecordCount!!.setText(count)
        tvTag!!.setText(tag)
        tvTitle!!.setText(title)
        tvLocation!!.setText(location)
        tvTimeLimit!!.setText(time)
        // 报酬文字替换
        tvRecordCount!!.setText(count)
        (getView()!!.findViewById<View?>(R.id.tv_money) as TextView).setText("报酬 " + money)
    }
}