package com.example.apptodo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apptodo.R;
import com.example.apptodo.api.UserService;
import com.example.apptodo.model.request.EmailRequest;
import com.example.apptodo.retrofit.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {
    private ImageButton imbBack;
    private TextView tvForgotPw;
    private EditText email;

    private UserService apiService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        apiService= RetrofitClient.getApiUserService();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_password), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        anhXa();
        navigation();
    }
    private void navigation(){
//        quay về
        imbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
//        tới reset pw
        tvForgotPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendOtp();
            }
        });
    }
    private void sendOtp(){
        String emailInput = email.getText().toString().trim();

        EmailRequest user = new EmailRequest();
        user.setEmail(emailInput);

        Call<Map<String, String>> call = apiService.forgotPassword(user);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful()) {
                    // Kiểm tra xem response.body() có null không
                    if (response.body() != null) {
                        String message = response.body().get("message");
                        Log.d("API_RESPONSE", "Response: " + message);
                        Toast.makeText(ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                        // Chuyển sang màn hình VerifyOtpCodeActivity với email và loại yêu cầu
                        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyOtpCodeActivity.class);
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("type", "forgot");
                        startActivity(intent);
                    } else {
                        Log.e("API_RESPONSE", "Response body is null");
                        Toast.makeText(ForgotPasswordActivity.this, "Lỗi từ server, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Khi response không thành công, in ra mã lỗi và lý do
                    Log.e("API_RESPONSE", "Error: " + response.code() + " - " + response.message());
                    Toast.makeText(ForgotPasswordActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }


            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                Log.e("Register", "Error: " + t.getMessage());
            }
        });
    }
    private void anhXa(){
        email=findViewById(R.id.edtEmail1);
        imbBack = findViewById(R.id.imbBack);
        tvForgotPw = findViewById(R.id.tvForgotPw);
    }
}