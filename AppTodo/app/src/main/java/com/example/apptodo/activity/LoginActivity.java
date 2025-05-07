package com.example.apptodo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptodo.R;
import com.example.apptodo.api.UserService;
import com.example.apptodo.model.LoginRequest;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private TextView tvSignUp, tvLogin, tvForgotPw;
    private EditText usernameEditText, passwordEditText;
    private CheckBox checkboxRemember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra trạng thái đăng nhập
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        if (isLoggedIn) {
            navigateToMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);
        initializeViews();
        setupNavigation();
    }

    private void initializeViews() {
        usernameEditText = findViewById(R.id.edtName);
        passwordEditText = findViewById(R.id.edtPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        tvForgotPw = findViewById(R.id.tvForgotPw);
        checkboxRemember = findViewById(R.id.checkBox);
    }

    private void setupNavigation() {
        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        tvLogin.setOnClickListener(view -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ Tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(username, password);
            }
        });

        tvForgotPw.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String username, String password) {
        // Khởi tạo service API và request login
        UserService apiService = RetrofitClient.getRetrofit().create(UserService.class);
        LoginRequest request = new LoginRequest(username, password);

        // Gọi API đăng nhập
        Call<UserResponse> call = apiService.login(request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    UserResponse userResponse = response.body();
                    if (userResponse != null) {
                        handleSuccessfulLogin(userResponse);
                    } else {
                        showErrorMessage("Dữ liệu trả về không hợp lệ");
                    }
                } else {
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                // Nếu có lỗi khi gọi API
                Log.e("LoginError", "API call failed: " + t.getMessage(), t);
                showErrorMessage("Lỗi kết nối, vui lòng thử lại!");
            }
        });
    }

    private void handleSuccessfulLogin(UserResponse userResponse) {
        // Lưu thông tin người dùng nếu checkbox Remember me được chọn
        if (checkboxRemember.isChecked()) {
            SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("username", userResponse.getUsername());
            editor.putInt("userId", userResponse.getId());
            editor.apply();
        }

        // Chuyển đến MainActivity với thông tin người dùng
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("user", userResponse); // Truyền đối tượng UserResponse (phải implements Serializable)
        startActivity(intent);
        finish();
    }

    private void handleLoginError(Response<UserResponse> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
            Log.e("LoginError", "Error body: " + errorBody);
        } catch (IOException e) {
            Log.e("LoginError", "Error parsing error body", e);
        }

        int statusCode = response.code();
        if (statusCode == 401) {
            showErrorMessage("Sai tên tài khoản hoặc mật khẩu");
        } else {
            showErrorMessage("Đã có lỗi xảy ra, vui lòng thử lại!");
        }
    }

    private void showErrorMessage(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
