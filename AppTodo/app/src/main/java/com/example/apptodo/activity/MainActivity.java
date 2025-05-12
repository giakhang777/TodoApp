package com.example.apptodo.activity;

import android.app.Dialog;
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
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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

        String[] priorities = {"Low", "Medium", "High"};
        String[] projects = {"Personal", "Work", "School"};
        String[] labels = {"Urgent", "Important", "Optional"};

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, priorities);
        prioritySpinner.setAdapter(priorityAdapter);

        ArrayAdapter<String> projectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, projects);
        projectSpinner.setAdapter(projectAdapter);

        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        labelSpinner.setAdapter(labelAdapter);

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);
        }
    }
}