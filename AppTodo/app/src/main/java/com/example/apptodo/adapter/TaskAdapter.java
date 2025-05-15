package com.example.apptodo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptodo.R;
import com.example.apptodo.model.request.SubTaskRequest;
import com.example.apptodo.model.response.SubTaskResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.SubTaskViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskResponse> taskList;
    private OnItemClickListener listener;
    private OnTaskStatusUpdatedListener statusUpdatedListener;
    private Context context;
    private SubTaskViewModel subTaskViewModel;
    private TaskViewModel taskViewModel;
    private LifecycleOwner lifecycleOwner;
    private Map<Integer, List<SubTaskResponse>> subTaskCache = new HashMap<>();
    private Map<Integer, Boolean> expansionState = new HashMap<>();
    private final Map<Integer, Boolean> isProcessingParentTask = new HashMap<>();

    public interface OnItemClickListener {
        void onItemClick(TaskResponse task);
    }

    public interface OnTaskStatusUpdatedListener {
        void onTaskStatusUpdated(int taskId, boolean completed);
    }

    public TaskAdapter(Context context, List<TaskResponse> taskList, OnItemClickListener listener, OnTaskStatusUpdatedListener statusUpdatedListener) {
        this.context = context;
        this.taskList = taskList != null ? new ArrayList<>(taskList) : new ArrayList<>();
        this.listener = listener;
        this.statusUpdatedListener = statusUpdatedListener;

        if (context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            this.subTaskViewModel = new ViewModelProvider(activity).get(SubTaskViewModel.class);
            this.taskViewModel = new ViewModelProvider(activity).get(TaskViewModel.class);
            this.lifecycleOwner = activity;
        } else if (context instanceof LifecycleOwner) {
            this.lifecycleOwner = (LifecycleOwner) context;
            if (this.lifecycleOwner instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) this.lifecycleOwner;
                this.subTaskViewModel = new ViewModelProvider(activity).get(SubTaskViewModel.class);
                this.taskViewModel = new ViewModelProvider(activity).get(TaskViewModel.class);
            }
        }
        setupObservers();
    }

    private void setupObservers() {
        if (lifecycleOwner == null || subTaskViewModel == null || taskViewModel == null) {
            return;
        }

        subTaskViewModel.getSubtaskOperationResult().observe(lifecycleOwner, subTaskOpResult -> {
            if (subTaskOpResult == null) return;
            Integer parentTaskIdForOperatedSubtask = subTaskViewModel.getCurrentParentTaskId();
            if (parentTaskIdForOperatedSubtask != null) {
                if (!isProcessingParentTask.getOrDefault(parentTaskIdForOperatedSubtask, false)) {
                    isProcessingParentTask.put(parentTaskIdForOperatedSubtask, true);
                    taskViewModel.getTaskById(parentTaskIdForOperatedSubtask);
                }
            }
        });

        taskViewModel.getSingleTask().observe(lifecycleOwner, updatedTask -> {
            if (updatedTask != null && updatedTask.getId() != null) {
                isProcessingParentTask.remove(updatedTask.getId());
                if (expansionState.getOrDefault(updatedTask.getId(), false)) {
                    subTaskViewModel.currentParentTaskId = updatedTask.getId();
                    subTaskViewModel.loadSubtasksForTask(updatedTask.getId());
                }
            }
        });

        subTaskViewModel.getSubtasks().observe(lifecycleOwner, subTaskResponses -> {
            Integer parentIdInVM = subTaskViewModel.getCurrentParentTaskId();
            if (parentIdInVM == null || subTaskResponses == null) return;
            subTaskCache.put(parentIdInVM, new ArrayList<>(subTaskResponses));
            for (int i = 0; i < taskList.size(); i++) {
                if (taskList.get(i).getId().equals(parentIdInVM)) {
                    notifyItemChanged(i, "SUBTASKS_UPDATED");
                    break;
                }
            }
        });

        taskViewModel.getTasks().observe(lifecycleOwner, newTasks -> {
            if (newTasks != null) {
                this.taskList.clear();
                this.taskList.addAll(newTasks);
                notifyDataSetChanged();
            }
        });

        taskViewModel.getErrorMessage().observe(lifecycleOwner, error -> {
            if (error != null && !error.isEmpty() && context != null) {
                // Toast.makeText(context, "TaskViewModel Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
        subTaskViewModel.getErrorMessage().observe(lifecycleOwner, error -> {
            if (error != null && !error.isEmpty() && context != null) {
                // Toast.makeText(context, "SubTaskViewModel Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tasks, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = taskList.get(position);
        if (task == null || task.getId() == null) return;
        holder.bind(task, expansionState.getOrDefault(task.getId(), false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            TaskResponse task = taskList.get(position);
            if (task == null || task.getId() == null) return;

            if (payloads.contains("SUBTASKS_UPDATED")) {
                if (holder.subTaskAdapter != null) {
                    List<SubTaskResponse> cachedSubtasks = subTaskCache.get(task.getId());
                    if (cachedSubtasks != null) {
                        holder.subTaskAdapter.setSubTaskList(cachedSubtasks);
                        holder.updateSubTaskCheckboxColor();
                    }
                }
                if (expansionState.getOrDefault(task.getId(), false)) {
                    holder.recyclerViewSubTasks.setVisibility(View.VISIBLE);
                } else {
                    holder.recyclerViewSubTasks.setVisibility(View.GONE);
                }
            } else if (payloads.contains("EXPANSION_CHANGED")) {
                holder.handleExpansion(task, expansionState.getOrDefault(task.getId(), false));
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public void setTaskList(List<TaskResponse> newTaskList) {
        this.taskList.clear();
        if (newTaskList != null) {
            this.taskList.addAll(new ArrayList<>(newTaskList));
        }
        isProcessingParentTask.clear();
        expansionState.clear();
        subTaskCache.clear();
        notifyDataSetChanged();
    }

    public void cleanup() {
    }

    private void showAddSubTaskDialog(TaskResponse parentTask) {
        if (context == null || subTaskViewModel == null || parentTask == null || parentTask.getId() == null) {
            if (context != null) {
                Toast.makeText(context, "Cannot add subtask: Missing information.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_subtask, null);
        builder.setView(dialogView);

        final EditText subTaskNameEditText = dialogView.findViewById(R.id.et_subtask_name);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String subTaskTitle = subTaskNameEditText.getText().toString().trim();
            if (!subTaskTitle.isEmpty()) {
                SubTaskRequest subTaskRequest = new SubTaskRequest(subTaskTitle, parentTask.getId());
                subTaskViewModel.currentParentTaskId = parentTask.getId();
                subTaskViewModel.createSubTask(subTaskRequest);
            } else {
                Toast.makeText(context, "Subtask title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameProject, tvNameTask, tvDescTask, tvDueDate, tvDueTime, tvNameLabel, tvAddSubTask;
        RadioButton statusButton;
        ImageButton imageButtonExpand, btnAddSubTask;
        RecyclerView recyclerViewSubTasks;
        LinearLayout linearLayout;
        SubTaskAdapter subTaskAdapter;
        int currentButtonTintColor;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameProject = itemView.findViewById(R.id.nameProject);
            tvNameTask = itemView.findViewById(R.id.nameTask);
            tvDescTask = itemView.findViewById(R.id.descTask);
            tvDueDate = itemView.findViewById(R.id.dueDate);
            tvDueTime = itemView.findViewById(R.id.dueTime);
            tvNameLabel = itemView.findViewById(R.id.nameLabel);
            statusButton = itemView.findViewById(R.id.statusButton);
            imageButtonExpand = itemView.findViewById(R.id.imageButton);
            recyclerViewSubTasks = itemView.findViewById(R.id.recyclerViewSubTasks);
            linearLayout = itemView.findViewById(R.id.layoutTask);
            btnAddSubTask = itemView.findViewById(R.id.btnAddSubTask);
            tvAddSubTask = itemView.findViewById(R.id.tvAddSubTask);

            recyclerViewSubTasks.setLayoutManager(new LinearLayoutManager(itemView.getContext()));

            View.OnClickListener addSubTaskClickListener = v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
                    TaskResponse parentTask = taskList.get(position);
                    showAddSubTaskDialog(parentTask);
                }
            };

            if (btnAddSubTask != null) {
                btnAddSubTask.setOnClickListener(addSubTaskClickListener);
            }
            if (tvAddSubTask != null) {
                tvAddSubTask.setOnClickListener(addSubTaskClickListener);
            }
        }

        void bind(TaskResponse task, boolean isExpanded) {
            if (task == null || task.getId() == null) return;

            tvNameProject.setText(task.getProject() != null ? task.getProject() : "");
            tvNameTask.setText(task.getTitle());
            tvDescTask.setText(task.getDescription());
            tvDueDate.setText(task.getDueDate() != null && !task.getDueDate().isEmpty() ? task.getDueDate() : "No Due Date");
            tvDueTime.setText(task.getReminderTime() != null ? task.getReminderTime() : "No Time");
            tvNameLabel.setText(task.getLabel() != null ? task.getLabel() : "");

            statusButton.setOnCheckedChangeListener(null);
            statusButton.setChecked(Boolean.TRUE.equals(task.getCompleted()));
            statusButton.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= taskList.size()) return;
                TaskResponse taskToUpdate = taskList.get(currentPosition);
                boolean isNowChecked = !Boolean.TRUE.equals(taskToUpdate.getCompleted());
                if (statusUpdatedListener != null && taskToUpdate.getId() != null) {
                    statusUpdatedListener.onTaskStatusUpdated(taskToUpdate.getId(), isNowChecked);
                }
            });

            String priority = task.getPriority() != null ? task.getPriority().toLowerCase() : "low";
            switch (priority) {
                case "high":
                    linearLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high));
                    if (imageButtonExpand != null) imageButtonExpand.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high));
                    if (btnAddSubTask != null) btnAddSubTask.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high));
                    currentButtonTintColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_high_dark);
                    break;
                case "medium":
                    linearLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium));
                    if (imageButtonExpand != null) imageButtonExpand.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium));
                    if (btnAddSubTask != null) btnAddSubTask.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium));
                    currentButtonTintColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_medium_dark);
                    break;
                default:
                    linearLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low));
                    if (imageButtonExpand != null) imageButtonExpand.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low));
                    if (btnAddSubTask != null) btnAddSubTask.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low));
                    currentButtonTintColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_low_dark);
                    break;
            }
            statusButton.setButtonTintList(ColorStateList.valueOf(currentButtonTintColor));

            if (subTaskAdapter == null || (task.getId() != null && !task.getId().equals(subTaskAdapter.parentTaskId))) {
                subTaskAdapter = new SubTaskAdapter(
                        itemView.getContext(),
                        new ArrayList<SubTaskResponse>(), // SỬA Ở ĐÂY
                        subTaskViewModel,
                        task.getId(),
                        (subTaskItem, isChecked) -> {
                            int currentTaskPosition = getAdapterPosition();
                            if (currentTaskPosition != RecyclerView.NO_POSITION && currentTaskPosition < taskList.size()) {
                                TaskResponse parentTaskResponse = taskList.get(currentTaskPosition);
                                if (subTaskViewModel != null && subTaskItem.getId() != null && parentTaskResponse != null && parentTaskResponse.getId() != null) {
                                    subTaskViewModel.currentParentTaskId = parentTaskResponse.getId();
                                    subTaskViewModel.changeSubTaskStatus(subTaskItem.getId(), isChecked);
                                }
                            }
                        },
                        currentButtonTintColor
                );
                recyclerViewSubTasks.setAdapter(subTaskAdapter);
            } else {
                subTaskAdapter.setParentTaskButtonColor(currentButtonTintColor);
            }
            updateSubTaskCheckboxColor();

            handleExpansion(task, isExpanded);

            itemView.setOnClickListener(v -> {
                int clickedPosition = getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION && clickedPosition < taskList.size() && listener != null) {
                    listener.onItemClick(taskList.get(clickedPosition));
                }
            });
        }

        void handleExpansion(TaskResponse task, boolean isExpanded) {
            if (imageButtonExpand != null) {
                imageButtonExpand.setOnClickListener(v -> {
                    int currentTaskPosition = getAdapterPosition();
                    if (currentTaskPosition != RecyclerView.NO_POSITION && currentTaskPosition < taskList.size()) {
                        TaskResponse currentTask = taskList.get(currentTaskPosition);
                        boolean newExpandedState = !expansionState.getOrDefault(currentTask.getId(), false);
                        expansionState.put(currentTask.getId(), newExpandedState);
                        notifyItemChanged(currentTaskPosition, "EXPANSION_CHANGED");
                    }
                });

                if (isExpanded) {
                    imageButtonExpand.setImageResource(R.drawable.baseline_expand_less_24);
                    recyclerViewSubTasks.setVisibility(View.VISIBLE);

                    if (subTaskAdapter == null || (task.getId() != null && !task.getId().equals(subTaskAdapter.parentTaskId))) {
                        subTaskAdapter = new SubTaskAdapter(
                                itemView.getContext(),
                                new ArrayList<SubTaskResponse>(), // SỬA Ở ĐÂY
                                subTaskViewModel,
                                task.getId(),
                                (subTaskItem, isChecked) -> {
                                    int currentTaskPosition = getAdapterPosition();
                                    if (currentTaskPosition != RecyclerView.NO_POSITION && currentTaskPosition < taskList.size()) {
                                        TaskResponse parentTaskResponse = taskList.get(currentTaskPosition);
                                        if (subTaskViewModel != null && subTaskItem.getId() != null && parentTaskResponse != null && parentTaskResponse.getId() != null) {
                                            subTaskViewModel.currentParentTaskId = parentTaskResponse.getId();
                                            subTaskViewModel.changeSubTaskStatus(subTaskItem.getId(), isChecked);
                                        }
                                    }
                                },
                                currentButtonTintColor
                        );
                        recyclerViewSubTasks.setAdapter(subTaskAdapter);
                    }

                    List<SubTaskResponse> cachedSubtasks = subTaskCache.get(task.getId());
                    if (cachedSubtasks != null) {
                        if (subTaskAdapter != null) { // Kiểm tra null trước khi sử dụng
                            subTaskAdapter.setSubTaskList(cachedSubtasks);
                        }
                    } else if (subTaskViewModel != null && task.getId() != null) {
                        subTaskViewModel.currentParentTaskId = task.getId();
                        subTaskViewModel.loadSubtasksForTask(task.getId());
                    }
                    updateSubTaskCheckboxColor();
                } else {
                    imageButtonExpand.setImageResource(R.drawable.baseline_expand_more_24);
                    recyclerViewSubTasks.setVisibility(View.GONE);
                }
            }
        }

        void updateSubTaskCheckboxColor() {
            if (subTaskAdapter != null) {
                subTaskAdapter.setParentTaskButtonColor(currentButtonTintColor);
            }
        }
    }
}
