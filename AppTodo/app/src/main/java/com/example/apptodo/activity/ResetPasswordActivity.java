package com.example.apptodo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.apptodo.R;
import com.example.apptodo.model.request.SignUpRequest;
import com.example.apptodo.retrofit.RetrofitClient;


import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private ImageButton imbBack;
    private EditText edtNewPassword, edtConfirmPassword;
    private TextView btnSubmitReset;
    private String email; // lấy từ intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_actity);

        anhXa();
        setupEvent();

        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy email!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void anhXa() {
        imbBack = findViewById(R.id.imbBack);
        edtNewPassword = findViewById(R.id.edtPassword1);
        edtConfirmPassword = findViewById(R.id.edtPassword2);
        btnSubmitReset = findViewById(R.id.tvResetPw);
    }

    private void setupEvent() {
        imbBack.setOnClickListener(view -> finish());

        btnSubmitReset.setOnClickListener(view -> {
            String newPassword = edtNewPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
                return;
            }

            resetPassword(email, newPassword);
        });
    }

    private void resetPassword(String email, String newPassword) {
        SignUpRequest.ResetPassword dto = new SignUpRequest.ResetPassword(email, newPassword);

        RetrofitClient.getApiUserService().resetPassword(dto).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message");
                    if ("password_reset_success".equals(message)) {
                        Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Lỗi từ server!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ResetPasswordActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
