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
import com.example.apptodo.model.OTPRequest;
import com.example.apptodo.retrofit.RetrofitClient;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyOtpCodeActivity extends AppCompatActivity {
    private ImageButton imbBack;
    private EditText editOtp1,editOtp2,editOtp3,editOtp4,editOtp5;
    String email,type;
    private TextView tvEnterOTP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_otp_code);
        email = getIntent().getStringExtra("email");
        type=getIntent().getStringExtra("type");
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.verify_otp), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        anhXa();
        navigation();
    }
    private void navigation(){
        imbBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tvEnterOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifyOTP();
            }
        });
    }
    private void verifyOTP() {

        String otp = editOtp1.getText().toString().trim() +
                    editOtp2.getText().toString().trim() +
                    editOtp3.getText().toString().trim() +
                    editOtp4.getText().toString().trim()+
                    editOtp5.getText().toString().trim();

        if (otp.length() < 5) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi dữ liệu xác thực OTP
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("code", otp);
        requestBody.put("email", email);
        OTPRequest request = new OTPRequest(email, otp);

        RetrofitClient.getApiUserService().verifyCode(request).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message");
                    Log.d("VerifyCode", "Server response: " + response.body());

                    if ("success".equals(message)) {
                        Toast.makeText(VerifyOtpCodeActivity.this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();
                        if ("register".equals(type)) {
                            startActivity(new Intent(VerifyOtpCodeActivity.this, LoginActivity.class));
                        } else {
                            Intent intent = new Intent(VerifyOtpCodeActivity.this, ResetPasswordActivity.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        }


                    } else {
                        Toast.makeText(VerifyOtpCodeActivity.this, "Xác thực thất bại!", Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(VerifyOtpCodeActivity.this, "Lỗi từ server!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(VerifyOtpCodeActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void anhXa(){

        editOtp1 = findViewById(R.id.otp1);
        editOtp2 = findViewById(R.id.otp2);
        editOtp3 = findViewById(R.id.otp3);
        editOtp4 = findViewById(R.id.otp4);
        editOtp5 = findViewById(R.id.otp5);
        imbBack = findViewById(R.id.imbBack);
        tvEnterOTP = findViewById(R.id.tvEnterOTP);

    }
}