package com.example.campusassistant

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.ui.UserSessionManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var navView: BottomNavigationView
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        navView = findViewById(R.id.bottom_navigation)
        val fab: FloatingActionButton = findViewById(R.id.fab_add)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            navView.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        fab.setOnClickListener {
            navController.navigate(R.id.navigation_publish)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_publish) {
                navView.menu.setGroupCheckable(0, true, false)
                for (i in 0 until navView.menu.size()) {
                    navView.menu.getItem(i).isChecked = false
                }
            } else {
                navView.menu.setGroupCheckable(0, true, true)
            }
            refreshUnreadMessageBadge()
        }

        navView.setOnItemSelectedListener { item ->
            if (item.itemId != R.id.navigation_placeholder) {
                navController.navigate(item.itemId)
            }
            true
        }

        navView.setOnItemReselectedListener { item ->
            if (
                item.itemId != R.id.navigation_placeholder &&
                navController.currentDestination?.id == R.id.navigation_publish
            ) {
                navController.navigate(item.itemId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshUnreadMessageBadge()
    }

    fun refreshUnreadMessageBadge() {
        val ownerUserId = UserSessionManager.getUserId(this)
        if (!UserSessionManager.isLoggedIn(this) || ownerUserId <= 0L) {
            navView.removeBadge(R.id.navigation_messages)
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val unreadCount = db.campusMessageDao().countUnread(ownerUserId)
            withContext(Dispatchers.Main) {
                if (unreadCount > 0) {
                    navView.getOrCreateBadge(R.id.navigation_messages).apply {
                        isVisible = true
                        number = unreadCount
                    }
                } else {
                    navView.removeBadge(R.id.navigation_messages)
                }
            }
        }
    }
}
