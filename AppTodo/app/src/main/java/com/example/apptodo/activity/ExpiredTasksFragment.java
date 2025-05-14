package com.example.apptodo.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.response.TaskResponse; // Import TaskResponse

import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;

import java.time.LocalDate; // Import LocalDate
import java.time.format.DateTimeFormatter; // Import DateTimeFormatter
import java.time.format.DateTimeParseException; // Import DateTimeParseException
import java.util.ArrayList;
import java.util.Collections; // Import Collections
import java.util.Comparator; // Import Comparator
import java.util.List;

public class ExpiredTasksFragment extends Fragment implements TaskAdapter.OnTaskStatusUpdatedListener {
    private RecyclerView recyclerView;
    private TextView emptyTasksText;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> taskList = new ArrayList<>();
    private TaskViewModel taskViewModel;
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

        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), new Observer<List<TaskResponse>>() {
            @Override
            public void onChanged(List<TaskResponse> tasks) {
                taskList.clear();
                List<TaskResponse> expiredTasks = new ArrayList<>();
                if (tasks != null) {
                    LocalDate today = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    for (TaskResponse task : tasks) {
                        if (task.getDueDate() != null && !Boolean.TRUE.equals(task.getCompleted())) {
                            try {
                                LocalDate dueDate = LocalDate.parse(task.getDueDate(), formatter);
                                if (dueDate.isBefore(today)) {
                                    expiredTasks.add(task);
                                }
                            } catch (DateTimeParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                Collections.sort(expiredTasks, new Comparator<TaskResponse>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    @Override
                    public int compare(TaskResponse t1, TaskResponse t2) {
                        if (t1.getDueDate() == null && t2.getDueDate() == null) return 0;
                        if (t1.getDueDate() == null) return 1; // Nulls last
                        if (t2.getDueDate() == null) return -1; // Nulls last

                        try {
                            LocalDate date1 = LocalDate.parse(t1.getDueDate(), formatter);
                            LocalDate date2 = LocalDate.parse(t2.getDueDate(), formatter);
                            return date1.compareTo(date2); // Tăng dần
                        } catch (DateTimeParseException e) {
                            e.printStackTrace();
                            return 0;
                        }
                    }
                });
                taskList.addAll(expiredTasks);
                taskAdapter.setTaskList(taskList);
                updateEmptyTasksVisibility();
            }
        });

        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty() && taskList.isEmpty()) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        taskViewModel.getTaskOperationResult().observe(getViewLifecycleOwner(), taskResponse -> {
            if (sharedUserViewModel.getUser().getValue() != null && isAdded()) {
                int userId = sharedUserViewModel.getUser().getValue().getId();
                taskViewModel.loadAllTasks(userId);
            }
        });


        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null && userResponse.getId() != null && isAdded()) {
                int userId = userResponse.getId();
                taskViewModel.loadAllTasks(userId);
            } else {
                Toast.makeText(getContext(), "User not logged in or data unavailable", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onTaskStatusUpdated(int taskId, boolean completed) {
        if (sharedUserViewModel.getUser().getValue() != null && isAdded()) {
            taskViewModel.changeTaskStatus(taskId, completed);
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
