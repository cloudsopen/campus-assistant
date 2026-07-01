package com.example.campusassistant.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.campusassistant.R;

public class AuthActivity extends AppCompatActivity {
    private EditText usernameInput;
    private EditText passwordInput;
    private EditText tagInput;
    private TextView currentAccountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        usernameInput = findViewById(R.id.et_username);
        passwordInput = findViewById(R.id.et_password);
        tagInput = findViewById(R.id.et_tag);
        currentAccountView = findViewById(R.id.tv_current_account);

        bindQuickAccount(R.id.btn_quick_account_1, "Auroraeus", "在校用户");
        bindQuickAccount(R.id.btn_quick_account_2, "西风漂流", "开发者");
        bindQuickAccount(R.id.btn_quick_account_3, "校园商铺", "商家用户");

        findViewById(R.id.btn_login_account).setOnClickListener(v -> submitLogin());
        findViewById(R.id.btn_clear_account).setOnClickListener(v -> {
            UserSessionManager.logout(this);
            updateCurrentAccountText();
            Toast.makeText(this, "当前账号已退出", Toast.LENGTH_SHORT).show();
        });

        updateCurrentAccountText();
    }

    private void bindQuickAccount(int buttonId, String username, String tag) {
        findViewById(buttonId).setOnClickListener(v -> {
            usernameInput.setText(username);
            tagInput.setText(tag);
            passwordInput.setText("123456");
        });
    }

    private void submitLogin() {
        String username = usernameInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String tag = tagInput.getText().toString().trim();

        if (username.isEmpty()) {
            usernameInput.setError("请输入账号名称");
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("请输入密码");
            return;
        }

        UserSessionManager.login(this, username, tag);
        Toast.makeText(this, "登录成功，已切换到 " + username, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateCurrentAccountText() {
        if (UserSessionManager.isLoggedIn(this)) {
            currentAccountView.setText("当前账号：" + UserSessionManager.getDisplayName(this));
        } else {
            currentAccountView.setText("当前账号：未登录");
        }
    }
}
