package com.example.apptodo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.apptodo.R;
import com.example.apptodo.api.UserService;
import com.example.apptodo.model.SignUpRequest;
import com.example.apptodo.model.User;
import com.example.apptodo.retrofit.RetrofitClient;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    private TextView tvSignIn, tvRegister;
    private ImageView passwordToggle , passwordToggleConfirm ;
    private TextView otpMessage;
    private UserService apiService;
    private EditText emailInput, passwordInput, passwordConfirmInput, usernameInput;

    private RadioGroup genderGroup;
    private RadioButton rbtnMale, rbtnFemale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        apiService = RetrofitClient.getApiUserService();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sign_up), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AnhXa();
        togglePasswordVisibility(passwordInput, passwordToggle);
        togglePasswordVisibility(passwordConfirmInput, passwordToggleConfirm);

        navigation();
    }

    private void navigation() {
        tvSignIn.setOnClickListener(view -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        tvRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String passwordConfirm = passwordConfirmInput.getText().toString().trim();
        String username = usernameInput.getText().toString().trim();

        String gender = "";
        int selectedGenderId = genderGroup.getCheckedRadioButtonId();
        if (selectedGenderId == R.id.rbtnMale) {
            gender = "MALE";
        } else if (selectedGenderId == R.id.rbtnFemale) {
            gender = "FEMALE";
        } else {
            Toast.makeText(this, "Vui lòng chọn giới tính!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(passwordConfirm)) {
            Toast.makeText(SignUpActivity.this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        SignUpRequest user = new SignUpRequest(username, email, password, gender, false);

        Call<Map<String, String>> call = apiService.signUpPostForm(user);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String message = response.body().get("message");
                    Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpActivity.this, VerifyOtpCodeActivity.class);
                    intent.putExtra("email", user.getEmail());
                    intent.putExtra("type","register");
                    startActivity(intent);
                } else {
                    Log.e("API_RESPONSE", "Error: " + response);
                    Toast.makeText(SignUpActivity.this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(SignUpActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                Log.e("Register", "Error: " + t.getMessage());
            }
        });
    }
    private void togglePasswordVisibility(EditText passwordField, ImageView toggleIcon) {
        toggleIcon.setOnClickListener(v -> {
            if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleIcon.setImageResource(R.drawable.ic_visibility_on); // Đổi sang icon hidden
            } else {
                passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleIcon.setImageResource(R.drawable.ic_visibility_off); // Đổi lại icon eye
            }
            passwordField.setSelection(passwordField.getText().length());
        });
    }

    private void AnhXa() {
        passwordToggle = findViewById(R.id.imgTogglePassword1);
        passwordToggleConfirm = findViewById(R.id.imgTogglePassword2);
        emailInput = findViewById(R.id.edtEmail);
        passwordInput = findViewById(R.id.edtPassword1);
        passwordConfirmInput = findViewById(R.id.edtPassword2);
        usernameInput = findViewById(R.id.edtName);
        tvSignIn = findViewById(R.id.tvSignIn);
        tvRegister = findViewById(R.id.tvRegister);
        genderGroup = findViewById(R.id.radioGroupGender);
        rbtnMale = findViewById(R.id.rbtnMale);
        rbtnFemale = findViewById(R.id.rbtnFemale);
    }
}
