package com.example.campusassistant.ui

import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.campusassistant.R
import com.example.campusassistant.data.AppDatabase
import com.example.campusassistant.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthActivity : AppCompatActivity() {
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var currentAccountView: TextView
    private val db by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        usernameInput = findViewById(R.id.et_username)
        passwordInput = findViewById(R.id.et_password)
        currentAccountView = findViewById(R.id.tv_current_account)

        findViewById<TextView>(R.id.btn_login_account).setOnClickListener { submitLogin() }
        findViewById<TextView>(R.id.btn_register_account).setOnClickListener { submitRegister() }
        findViewById<TextView>(R.id.btn_clear_account).setOnClickListener {
            UserSessionManager.logout(this)
            updateCurrentAccountText()
            Toast.makeText(this, "当前账号已退出", Toast.LENGTH_SHORT).show()
        }

        updateCurrentAccountText()
    }

    private fun submitLogin() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入账号和密码", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val user = db.userDao().getUserByUsername(username)
            withContext(Dispatchers.Main) {
                if (user != null && user.password == password) {
                    UserSessionManager.login(this@AuthActivity, user)
                    Toast.makeText(this@AuthActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@AuthActivity, "账号或密码错误，请确认是否已注册", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun submitRegister() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "注册需要填写账号和密码", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val existingUser = db.userDao().getUserByUsername(username)
            if (existingUser != null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthActivity, "该账号已存在", Toast.LENGTH_SHORT).show()
                }
            } else {
                val newUser = User(username = username, password = password)
                db.userDao().insertUser(newUser)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AuthActivity, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateCurrentAccountText() {
        if (UserSessionManager.isLoggedIn(this)) {
            currentAccountView.text = "当前账号：${UserSessionManager.getDisplayName(this)}"
        } else {
            currentAccountView.text = "当前账号：未登录"
        }
    }
}
