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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private CheckBox checkboxRemember ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Kiểm tra xem đã đăng nhập và chọn "Ghi nhớ đăng nhập" chưa
        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        isLoggedIn=false;
        if (isLoggedIn) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Nếu chưa thì hiện giao diện đăng nhập
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_in), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        anhXa();
        navigation();
    }

    private void anhXa() {
        usernameEditText = findViewById(R.id.edtName);
        passwordEditText = findViewById(R.id.edtPassword);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvLogin = findViewById(R.id.tvLogin);
        tvForgotPw = findViewById(R.id.tvForgotPw);
        checkboxRemember = findViewById(R.id.checkBox);
    }

    private void navigation() {
        tvSignUp.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        tvLogin.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (!username.isEmpty() && !password.isEmpty()) {
                loginUser(username, password);
            } else {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập đầy đủ Tên đăng nhập và mật khẩu", Toast.LENGTH_SHORT).show();
            }
        });

        tvForgotPw.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser(String username, String password) {
        UserService apiService = RetrofitClient.getRetrofit().create(UserService.class);
        LoginRequest request = new LoginRequest(username, password);

        Call<UserResponse> call = apiService.login(request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    UserResponse userResponse = response.body();
                    if (userResponse != null) {
                        String username = userResponse.getUsername();

                        if (checkboxRemember.isChecked()) {
                            SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("isLoggedIn", true);
                            editor.putString("username", username);
                            editor.apply();
                        }

                        // ➡️ Chuyển qua màn hình chính
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("LoginError", "Error body: " + errorBody);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("LoginError", "Error parsing error body", e);
                    }

                    int statusCode = response.code();
                    Log.e("LoginError", "HTTP Status Code: " + statusCode);
                    if (statusCode == 401) {
                        Toast.makeText(LoginActivity.this, "Sai tên tài khoản hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Đã có lỗi xảy ra, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("LoginError", "API call failed: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Lỗi kết nối, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
