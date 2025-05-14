package com.example.apptodo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final Map<Integer, Boolean> isProcessing = new HashMap<>();

    public interface OnItemClickListener {
        void onItemClick(TaskResponse task);
    }

    public interface OnTaskStatusUpdatedListener {
        void onTaskStatusUpdated(int taskId, boolean completed);
    }

    public TaskAdapter(Context context, List<TaskResponse> taskList, OnItemClickListener listener, OnTaskStatusUpdatedListener statusUpdatedListener) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
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
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (subTaskViewModel == null && context instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) context;
            this.subTaskViewModel = new ViewModelProvider(activity).get(SubTaskViewModel.class);
            this.taskViewModel = new ViewModelProvider(activity).get(TaskViewModel.class);
            this.lifecycleOwner = activity;
        } else if (subTaskViewModel == null && lifecycleOwner instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) lifecycleOwner;
            this.subTaskViewModel = new ViewModelProvider(activity).get(SubTaskViewModel.class);
            this.taskViewModel = new ViewModelProvider(activity).get(TaskViewModel.class);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_tasks, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = taskList.get(position);
        if (task == null || task.getId() == null) return;

        Integer taskId = task.getId();
        holder.bind(task, expansionState.getOrDefault(taskId, false));

        holder.statusButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= taskList.size()) return;
            TaskResponse taskToUpdate = taskList.get(currentPosition);
            boolean isNowChecked = !(Boolean.TRUE.equals(taskToUpdate.getCompleted()));
            if (statusUpdatedListener != null && taskToUpdate.getId() != null) {
                statusUpdatedListener.onTaskStatusUpdated(taskToUpdate.getId(), isNowChecked);
            }
        });

        holder.imageButton.setOnClickListener(v -> {
            boolean isCurrentlyExpanded = expansionState.getOrDefault(taskId, false);
            boolean newExpandedState = !isCurrentlyExpanded;
            expansionState.put(taskId, newExpandedState);

            if (newExpandedState) {
                holder.recyclerViewSubTasks.setVisibility(View.VISIBLE);
                holder.imageButton.setImageResource(R.drawable.baseline_expand_less_24);
                if (subTaskViewModel != null) {
                    subTaskViewModel.loadSubtasksForTask(taskId);
                }
            } else {
                holder.recyclerViewSubTasks.setVisibility(View.GONE);
                holder.imageButton.setImageResource(R.drawable.baseline_expand_more_24);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION && clickedPosition < taskList.size() && listener != null) {
                listener.onItemClick(taskList.get(clickedPosition));
            }
        });

        if (lifecycleOwner != null && subTaskViewModel != null && task.getId() != null) {
            final Integer finalTaskId = taskId; // Sử dụng final để tránh xung đột trong lambda
            subTaskViewModel.getSubtasks().observe(lifecycleOwner, subTaskResponses -> {
                if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && holder.getAdapterPosition() < taskList.size()) {
                    TaskResponse currentTaskForHolder = taskList.get(holder.getAdapterPosition());
                    Integer currentParentIdInVM = subTaskViewModel.getCurrentParentTaskId();
                    Log.d("TaskAdapter", "Updating subtasks for task ID: " + currentTaskForHolder.getId() + ", parentId: " + currentParentIdInVM);

                    if (currentParentIdInVM != null && currentTaskForHolder.getId().equals(currentParentIdInVM)) {
                        holder.subTaskAdapter.setSubTaskList(subTaskResponses);
                        subTaskCache.put(currentTaskForHolder.getId(), new ArrayList<>(subTaskResponses != null ? subTaskResponses : new ArrayList<>()));
                        if (expansionState.getOrDefault(currentTaskForHolder.getId(), false)) {
                            holder.recyclerViewSubTasks.setVisibility(View.VISIBLE);
                        } else {
                            holder.recyclerViewSubTasks.setVisibility(View.GONE);
                        }
                    }
                }
            });

            subTaskViewModel.getErrorMessage().observe(lifecycleOwner, error -> {
                if (error != null && !error.isEmpty()) {
                    Log.e("TaskAdapter", "Error: " + error);
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                }
            });

            subTaskViewModel.getSubtaskOperationResult().observe(lifecycleOwner, subTaskOpResult -> {
                if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && holder.getAdapterPosition() < taskList.size()) {
                    TaskResponse currentTaskAtEvent = taskList.get(holder.getAdapterPosition());
                    if (currentTaskAtEvent != null && currentTaskAtEvent.getId() != null) {
                        Integer parentOfOperatedSubtask = subTaskViewModel.getCurrentParentTaskId();
                        Log.d("TaskAdapter", "Operation result for task ID: " + currentTaskAtEvent.getId() + ", parentId: " + parentOfOperatedSubtask);
                        if (parentOfOperatedSubtask != null && parentOfOperatedSubtask.equals(currentTaskAtEvent.getId()) && !isProcessing.getOrDefault(currentTaskAtEvent.getId(), false)) {
                            isProcessing.put(currentTaskAtEvent.getId(), true);
                            taskViewModel.getTaskById(currentTaskAtEvent.getId());
                        }
                    }
                }
            });

            taskViewModel.getSingleTask().observe(lifecycleOwner, updatedTask -> {
                if (updatedTask != null && updatedTask.getId() != null) {
                    isProcessing.remove(updatedTask.getId());
                    Log.d("TaskAdapter", "Task updated: " + updatedTask.getId());
                }
            });

            taskViewModel.getErrorMessage().observe(lifecycleOwner, error -> {
                if (error != null && !error.isEmpty()) {
                    Log.e("TaskAdapter", "Task error: " + error);
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull TaskViewHolder holder) {
        super.onViewRecycled(holder);
        if (lifecycleOwner != null && subTaskViewModel != null) {
            subTaskViewModel.getSubtasks().removeObservers(lifecycleOwner);
            subTaskViewModel.getSubtaskOperationResult().removeObservers(lifecycleOwner);
            subTaskViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        }
        if (lifecycleOwner != null && taskViewModel != null) {
            taskViewModel.getSingleTask().removeObservers(lifecycleOwner);
            taskViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public void setTaskList(List<TaskResponse> newTaskList) {
        this.taskList = newTaskList != null ? new ArrayList<>(newTaskList) : new ArrayList<>();
        subTaskCache.clear();
        expansionState.clear();
        isProcessing.clear();
        notifyDataSetChanged();
    }

    public void cleanup() {
        if (lifecycleOwner != null && subTaskViewModel != null) {
            subTaskViewModel.getSubtasks().removeObservers(lifecycleOwner);
            subTaskViewModel.getSubtaskOperationResult().removeObservers(lifecycleOwner);
            subTaskViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        }
        if (lifecycleOwner != null && taskViewModel != null) {
            taskViewModel.getSingleTask().removeObservers(lifecycleOwner);
            taskViewModel.getErrorMessage().removeObservers(lifecycleOwner);
        }
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameProject, tvNameTask, tvDescTask, tvDueDate, tvDueTime, tvNameLabel;
        RadioButton statusButton;
        ImageButton imageButton, btnAddSubTask;
        RecyclerView recyclerViewSubTasks;
        LinearLayout linearLayout;
        SubTaskAdapter subTaskAdapter;
        int buttonTintColor;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameProject = itemView.findViewById(R.id.nameProject);
            tvNameTask = itemView.findViewById(R.id.nameTask);
            tvDescTask = itemView.findViewById(R.id.descTask);
            tvDueDate = itemView.findViewById(R.id.dueDate);
            tvDueTime = itemView.findViewById(R.id.dueTime);
            tvNameLabel = itemView.findViewById(R.id.nameLabel);
            statusButton = itemView.findViewById(R.id.statusButton);
            imageButton = itemView.findViewById(R.id.imageButton);
            recyclerViewSubTasks = itemView.findViewById(R.id.recyclerViewSubTasks);
            linearLayout = itemView.findViewById(R.id.layoutTask);
            btnAddSubTask = itemView.findViewById(R.id.btnAddSubTask);

            recyclerViewSubTasks.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            subTaskAdapter = new SubTaskAdapter(new ArrayList<>(), (subTask, isChecked) -> {
                if (subTaskViewModel != null && subTask.getId() != null) {
                    subTaskViewModel.changeSubTaskStatus(subTask.getId(), isChecked);
                }
            }, ContextCompat.getColor(itemView.getContext(), R.color.priority_low_dark));
            recyclerViewSubTasks.setAdapter(subTaskAdapter);
        }

        void bind(TaskResponse task, boolean isExpanded) {
            tvNameProject.setText(task.getProject() != null ? task.getProject() : "");
            tvNameTask.setText(task.getTitle());
            tvDescTask.setText(task.getDescription());
            tvDueDate.setText(task.getDueDate() != null && !task.getDueDate().isEmpty() ? task.getDueDate() : "No Due Date");
            tvDueTime.setText(task.getReminderTime() != null ? task.getReminderTime() : "No Time");
            tvNameLabel.setText(task.getLabel() != null ? task.getLabel() : "");
            statusButton.setChecked(Boolean.TRUE.equals(task.getCompleted()));

            String priority = task.getPriority() != null ? task.getPriority().toLowerCase() : "low";
            switch (priority) {
                case "high":
                    linearLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high));
                    imageButton.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high));
                    btnAddSubTask.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_high));
                    buttonTintColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_high_dark);
                    break;
                case "medium":
                    linearLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium));
                    imageButton.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium));
                    btnAddSubTask.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_medium));
                    buttonTintColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_medium_dark);
                    break;
                default:
                    linearLayout.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low));
                    imageButton.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low));
                    btnAddSubTask.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.priority_low));
                    buttonTintColor = ContextCompat.getColor(itemView.getContext(), R.color.priority_low_dark);
                    break;
            }
            statusButton.setButtonTintList(ColorStateList.valueOf(buttonTintColor));
            subTaskAdapter.setParentTaskButtonColor(buttonTintColor);

            imageButton.setVisibility(View.VISIBLE);

            if (isExpanded) {
                imageButton.setImageResource(R.drawable.baseline_expand_less_24);
                recyclerViewSubTasks.setVisibility(View.VISIBLE);
                if (task.getId() != null && subTaskCache.containsKey(task.getId())) {
                    subTaskAdapter.setSubTaskList(subTaskCache.get(task.getId()));
                } else if (task.getId() != null && subTaskViewModel != null) {
                    subTaskViewModel.loadSubtasksForTask(task.getId());
                }
            } else {
                imageButton.setImageResource(R.drawable.baseline_expand_more_24);
                recyclerViewSubTasks.setVisibility(View.GONE);
            }
        }
    }
}