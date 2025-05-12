package com.example.apptodo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptodo.R;
import com.example.apptodo.api.UserService;
import com.example.apptodo.model.request.LoginRequest;
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
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);

        // Kiểm tra trạng thái đăng nhập khi mở lại app
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        boolean rememberMeChecked = prefs.getBoolean("rememberMeChecked", false);

        // Nếu đã đăng nhập và Remember Me được chọn, tự động gọi lại API login
        if (isLoggedIn && rememberMeChecked) {
            String username = prefs.getString("username", "");
            String password = prefs.getString("password", "");
            if (!username.isEmpty() && !password.isEmpty()) {
                loginUser(username, password);  // Gọi lại API login
            }
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
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });

        tvForgotPw.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
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
                        handleSuccessfulLogin(userResponse, username, password);
                    } else {
                        showErrorMessage("Dữ liệu trả về không hợp lệ");
                    }
                } else {
                    handleLoginError(response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("LoginError", "API call failed: " + t.getMessage(), t);
                showErrorMessage("Lỗi kết nối, vui lòng thử lại!");
            }
        });
    }

    private void handleSuccessfulLogin(UserResponse userResponse, String username, String password) {
        // Lưu thông tin người dùng nếu checkbox Remember me được chọn
        if (checkboxRemember.isChecked()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putBoolean("rememberMeChecked", true);
            editor.putString("username", username);
            editor.putString("password", password);  // Lưu password (cẩn thận khi lưu password)
            editor.putString("user_data", userResponse.getUsername());
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
