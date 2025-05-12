package com.example.apptodo.activity;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.apptodo.R;
import com.example.apptodo.api.ProjectService;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.request.ProjectRequest;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.viewmodel.SharedUserViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private List<ProjectResponse> projectList = new ArrayList<>();
    private TextView tvNotificationCount, profileNameTextView;
    private ListView projectListView;
    private String selectedColor = "#000000";  // Màu đen mặc định

    private ImageView profileImageView;
    private SharedUserViewModel userViewModel;

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Khởi tạo ViewModel
        userViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);

        // Ánh xạ View
        profileImageView = view.findViewById(R.id.profile_image);
        profileNameTextView = view.findViewById(R.id.profile_name);
        tvNotificationCount = view.findViewById(R.id.tv_notification_count);
        ImageButton btnNotification = view.findViewById(R.id.btn_notification);
        Button btnMyAccount = view.findViewById(R.id.btn_my_account);
        Button btnAddProject = view.findViewById(R.id.btn_add_project);
        Button btnCompleted = view.findViewById(R.id.btn_completed);
        Button btnLabel = view.findViewById(R.id.btn_labels);
        projectListView = view.findViewById(R.id.project_list_view);

        // Quan sát dữ liệu người dùng
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && profileNameTextView != null) {
                profileNameTextView.setText(user.getUsername());

                // Sử dụng Glide để tải ảnh vào ImageView nếu có URL avatar
                String avatarUrl = user.getAvatar(); // Lấy URL avatar từ đối tượng user
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(avatarUrl)  // URL ảnh đại diện
                            .placeholder(R.drawable.defaulter_user) // Placeholder nếu không có ảnh
                            .into(profileImageView);  // Đưa ảnh vào ImageView
                }
                loadProjects(user.getId());
            }
        });

        // Xử lý các sự kiện
        btnMyAccount.setOnClickListener(v -> openFragment(new AccountFragment()));
        btnAddProject.setOnClickListener(v -> showAddProjectDialog());
        btnCompleted.setOnClickListener(v -> openFragment(new HistoryFragment()));
        btnLabel.setOnClickListener(v -> openFragment(new LabelFragment()));
        btnNotification.setOnClickListener(this::showNotificationPopup);

        return view;
    }

    private void loadProjects(int userId) {
        ProjectService service = RetrofitClient.getProjectService();
        service.getAllProjects(userId).enqueue(new retrofit2.Callback<List<ProjectResponse>>() {
            @Override
            public void onResponse(Call<List<ProjectResponse>> call, Response<List<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.clear();  // Xóa danh sách cũ
                    for (ProjectResponse project : response.body()) {
                        projectList.add(project);  // Thêm dự án vào danh sách
                    }
                    updateProjectList();  // Cập nhật danh sách dự án
                } else {
                    Toast.makeText(getContext(), "Failed to load projects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ProjectResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm cập nhật danh sách dự án lên ListView
    private void updateProjectList() {
        // Tạo một danh sách các tên dự án và màu sắc tương ứng
        List<ProjectResponse> projectResponses = new ArrayList<>(projectList);

        // Tạo một adapter tùy chỉnh để hiển thị tên và màu của mỗi dự án
        ArrayAdapter<ProjectResponse> projectAdapter = new ArrayAdapter<ProjectResponse>(getContext(), android.R.layout.simple_list_item_1, projectResponses) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);  // Lấy view mặc định

                // Lấy dự án tương ứng với vị trí
                ProjectResponse project = getItem(position);

                // Lấy TextView để hiển thị tên dự án
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                if (textView != null) {
                    // Cập nhật tên dự án
                    textView.setText(project.getName());

                    // Lấy màu sắc của dự án và cập nhật màu cho tên dự án
                    String color = project.getColor();
                    try {
                        int colorInt = android.graphics.Color.parseColor(color);  // Chuyển mã màu sang màu sắc
                        textView.setTextColor(colorInt);  // Cập nhật màu chữ của tên dự án
                    } catch (IllegalArgumentException e) {
                        // Nếu màu không hợp lệ, có thể sử dụng màu mặc định
                        textView.setTextColor(android.graphics.Color.BLACK);  // Màu mặc định nếu không hợp lệ
                    }
                }

                return view;
            }
        };

        // Gắn adapter vào ListView
        projectListView.setAdapter(projectAdapter);
        projectAdapter.notifyDataSetChanged();  // Cập nhật lại danh sách
    }

    private void addNewProject(String name, String color) {
        ProjectResponse newProject = new ProjectResponse();  // Giả sử ProjectResponse có constructor mặc định
        newProject.setName(name);
        newProject.setColor(color);
        projectList.add(newProject);
        updateProjectList();
    }

    private void showNotificationPopup(View anchor) {
        // Chức năng hiển thị popup thông báo, có thể xử lý tương tự như hiện tại
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showAddProjectDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_project, null);

        EditText etProjectName = dialogView.findViewById(R.id.et_project_name);
        View viewSelectedColor = dialogView.findViewById(R.id.view_selected_color);
        LinearLayout layoutColorPicker = dialogView.findViewById(R.id.layout_color_picker);

        layoutColorPicker.setOnClickListener(v -> showColorPickerDialog(viewSelectedColor));

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(dialogView)
                .setTitle("Add New Project")
                .setPositiveButton("Add", null) // Để xử lý thủ công
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String projectName = etProjectName.getText().toString().trim();

            if (projectName.isEmpty()) {
                etProjectName.setError("Project name is required");
                return;
            }

            UserResponse currentUser = userViewModel.getUser().getValue();
            if (currentUser == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            ProjectRequest request = new ProjectRequest(currentUser.getId(), projectName, selectedColor);
            ProjectService service = RetrofitClient.getProjectService();

            service.createProject(request).enqueue(new retrofit2.Callback<ProjectResponse>() {
                @Override
                public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        addNewProject(response.body().getName(), response.body().getColor());
                        Toast.makeText(getContext(), "Project created successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to create project", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ProjectResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showColorPickerDialog(View viewSelectedColor) {
        final String[] colors = {
                "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
                "#000000", "#FFFFFF", "#808080", "#C0C0C0", "#800000", "#008000"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Color");

        HorizontalScrollView scrollView = new HorizontalScrollView(requireContext()); // để kéo nếu dài
        LinearLayout colorOptionsLayout = new LinearLayout(requireContext());
        colorOptionsLayout.setOrientation(LinearLayout.HORIZONTAL);
        colorOptionsLayout.setPadding(16, 16, 16, 16);

        scrollView.addView(colorOptionsLayout);

        AlertDialog colorDialog = builder.setView(scrollView).create();

        for (String color : colors) {
            ImageView colorView = new ImageView(requireContext());

            int size = 100; // px
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(10, 0, 10, 0);
            colorView.setLayoutParams(params);

            // Tạo drawable hình tròn với màu tương ứng
            GradientDrawable circleDrawable = new GradientDrawable();
            circleDrawable.setShape(GradientDrawable.OVAL);
            circleDrawable.setColor(android.graphics.Color.parseColor(color));
            circleDrawable.setSize(size, size);

            colorView.setBackground(circleDrawable);

            colorView.setOnClickListener(v -> {
                selectedColor = color;

                // Cập nhật view hiển thị màu đã chọn
                GradientDrawable selectedDrawable = new GradientDrawable();
                selectedDrawable.setShape(GradientDrawable.OVAL);
                selectedDrawable.setColor(android.graphics.Color.parseColor(selectedColor));
                selectedDrawable.setSize(24, 24);
                viewSelectedColor.setBackground(selectedDrawable);

                colorDialog.dismiss(); // Đóng dialog sau khi chọn
            });

            colorOptionsLayout.addView(colorView);
        }

        colorDialog.show();
    }
}
