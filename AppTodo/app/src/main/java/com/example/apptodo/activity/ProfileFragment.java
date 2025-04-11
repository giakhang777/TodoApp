package com.example.apptodo.activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.adapter.NotificationAdapter;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private List<String> projectList = new ArrayList<>();
    private ArrayAdapter<String> projectAdapter;
    private ListView projectListView;
    private List<String> notifications = new ArrayList<>();
    private NotificationAdapter notificationAdapter;
    private TextView tvNotificationCount;
    private Button btnMyAccount , btnAddProject,btnCompleted;
    private LinearLayout projectListContainer;
    private int projectCount ;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Tham chiếu đến TextView số thông báo và Button Notification
        tvNotificationCount = view.findViewById(R.id.tv_notification_count);
        ImageButton btnNotification = view.findViewById(R.id.btn_notification);
        // Khởi tạo nút và thiết lập sự kiện nhấn
        btnMyAccount = view.findViewById(R.id.btn_my_account);
        btnMyAccount.setOnClickListener(v -> openAccountFragment(new AccountFragment()));
        btnAddProject = view.findViewById(R.id.btn_add_project);  // Nút Add Project
        btnCompleted=view.findViewById(R.id.btn_completed);// Nút Completed
        btnCompleted.setOnClickListener(v -> openAccountFragment(new HistoryFragment()));
        projectListView = view.findViewById(R.id.project_list_view);

        // Khởi tạo Adapter và gán dữ liệu cho ListView
        projectAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, projectList);
        projectListView.setAdapter(projectAdapter);

        // Thêm dự án mẫu ban đầu
        addNewProject("Project 1");
        addNewProject("Project 2");
        addNewProject("Project 3");

        // Sự kiện khi nhấn nút thêm dự án
       // Thêm nút để thêm dự án mới
        btnAddProject.setOnClickListener(v -> addNewProject("Project " + (projectList.size() + 1)));
        notifications.add("Bạn có tin nhắn mới!");
        notifications.add("Cập nhật tài khoản thành công.");
        notifications.add("Khuyến mãi 50% hôm nay!");
        // Cập nhật số thông báo chưa đọc
        updateNotificationCount();


        // Xử lý sự kiện khi nhấn vào nút Notification
        btnNotification.setOnClickListener(v -> showNotificationPopup(v, notifications));

        return view;
    }

    private void updateNotificationCount() {
        // Cập nhật số lượng thông báo chưa đọc
        int unreadCount = notifications.size();
        if (unreadCount > 0) {
            tvNotificationCount.setText(String.valueOf(unreadCount));
            tvNotificationCount.setVisibility(View.VISIBLE);
        } else {
            tvNotificationCount.setVisibility(View.GONE);
        }
    }

    private void showNotificationPopup(View anchorView, List<String> notifications) {
        LayoutInflater inflater = (LayoutInflater) requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.notification_list, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recycler_notifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Khởi tạo adapter và truyền listener để xử lý click
        notificationAdapter = new NotificationAdapter(notifications, position -> {
            // Cập nhật lại số lượng thông báo chưa đọc
            updateNotificationCount();
        });
        recyclerView.setAdapter(notificationAdapter);

        PopupWindow popupWindow = new PopupWindow(popupView,
                800,
                800,
                true);

        popupWindow.showAsDropDown(anchorView, -150, 50);
        popupWindow.setOutsideTouchable(true); // Cho phép đóng khi nhấn ngoài
        popupWindow.setFocusable(true);
    }
    private void openAccountFragment(Fragment fragmentThis ) {
        // Sử dụng getActivity().getSupportFragmentManager() để thay thế toàn bộ ProfileFragment
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragmentThis); // frame_layout là ID của FrameLayout trong Activity
        transaction.addToBackStack(null); // Nếu bạn muốn người dùng có thể quay lại ProfileFragment
        transaction.commit();
    }
    private void addNewProject(String projectName) {
        // Thêm dự án mới vào danh sách
        projectList.add(projectName);

        // Cập nhật lại Adapter để ListView hiển thị item mới
        projectAdapter.notifyDataSetChanged();
    }

}

