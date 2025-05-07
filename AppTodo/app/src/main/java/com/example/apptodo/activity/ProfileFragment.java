package com.example.apptodo.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.apptodo.R;
import com.example.apptodo.adapter.NotificationAdapter;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.viewmodel.SharedUserViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private List<String> projectList = new ArrayList<>();
    private NotificationAdapter notificationAdapter;
    private List<String> notifications = new ArrayList<>();
    private ArrayAdapter<String> projectAdapter;
    private TextView tvNotificationCount, profileNameTextView;
    private ListView projectListView;

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
            }
        });

        // Khởi tạo dữ liệu
        projectAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, projectList);
        projectListView.setAdapter(projectAdapter);
        addNewProject("Project 1");
        addNewProject("Project 2");
        addNewProject("Project 3");

        // Thêm thông báo mẫu
        notifications.add("Bạn có tin nhắn mới!");
        notifications.add("Cập nhật tài khoản thành công.");
        notifications.add("Khuyến mãi 50% hôm nay!");
        updateNotificationCount();

        // Xử lý các sự kiện
        btnMyAccount.setOnClickListener(v -> openFragment(new AccountFragment()));
        btnAddProject.setOnClickListener(v -> addNewProject("Project " + (projectList.size() + 1)));
        btnCompleted.setOnClickListener(v -> openFragment(new HistoryFragment()));
        btnNotification.setOnClickListener(this::showNotificationPopup);

        return view;
    }

    private void addNewProject(String name) {
        projectList.add(name);
        projectAdapter.notifyDataSetChanged();
    }

    private void updateNotificationCount() {
        int unread = notifications.size();
        tvNotificationCount.setText(String.valueOf(unread));
        tvNotificationCount.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
    }

    private void showNotificationPopup(View anchor) {
        View popupView = LayoutInflater.from(getContext()).inflate(R.layout.notification_list, null);
        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationAdapter = new NotificationAdapter(notifications, position -> updateNotificationCount());
        recyclerView.setAdapter(notificationAdapter);

        PopupWindow popupWindow = new PopupWindow(popupView, 800, 800, true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(anchor, -150, 50);
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
