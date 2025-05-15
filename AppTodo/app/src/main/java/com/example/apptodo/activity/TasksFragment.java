package com.example.apptodo.activity;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.apptodo.R;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;
import java.util.ArrayList;
import java.util.List;

public class TasksFragment extends Fragment implements TaskAdapter.OnItemClickListener, TaskAdapter.OnTaskStatusUpdatedListener {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> allTaskList = new ArrayList<>();
    private List<TaskResponse> displayedTaskList = new ArrayList<>();
    private TaskViewModel taskViewModel;
    private SharedUserViewModel sharedUserViewModel;
    private TextView emptyTasksText;
    private TextView fragmentTitleTextView;
    private LinearLayout searchBarLayout;
    private EditText editSearch;
    private Button btnCancelSearch;

    public TasksFragment() {
    }

    public static TasksFragment newInstance() {
        return new TasksFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        tasksRecyclerView = view.findViewById(R.id.recyclerViewTasks);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);
        fragmentTitleTextView = view.findViewById(R.id.projectTitleTextView);
        searchBarLayout = view.findViewById(R.id.search_bar_layout);
        editSearch = view.findViewById(R.id.editSearch);
        btnCancelSearch = view.findViewById(R.id.btnCancelSearch);

        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), displayedTaskList, this, this);
        tasksRecyclerView.setAdapter(taskAdapter);

        // Ban đầu ẩn danh sách task và text "không có task"
        tasksRecyclerView.setVisibility(View.GONE);
        emptyTasksText.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        setupSearch();

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTaskList.clear();
            if (tasks != null) {
                allTaskList.addAll(tasks);
            }
            // Chỉ lọc lại nếu query hiện tại không rỗng
            String currentQuery = editSearch.getText().toString();
            if (!currentQuery.isEmpty()) {
                filterTasksBySearch(currentQuery);
            } else {
                // Nếu query rỗng, đảm bảo displayedTaskList rỗng và UI được cập nhật đúng
                displayedTaskList.clear();
                if (taskAdapter != null) {
                    taskAdapter.notifyDataSetChanged();
                }
                updateEmptyTasksVisibility(false); // false vì không có query -> không hiển thị gì
            }
        });

        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                if (displayedTaskList.isEmpty() && (editSearch == null || editSearch.getText().toString().isEmpty())) {
                    // Chỉ hiển thị lỗi nếu không có task nào đang được hiển thị và không có query
                } else if (!allTaskList.isEmpty() && displayedTaskList.isEmpty() && editSearch != null && !editSearch.getText().toString().isEmpty()){
                    // Hiển thị lỗi nếu có query nhưng không tìm thấy và allTaskList không rỗng (nghĩa là API thành công nhưng query ko match)
                }
                else if (allTaskList.isEmpty() && (editSearch == null || editSearch.getText().toString().isEmpty())) {
                    Toast.makeText(getContext(), "Error loading tasks: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });

        taskViewModel.getTaskOperationResult().observe(getViewLifecycleOwner(), taskResponse -> {
            loadAllUserTasks();
        });

        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null && userResponse.getId() != null && isAdded()) {
                loadAllUserTasks();
            } else {
                if (isAdded()) {
                    Toast.makeText(getContext(), "User not logged in. Cannot load tasks.", Toast.LENGTH_LONG).show();
                }
                allTaskList.clear();
                filterTasksBySearch("");
            }
        });
    }

    private void loadAllUserTasks() {
        UserResponse currentUser = sharedUserViewModel.getUser().getValue();
        if (currentUser != null && currentUser.getId() != null && isAdded()) {
            taskViewModel.loadAllTasks(currentUser.getId());
        }
    }

    private void setupSearch() {
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTasksBySearch(s.toString());
                btnCancelSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnCancelSearch.setOnClickListener(v -> {
            editSearch.setText("");
        });
    }

    private void filterTasksBySearch(String query) {
        displayedTaskList.clear();
        String lowerCaseQuery = query.toLowerCase().trim();

        if (lowerCaseQuery.isEmpty()) {
            // Không làm gì cả, displayedTaskList đã được xóa
            // RecyclerView và emptyTasksText sẽ được ẩn
            updateEmptyTasksVisibility(false); // false = không hiển thị gì cả
        } else {
            for (TaskResponse task : allTaskList) {
                boolean titleMatches = task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerCaseQuery);
                boolean descriptionMatches = task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerCaseQuery);
                boolean projectMatches = task.getProject() != null && task.getProject().toLowerCase().contains(lowerCaseQuery);
                boolean labelMatches = task.getLabel() != null && task.getLabel().toLowerCase().contains(lowerCaseQuery);

                if (titleMatches || descriptionMatches || projectMatches || labelMatches) {
                    displayedTaskList.add(task);
                }
            }
            updateEmptyTasksVisibility(true); // true = hiển thị RecyclerView hoặc emptyTasksText dựa trên displayedTaskList
        }

        if (taskAdapter != null) {
            taskAdapter.notifyDataSetChanged();
        }
    }

    // Thêm một tham số để quyết định có nên hiển thị dựa trên query hay không
    private void updateEmptyTasksVisibility(boolean shouldShowBasedOnQuery) {
        if (!shouldShowBasedOnQuery) {
            // Nếu không nên hiển thị dựa trên query (ví dụ query rỗng), ẩn cả hai
            if (emptyTasksText != null) emptyTasksText.setVisibility(View.GONE);
            if (tasksRecyclerView != null) tasksRecyclerView.setVisibility(View.GONE);
            return;
        }

        // Nếu nên hiển thị dựa trên query, kiểm tra displayedTaskList
        boolean isEmpty = displayedTaskList.isEmpty();
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
        fragmentTitleTextView = null;
        searchBarLayout = null;
        editSearch = null;
        btnCancelSearch = null;
        allTaskList.clear();
        displayedTaskList.clear();
    }
}