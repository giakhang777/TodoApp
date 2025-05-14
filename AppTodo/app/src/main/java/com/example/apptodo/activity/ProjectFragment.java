package com.example.apptodo.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.ProjectViewModel;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;
import java.util.ArrayList;
import java.util.List;

public class ProjectFragment extends Fragment implements TaskAdapter.OnItemClickListener, TaskAdapter.OnTaskStatusUpdatedListener {

    private static final String ARG_PROJECT_ID = "project_id";
    private Integer projectId;

    private TextView projectTitleTextView;
    private RecyclerView recyclerViewTasks;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> taskList = new ArrayList<>();
    private TextView emptyTasksText;

    private ProjectViewModel projectViewModel;
    private TaskViewModel taskViewModel;
    private SharedUserViewModel sharedUserViewModel;


    public ProjectFragment() {
        // Required empty public constructor
    }

    public static ProjectFragment newInstance(int projectId) {
        ProjectFragment fragment = new ProjectFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_PROJECT_ID)) {
                projectId = getArguments().getInt(ARG_PROJECT_ID);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project, container, false);
        projectTitleTextView = view.findViewById(R.id.projectTitleTextView);
        recyclerViewTasks = view.findViewById(R.id.recyclerViewTasks);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);

        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), taskList, this, this);
        recyclerViewTasks.setAdapter(taskAdapter);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);

        projectViewModel.getSingleProject().observe(getViewLifecycleOwner(), project -> {
            if (project != null && project.getId() == projectId) {
                projectTitleTextView.setText(project.getName());
            } else if (projectId != null) {
                projectViewModel.getProjectById(projectId); // Fetch if not matching or null
            } else {
                projectTitleTextView.setText("Project Details");
            }
        });

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskList.clear();
            if (tasks != null) {
                taskList.addAll(tasks);
            }
            taskAdapter.setTaskList(taskList);
            updateEmptyTasksVisibility();
        });

        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && taskList.isEmpty()) {
                Toast.makeText(getContext(), "Error: " + errorMessage, Toast.LENGTH_LONG).show();
            }
        });

        taskViewModel.getTaskOperationResult().observe(getViewLifecycleOwner(), taskResponse -> {
            loadTasksForProject();
        });


        if (projectId != null) {
            projectViewModel.getProjectById(projectId);
            loadTasksForProject();
        } else {
            projectTitleTextView.setText("No Project Selected");
            taskList.clear();
            taskAdapter.setTaskList(taskList);
            updateEmptyTasksVisibility();
        }
    }

    private void loadTasksForProject() {
        if (projectId != null && isAdded()) {
            taskViewModel.loadTasksByProject(projectId);
        }
    }

    private void updateEmptyTasksVisibility() {
        if (emptyTasksText != null && recyclerViewTasks != null) {
            if (taskList.isEmpty()) {
                emptyTasksText.setVisibility(View.VISIBLE);
                recyclerViewTasks.setVisibility(View.GONE);
            } else {
                emptyTasksText.setVisibility(View.GONE);
                recyclerViewTasks.setVisibility(View.VISIBLE);
            }
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
    }
}