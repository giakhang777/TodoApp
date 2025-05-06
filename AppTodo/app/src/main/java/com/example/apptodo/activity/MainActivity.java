package com.example.apptodo.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.apptodo.R;
import com.example.apptodo.databinding.ActivityMainBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Mặc định mở Fragment Home
        replaceFragment(new HomeFragment());

        // Xử lý sự kiện khi chọn menu trong BottomNavigationView
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
        FloatingActionButton fab = findViewById(R.id.fab); // Chính xác
        // Sự kiện cho FloatingActionButton
        fab.setOnClickListener(view -> showBottomDialog());


    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomsheetlayout);

        // Lấy View root từ dialog
        View view = dialog.findViewById(android.R.id.content);

        // Nút đóng
        ImageView cancelButton = view.findViewById(R.id.closeButton);
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        // Tìm Spinner đúng từ layout của dialog
        Spinner prioritySpinner = view.findViewById(R.id.prioritySpinner);
        Spinner projectSpinner = view.findViewById(R.id.projectSpinner);
        Spinner labelSpinner = view.findViewById(R.id.labelSpinner);

        // Dữ liệu mẫu
        String[] priorities = {"Low", "Medium", "High"};
        String[] projects = {"Personal", "Work", "School"};
        String[] labels = {"Urgent", "Important", "Optional"};

        // Adapter setup
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        prioritySpinner.setAdapter(priorityAdapter);

        ArrayAdapter<String> projectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, projects);
        projectSpinner.setAdapter(projectAdapter);

        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        labelSpinner.setAdapter(labelAdapter);

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
