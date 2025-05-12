package com.example.apptodo.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.apptodo.R;
import com.example.apptodo.databinding.ActivityMainBinding;
import com.example.apptodo.activity.CalendarFragment;
import com.example.apptodo.activity.HomeFragment;
import com.example.apptodo.activity.ProfileFragment;
import com.example.apptodo.activity.TasksFragment;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.response.LabelResponse;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.viewmodel.LabelViewModel;
import com.example.apptodo.viewmodel.ProjectViewModel;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        UserResponse user = (UserResponse) getIntent().getSerializableExtra("user");
        SharedUserViewModel viewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        viewModel.setUser(user);

        // Mặc định mở Fragment Home
        replaceFragment(new HomeFragment());

        // Xử lý sự kiện chọn menu bottom
        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.calendar) {
                replaceFragment(new CalendarFragment());
            } else if (item.getItemId() == R.id.tasks) {
                replaceFragment(new TasksFragment());
            } else if (item.getItemId() == R.id.profile) {
                replaceFragment(new ProfileFragment());
            }
            return true;
        });

        // Floating action button (Thêm task)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showBottomDialog());
    }

    // Thay Fragment
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.commit();
    }

    // Dialog thêm task
    private void showBottomDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        View view = dialog.findViewById(android.R.id.content);

        ImageView cancelButton = view.findViewById(R.id.closeButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        Spinner prioritySpinner = view.findViewById(R.id.prioritySpinner);
        Spinner projectSpinner = view.findViewById(R.id.projectSpinner);
        Spinner labelSpinner = view.findViewById(R.id.labelSpinner);

        EditText scheduledDateEditText = view.findViewById(R.id.scheduledDate);
        EditText reminderTimeEditText = view.findViewById(R.id.reminderTime);

        // Thiết lập spinner độ ưu tiên (tĩnh)
        String[] priorities = {"Low", "Medium", "High"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        prioritySpinner.setAdapter(priorityAdapter);

        // Lấy user từ ViewModel
        SharedUserViewModel userViewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        int userId = userViewModel.getUser().getValue().getId();

        // ViewModel cho project và label
        ProjectViewModel projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        LabelViewModel labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);

        // Gọi API để lấy danh sách
        projectViewModel.fetchProjects(userId);
        labelViewModel.loadLabels(userId);

        // Quan sát danh sách Project
        projectViewModel.getProjectList().observe(this, projectList -> {
            List<String> projectNames = new ArrayList<>();
            projectNames.add("Select Project"); // Add a default "null" option
            for (ProjectResponse p : projectList) {
                projectNames.add(p.getName());
            }
            ArrayAdapter<String> projectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, projectNames);
            projectSpinner.setAdapter(projectAdapter);
        });

        // Quan sát danh sách Label
        labelViewModel.getLabels().observe(this, labelList -> {
            List<String> labelNames = new ArrayList<>();
            labelNames.add("Select Label"); // Add a default "null" option
            for (LabelResponse l : labelList) {
                labelNames.add(l.getTitle());
            }
            ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labelNames);
            labelSpinner.setAdapter(labelAdapter);
        });

        // Xử lý chọn ngày
        final Calendar calendar = Calendar.getInstance();
        scheduledDateEditText.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, selectedYear, selectedMonth, selectedDay) -> {
                String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                scheduledDateEditText.setText(formattedDate);
            }, year, month, day);

            datePickerDialog.show();
        });

        // Xử lý chọn giờ
        reminderTimeEditText.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view12, selectedHour, selectedMinute) -> {
                String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
                reminderTimeEditText.setText(formattedTime);
            }, hour, minute, true);

            timePickerDialog.show();
        });

        // Nút Save
        Button saveButton = view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            // Lấy dữ liệu từ các trường nhập
            EditText taskTitleEditText = view.findViewById(R.id.taskTitle);
            EditText taskDescriptionEditText = view.findViewById(R.id.taskDescription);

            String title = taskTitleEditText.getText().toString().trim();
            String description = taskDescriptionEditText.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();
            String projectName = projectSpinner.getSelectedItem().toString();
            String labelTitle = labelSpinner.getSelectedItem().toString();
            String scheduledDate = scheduledDateEditText.getText().toString();
            String reminderTime = reminderTimeEditText.getText().toString();

            // Nếu không chọn Project hay Label thì dùng giá trị null
            if (projectName.equals("Select Project")) {
                projectName = null;  // Nếu không chọn, gán là null
            }
            if (labelTitle.equals("Select Label")) {
                labelTitle = null;  // Nếu không chọn, gán là null
            }

            // TODO: Nếu bạn có ViewModel hoặc API để tạo task:
            // 1. Chuyển projectName & labelTitle sang ID (nếu cần)
            // 2. Tạo object TaskRequest và gọi API để lưu task
            // 3. Hiển thị thông báo thành công hoặc lỗi
            // 4. Đóng dialog nếu thành công

            Log.d("SAVE_TASK", "Title: " + title);
            Log.d("SAVE_TASK", "Description: " + description);
            Log.d("SAVE_TASK", "Priority: " + priority);
            Log.d("SAVE_TASK", "Project: " + projectName);
            Log.d("SAVE_TASK", "Label: " + labelTitle);
            Log.d("SAVE_TASK", "Scheduled Date: " + scheduledDate);
            Log.d("SAVE_TASK", "Reminder Time: " + reminderTime);

            // Tạm thời chỉ đóng dialog
            dialog.dismiss();
        });

        // Hiển thị dialog
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }
    }


}