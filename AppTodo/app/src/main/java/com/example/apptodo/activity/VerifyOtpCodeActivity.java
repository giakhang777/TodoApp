package com.example.apptodo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apptodo.R;

public class VerifyOtpCodeActivity extends AppCompatActivity {
    private ImageButton imbBack;
    private TextView tvEnterOTP;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_otp_code);
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
                Intent intent = new Intent(VerifyOtpCodeActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void anhXa(){
        imbBack = findViewById(R.id.imbBack);
        tvEnterOTP = findViewById(R.id.tvEnterOTP);
    }
}