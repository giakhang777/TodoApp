package com.example.apptodo.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompletedTasksFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyTasksText;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> taskList = new ArrayList<>();
    private TaskService taskService;
    private int userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_tab_task, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);
        if (emptyTasksText == null) {
            Toast.makeText(getContext(), "Error: emptyTasksText not found in layout", Toast.LENGTH_SHORT).show();
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), taskList);
        recyclerView.setAdapter(taskAdapter);

        SharedPreferences prefs = requireActivity().getSharedPreferences("login_prefs", getContext().MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        if (userId != -1 && isAdded()) {
            taskService = RetrofitClient.getTaskService();
            loadCompletedTasks();
        } else {
            Toast.makeText(getContext(), "User not logged in or fragment not attached", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadCompletedTasks() {
        if (taskService == null) {
            return;
        }

        taskService.getTasksByUser(userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<TaskResponse>> call, @NonNull Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.clear();

                    for (TaskResponse task : response.body()) {
                        if (Boolean.TRUE.equals(task.getCompleted())) {
                            taskList.add(task);
                        }
                    }
                    taskAdapter.setTaskList(taskList);
                    updateEmptyTasksVisibility();
                } else {
                    taskList.clear();
                    taskAdapter.setTaskList(taskList);
                    updateEmptyTasksVisibility();
                    Toast.makeText(getContext(), "Unable to load completed tasks", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TaskResponse>> call, @NonNull Throwable t) {
                taskList.clear();
                taskAdapter.setTaskList(taskList);
                updateEmptyTasksVisibility();
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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