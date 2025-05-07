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

    import androidx.fragment.app.Fragment;
    import androidx.lifecycle.ViewModelProvider;

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
    import com.bumptech.glide.Glide;
    import de.hdodenhof.circleimageview.CircleImageView;
    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    import java.util.HashMap;
    import java.util.Map;

    public class AccountFragment extends Fragment {

        private static final int PICK_IMAGE_REQUEST = 71;

        private EditText etName, etEmail, etGender;
        private Button btnLogout, btnSave;
        private CircleImageView profileImageView;
        private ImageButton editImage;
        private SharedPreferences sharedPreferences;
        private SharedUserViewModel userViewModel;
        private UserService userService;


        private String avatarUrl; // Biến lưu trữ URL ảnh đại diện

        public AccountFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            sharedPreferences = requireActivity().getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
            userService = RetrofitClient.getApiUserService();

            // Cấu hình Cloudinary tại đây
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dewjxowhs");
            config.put("api_key", "332181415891584");
            config.put("api_secret", "7lVo4-Zcph562hflmMD5qORBfG0");

            // Cấu hình Cloudinary
            MediaManager.init(getContext(), config);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_account, container, false);

            // Ánh xạ View
            etName = view.findViewById(R.id.et_account_name);
            etEmail = view.findViewById(R.id.et_email);
            etGender = view.findViewById(R.id.et_gender);
            btnLogout = view.findViewById(R.id.btn_logout);
            btnSave = view.findViewById(R.id.btn_save);
            profileImageView = view.findViewById(R.id.profile_image);
            editImage = view.findViewById(R.id.btn_edit_avatar);

            // Khởi tạo ViewModel để lấy user
            userViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
            userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    etName.setText(user.getUsername());
                    etEmail.setText(user.getEmail());
                    etGender.setText(user.getGender());

                    // Cập nhật ảnh đại diện nếu có
                    if (user.getAvatar() != null) {
                        Glide.with(requireContext()).load(user.getAvatar()).into(profileImageView);
                        avatarUrl = user.getAvatar(); // Lưu lại URL ảnh đại diện
                    }
                }
            });

            // Đăng xuất người dùng
            btnLogout.setOnClickListener(v -> logoutUser());

            // Mở bộ chọn ảnh khi người dùng muốn thay đổi ảnh đại diện
            editImage.setOnClickListener(v -> openImagePicker());

            // Xử lý sự kiện bấm nút "Save"
            btnSave.setOnClickListener(v -> updateUserInfo());

            return view;
        }

        // Mở bộ chọn ảnh từ bộ sưu tập
        private void openImagePicker() {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }

        // Xử lý kết quả khi người dùng chọn ảnh
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    uploadImageToCloudinary(imageUri); // Gọi hàm upload ảnh
                }
            }
        }

        // Tải ảnh lên Cloudinary
        private void uploadImageToCloudinary(Uri imageUri) {
            UploadCallback callback = new UploadCallback() {
                @Override
                public void onStart(String requestId) {
                    // Bắt đầu upload
                    Toast.makeText(requireContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onProgress(String requestId, long bytes, long totalBytes) {}

                @Override
                public void onSuccess(String requestId, Map resultData) {
                    // Upload thành công
                    avatarUrl = (String) resultData.get("secure_url");
                    Glide.with(requireContext()).load(avatarUrl).into(profileImageView); // Cập nhật ảnh đại diện
                    Toast.makeText(requireContext(), "Upload successful!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String requestId, ErrorInfo error) {
                    // Xử lý lỗi khi upload
                    Toast.makeText(requireContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onReschedule(String requestId, ErrorInfo error) {
                    // Reschedule nếu có lỗi
                    Toast.makeText(requireContext(), "Upload rescheduled: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                }
            };

            // Tải ảnh lên Cloudinary
            MediaManager.get().upload(imageUri)
                    .option("public_id", "user_avatar")
                    .callback(callback)
                    .dispatch();
        }

        // Gửi yêu cầu cập nhật thông tin người dùng
        private void updateUserInfo() {
            String username = etName.getText().toString();

            // Tạo request DTO để gửi đến API
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setUsername(username);
            updateRequest.setAvatar(avatarUrl); // Đưa URL ảnh vào request

            Integer userId = 1; // Thay bằng ID người dùng thực tế
            userService.updateUser(userId, updateRequest).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful()) {
                        // Lấy user từ response và cập nhật lại trong ViewModel
                        UserResponse updatedUserResponse = response.body();
                        if (updatedUserResponse != null) {
                            // Cập nhật lại dữ liệu trong ViewModel
                            userViewModel.setUser(updatedUserResponse);
                        }
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


        // Lưu URL của ảnh vào SharedPreferences
        private void saveAvatarUrl(String url) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("avatar_url", url);
            editor.apply();
        }

        // Đăng xuất người dùng
        private void logoutUser() {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }
