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

public class ResetPasswordActity extends AppCompatActivity {
    private ImageButton imbBack;
    private TextView tvResetPw;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password_actity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.reset_password), (v, insets) -> {
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
        tvResetPw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ResetPasswordActity.this, VerifyOtpCodeActivity.class);
                startActivity(intent);
            }
        });
    }
    private void anhXa(){
        imbBack = findViewById(R.id.imbBack);
        tvResetPw = findViewById(R.id.tvResetPw);
    }
}