package com.example.apptodo.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.apptodo.R;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;
import com.example.apptodo.viewmodel.ProjectViewModel;

import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnItemClickListener, TaskAdapter.OnTaskStatusUpdatedListener {

    private static final String ARG_PROJECT_ID = "projectId";
    private Integer projectId;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> taskList = new ArrayList<>();
    private TaskViewModel taskViewModel;
    private SharedUserViewModel sharedUserViewModel;
    private ProjectViewModel projectViewModel;
    private TextView emptyTasksText;
    private TextView projectTitleTextView;


    public TasksFragment() {
    }

    public static TasksFragment newInstance(int projectId) {
        TasksFragment fragment = new TasksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getInt(ARG_PROJECT_ID, -1);
            if (projectId == -1) projectId = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        tasksRecyclerView = view.findViewById(R.id.recyclerViewTasks);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), new ArrayList<>(), this, this);
        tasksRecyclerView.setAdapter(taskAdapter);

        if (projectTitleTextView != null) {
            projectTitleTextView.setText("Loading Tasks...");
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskList.clear();
            if (tasks != null) {
                taskList.addAll(tasks);
            }
            taskAdapter.setTaskList(taskList);
            updateEmptyTasksVisibility();
        });

        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                if (taskList.isEmpty()) {
                    Toast.makeText(getContext(), "Error loading tasks: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });

        taskViewModel.getTaskOperationResult().observe(getViewLifecycleOwner(), taskResponse -> {
            UserResponse currentUser = sharedUserViewModel.getUser().getValue();
            if (currentUser != null && currentUser.getId() != null && isAdded()) {
                if (projectId != null) {
                    taskViewModel.loadTasksByProject(projectId);
                }
            }
        });


        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null && userResponse.getId() != null && isAdded()) {
                int userId = userResponse.getId();
                if (projectId != null) {
                    taskViewModel.loadTasksByProject(projectId);
                    projectViewModel.getProjectById(projectId);
                }
            } else {
                if (isAdded()) {
                    Toast.makeText(getContext(), "User not logged in or data unavailable. Cannot load tasks.", Toast.LENGTH_LONG).show();
                }
                taskList.clear();
                taskAdapter.setTaskList(taskList);
                updateEmptyTasksVisibility();
            }
        });
    }

    private void updateEmptyTasksVisibility() {
        boolean isEmpty = taskList.isEmpty();
        if (emptyTasksText != null && tasksRecyclerView != null) {
            emptyTasksText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            tasksRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(TaskResponse task) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onItemClick(task);
        }
    }

    @Override
    public void onTaskStatusUpdated(int taskId, boolean completed) {
        if (sharedUserViewModel.getUser().getValue() != null && isAdded()) {
            taskViewModel.changeTaskStatus(taskId, completed);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (taskAdapter != null) {
            taskAdapter.cleanup();
        }
        tasksRecyclerView = null;
        emptyTasksText = null;
        taskAdapter = null;
        projectTitleTextView = null;
    }
}
