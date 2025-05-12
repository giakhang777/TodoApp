package com.example.apptodo.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.viewmodel.SharedUserViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExpiredTasksFragment extends Fragment implements TaskAdapter.OnTaskStatusUpdatedListener {
    private RecyclerView recyclerView;
    private TextView emptyTasksText;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> taskList = new ArrayList<>();
    private TaskService taskService;
    private SharedUserViewModel sharedUserViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_tab_task, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);
        if (emptyTasksText == null) {
            Toast.makeText(getContext(), "Error: emptyTasksText not found in layout", Toast.LENGTH_SHORT).show();
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), taskList, null, this);
        recyclerView.setAdapter(taskAdapter);

        // Sử dụng SharedUserViewModel để lấy userId
        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null && userResponse.getId() != null && isAdded()) {
                int userId = userResponse.getId(); // Lấy userId trực tiếp từ UserResponse
                taskService = RetrofitClient.getTaskService();
                loadExpiredTasks(userId);
            } else {
                Toast.makeText(getContext(), "User not logged in or data unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loadExpiredTasks(int userId) {
        if (taskService == null) {
            return;
        }

        taskService.getTasksByUser(userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<TaskResponse>> call, @NonNull Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.clear();
                    LocalDate today = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    for (TaskResponse task : response.body()) {
                        if (task.getDueDate() != null && !Boolean.TRUE.equals(task.getCompleted())) {
                            try {
                                LocalDate dueDate = LocalDate.parse(task.getDueDate(), formatter);
                                if (dueDate.isBefore(today)) {
                                    taskList.add(task);
                                }
                            } catch (Exception e) {
                                // Log lỗi nếu cần: Log.e("ExpiredTasksFragment", "Error parsing date: " + e.getMessage());
                            }
                        }
                    }
                    taskAdapter.setTaskList(taskList);
                    updateEmptyTasksVisibility();
                } else {
                    taskList.clear();
                    taskAdapter.setTaskList(taskList);
                    updateEmptyTasksVisibility();
                    Toast.makeText(getContext(), "Unable to load expired tasks: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TaskResponse>> call, @NonNull Throwable t) {
                taskList.clear();
                taskAdapter.setTaskList(taskList);
                updateEmptyTasksVisibility();
                Toast.makeText(getContext(), "Error loading expired tasks: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onTaskStatusUpdated() {
        // Làm mới danh sách khi trạng thái task thay đổi
        if (sharedUserViewModel.getUser().getValue() != null && isAdded()) {
            int userId = sharedUserViewModel.getUser().getValue().getId(); // Lấy userId trực tiếp từ UserResponse
            loadExpiredTasks(userId);
        }
    }

    private void updateEmptyTasksVisibility() {
        if (emptyTasksText != null) {
            if (taskList.isEmpty()) {
                emptyTasksText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyTasksText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (taskAdapter != null) {
            taskAdapter.cleanup();
        }
        recyclerView = null;
        emptyTasksText = null;
        taskAdapter = null;
    }
}