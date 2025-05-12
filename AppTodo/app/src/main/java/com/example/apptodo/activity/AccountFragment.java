package com.example.apptodo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.apptodo.R;
import com.example.apptodo.api.UserService;
import com.example.apptodo.model.User;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.UserUpdateRequest;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.viewmodel.SharedUserViewModel;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 71;

    private EditText etName, etEmail, etGender;
    private Button btnLogout, btnSave;
    private CircleImageView profileImageView;
    private ImageButton editImage;

    private SharedPreferences sharedPreferences;
    private SharedUserViewModel userViewModel;
    private UserService userService;
    private String avatarUrl;
    private Integer userId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        userService = RetrofitClient.getApiUserService();

        // Init Cloudinary
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dxhniukul");
        config.put("api_key", "515229361846327");
        config.put("api_secret", "xJm1cf-iCSgQlX8pwahOYHP00UE");

        try {
            MediaManager.init(requireContext(), config);
        } catch (IllegalStateException e) {
            // Đã khởi tạo rồi
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        etName = view.findViewById(R.id.et_account_name);
        etEmail = view.findViewById(R.id.et_email);
        etGender = view.findViewById(R.id.et_gender);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnSave = view.findViewById(R.id.btn_save);
        profileImageView = view.findViewById(R.id.profile_image);
        editImage = view.findViewById(R.id.btn_edit_avatar);

        userViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                etName.setText(user.getUsername());
                etEmail.setText(user.getEmail());
                etGender.setText(user.getGender());
                userId=user.getId();

                avatarUrl = user.getAvatar();
                loadAvatar(avatarUrl);
            }
        });

        editImage.setOnClickListener(v -> openImagePicker());
        btnSave.setOnClickListener(v -> updateUserInfo());
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void loadAvatar(String url) {
        if (url != null && !url.trim().isEmpty()) {
            Glide.with(requireContext()).load(url).placeholder(R.drawable.defaulter_user).into(profileImageView);
        } else {
            profileImageView.setImageResource(R.drawable.defaulter_user);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            uploadImageToCloudinary(data.getData());
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        MediaManager.get().upload(imageUri)
                .option("public_id", "user_avatar_" + System.currentTimeMillis())
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        avatarUrl = (String) resultData.get("secure_url");
                        loadAvatar(avatarUrl);
                        Toast.makeText(requireContext(), "Upload successful!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(requireContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Toast.makeText(requireContext(), "Upload rescheduled: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                    }
                })
                .dispatch();
    }

    private void updateUserInfo() {
        String username = etName.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setUsername(username);
        updateRequest.setAvatar(avatarUrl);

         // <-- Thay bằng ID người dùng thật trong hệ thống của bạn

        userService.updateUser(userId, updateRequest).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userViewModel.setUser(response.body());
                    Toast.makeText(requireContext(), "User updated successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to update user", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutUser() {
        sharedPreferences.edit().clear().apply();
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        requireActivity().finish();
    }
}
