package com.example.apptodo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log; // Import Log
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.api.SubTaskService;
import com.example.apptodo.model.response.SubTaskResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private static final String TAG = "TaskAdapter"; // Logging Tag

    private List<TaskResponse> taskList;
    private OnItemClickListener listener;
    private OnTaskStatusUpdatedListener statusUpdatedListener;
    private Context context;
    private Map<Integer, Call<List<SubTaskResponse>>> activeCalls = new HashMap<>();
    private SubTaskService subTaskService;

    public interface OnItemClickListener {
        void onItemClick(TaskResponse task);
    }

    public interface OnTaskStatusUpdatedListener {
        void onTaskStatusUpdated(int taskId, boolean completed);
    }

    // Constructor without listeners (if used, status updates/clicks might not work as expected)
    public TaskAdapter(Context context, List<TaskResponse> taskList) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.listener = null;
        this.statusUpdatedListener = null;
        this.subTaskService = RetrofitClient.getSubTaskService();
        Log.d(TAG, "TaskAdapter initialized (constructor 1). Task count: " + this.taskList.size());
    }

    public TaskAdapter(Context context, List<TaskResponse> taskList, OnItemClickListener listener, OnTaskStatusUpdatedListener statusUpdatedListener) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.listener = listener;
        this.statusUpdatedListener = statusUpdatedListener;
        this.subTaskService = RetrofitClient.getSubTaskService();
        Log.d(TAG, "TaskAdapter initialized (constructor 2). Task count: " + this.taskList.size());
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Ensure context is always set
        View view = LayoutInflater.from(context).inflate(R.layout.item_tasks, parent, false);
        Log.d(TAG, "onCreateViewHolder called");
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = taskList.get(position);
        Log.d(TAG, "onBindViewHolder for position: " + position + ", Task ID: " + task.getId() + ", Title: " + task.getTitle());

        holder.tvNameProject.setText(task.getProject() != null ? task.getProject() : "N/A");
        holder.tvNameTask.setText(task.getTitle());
        holder.tvDescTask.setText(task.getDescription());
        holder.tvDueDate.setText(task.getDueDate() != null && !task.getDueDate().isEmpty() ? task.getDueDate() : "N/A");
        holder.tvDueTime.setText(task.getReminderTime() != null ? task.getReminderTime() : "No Time");
        holder.tvNameLabel.setText(task.getLabel() != null ? task.getLabel() : "N/A");

        // Manage statusButton state and listener carefully
        holder.statusButton.setOnCheckedChangeListener(null); // Remove previous listener
        holder.statusButton.setChecked(Boolean.TRUE.equals(task.getCompleted()));
        holder.statusButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                Log.w(TAG, "statusButton clicked but position is NO_POSITION");
                return;
            }
            TaskResponse taskToUpdate = taskList.get(currentPosition);
            boolean isNowChecked = !Boolean.TRUE.equals(taskToUpdate.getCompleted()); // Correctly toggle

            Log.d(TAG, "statusButton clicked for Task ID: " + taskToUpdate.getId() + ". New status: " + isNowChecked);

            if (statusUpdatedListener != null) {
                statusUpdatedListener.onTaskStatusUpdated(taskToUpdate.getId(), isNowChecked);
                // The ViewModel should be the source of truth.
                // The list update will come via LiveData observation in the Fragment.
                // taskToUpdate.setCompleted(isNowChecked); // Avoid direct model mutation here if LiveData drives updates
                // holder.statusButton.setChecked(isNowChecked); // This too will be handled by re-bind on list update
            } else {
                Log.w(TAG, "statusUpdatedListener is null for Task ID: " + taskToUpdate.getId());
                // Revert UI if no listener to handle the change, as the backend won't be updated.
                holder.statusButton.setChecked(Boolean.TRUE.equals(taskToUpdate.getCompleted()));
                Toast.makeText(context, "Task status listener not set", Toast.LENGTH_SHORT).show();
            }
        });


        final int buttonTintColor;
        String priority = task.getPriority() != null ? task.getPriority().toLowerCase() : "default";
        switch (priority) {
            case "high":
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                buttonTintColor = ContextCompat.getColor(context, R.color.priority_high_dark);
                break;
            case "medium":
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                buttonTintColor = ContextCompat.getColor(context, R.color.priority_medium_dark);
                break;
            case "low":
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                buttonTintColor = ContextCompat.getColor(context, R.color.priority_low_dark);
                break;
            default:
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
                buttonTintColor = ContextCompat.getColor(context, R.color.primary); // Ensure R.color.primary is defined
                break;
        }
        holder.statusButton.setButtonTintList(ColorStateList.valueOf(buttonTintColor));


        Integer taskId = task.getId();
        // Cancel any existing call for this task ID before starting a new one
        if (taskId != null && activeCalls.containsKey(taskId)) {
            Call<List<SubTaskResponse>> existingCall = activeCalls.get(taskId);
            if (existingCall != null && !existingCall.isCanceled()) {
                Log.d(TAG, "Cancelling existing subtask call for taskId: " + taskId);
                existingCall.cancel();
            }
            activeCalls.remove(taskId);
        }
        // Ensure subtask RecyclerView is reset if no subtasks or error
        holder.recyclerViewSubTasks.setAdapter(null);
        holder.recyclerViewSubTasks.setVisibility(View.GONE);
        holder.imageButton.setVisibility(View.GONE); // Default to hidden, show if subtasks might exist

        if (taskId != null && subTaskService != null) {
            Log.d(TAG, "Fetching subtasks for taskId: " + taskId);
            holder.imageButton.setVisibility(View.VISIBLE); // Show expand button if we attempt to load
            holder.imageButton.setImageResource(R.drawable.baseline_expand_more_24); // Default to collapsed

            Call<List<SubTaskResponse>> call = subTaskService.getSubTasksByTaskId(taskId);
            activeCalls.put(taskId, call);

            call.enqueue(new Callback<List<SubTaskResponse>>() {
                @Override
                public void onResponse(Call<List<SubTaskResponse>> subTaskCall, Response<List<SubTaskResponse>> response) {
                    activeCalls.remove(taskId); // Remove call from map once handled
                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) { // View might have been recycled
                        Log.d(TAG, "SubTask onResponse: View recycled for taskId: " + taskId);
                        return;
                    }

                    Log.d(TAG, "SubTask onResponse for taskId: " + taskId + ". Code: " + response.code());
                    if (response.isSuccessful() && response.body() != null) {
                        if (!response.body().isEmpty()) {
                            Log.d(TAG, "SubTask success for taskId: " + taskId + ". Count: " + response.body().size());
                            SubTaskAdapter subTaskAdapter = new SubTaskAdapter(response.body(), (subTask, isChecked) -> {
                                Log.d(TAG, "SubTask ID " + subTask.getId() + " status changed to: " + isChecked);
                                // Handle subtask status change: Ideally, notify ViewModel
                                // For now, direct update or pass to a higher-level listener
                            }, buttonTintColor);
                            holder.recyclerViewSubTasks.setLayoutManager(new LinearLayoutManager(context));
                            holder.recyclerViewSubTasks.setAdapter(subTaskAdapter);
                            // Visibility of subtasks RecyclerView is handled by imageButton click listener
                        } else {
                            Log.d(TAG, "SubTask success for taskId: " + taskId + ". No subtasks found.");
                            holder.recyclerViewSubTasks.setAdapter(null); // Clear adapter
                            holder.recyclerViewSubTasks.setVisibility(View.GONE);
                            // holder.imageButton.setVisibility(View.GONE); // Keep button if API says no subtasks, vs error
                        }
                    } else {
                        Log.w(TAG, "SubTask failed for taskId: " + taskId + ". Code: " + response.code() + " Msg: " + response.message());
                        holder.recyclerViewSubTasks.setAdapter(null);
                        holder.recyclerViewSubTasks.setVisibility(View.GONE);
                        // Optionally hide imageButton on error, or show an error indicator
                    }
                }

                @Override
                public void onFailure(Call<List<SubTaskResponse>> subTaskCall, Throwable t) {
                    activeCalls.remove(taskId);
                    if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) {
                        Log.d(TAG, "SubTask onFailure: View recycled for taskId: " + taskId);
                        return;
                    }
                    if (subTaskCall.isCanceled()) {
                        Log.d(TAG, "SubTask onFailure: Call was canceled for taskId: " + taskId);
                    } else {
                        Log.e(TAG, "SubTask onFailure for taskId: " + taskId + ": " + t.getMessage(), t);
                    }
                    holder.recyclerViewSubTasks.setAdapter(null);
                    holder.recyclerViewSubTasks.setVisibility(View.GONE);
                }
            });
        } else {
            Log.d(TAG, "No taskId or subTaskService for task at position " + position + ". Subtasks will not be loaded.");
            holder.recyclerViewSubTasks.setVisibility(View.GONE);
            holder.imageButton.setVisibility(View.GONE);
        }

        // Subtask expand/collapse listener
        holder.imageButton.setOnClickListener(v -> {
            if (holder.recyclerViewSubTasks.getVisibility() == View.GONE) {
                if (holder.recyclerViewSubTasks.getAdapter() != null && holder.recyclerViewSubTasks.getAdapter().getItemCount() > 0) {
                    holder.recyclerViewSubTasks.setVisibility(View.VISIBLE);
                    holder.imageButton.setImageResource(R.drawable.baseline_expand_less_24);
                    Log.d(TAG, "Expanding subtasks for Task ID: " + task.getId());
                } else {
                    // Optionally, re-trigger subtask load if adapter is null or empty,
                    // or just indicate no subtasks if that's the known state.
                    Log.d(TAG, "Attempted to expand subtasks for Task ID: " + task.getId() + ", but no subtasks loaded/available.");
                    Toast.makeText(context, "No sub-tasks available or loaded.", Toast.LENGTH_SHORT).show();
                }
            } else {
                holder.recyclerViewSubTasks.setVisibility(View.GONE);
                holder.imageButton.setImageResource(R.drawable.baseline_expand_more_24);
                Log.d(TAG, "Collapsing subtasks for Task ID: " + task.getId());
            }
        });


        if (listener != null) {
            holder.itemView.setOnClickListener(v -> {
                Log.d(TAG, "Item clicked: Task ID " + task.getId());
                listener.onItemClick(task);
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull TaskViewHolder holder) {
        super.onViewRecycled(holder);
        // Get task ID from the holder if possible, though it might be tricky if taskList changed
        // For simplicity, we rely on the activeCalls map being managed correctly.
        // More robust: holder.itemView.getTag() could store taskId if set in onBindViewHolder
        Log.d(TAG, "onViewRecycled for holder at position: " + holder.getAdapterPosition());
        // The specific call cancellation for this view's previous task is handled at the start of onBindViewHolder
        // or when setTaskList is called.
    }

    @Override
    public int getItemCount() {
        int count = taskList != null ? taskList.size() : 0;
        // Log.d(TAG, "getItemCount called. Returning: " + count); // This can be very noisy
        return count;
    }

    public void setTaskList(List<TaskResponse> newTaskList) {
        // Cancel all previous subtask calls as the main list is changing
        for (Call<List<SubTaskResponse>> call : activeCalls.values()) {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
        }
        activeCalls.clear();

        this.taskList = newTaskList != null ? new ArrayList<>(newTaskList) : new ArrayList<>();
        Log.d(TAG, "setTaskList called. New task count: " + this.taskList.size());
        notifyDataSetChanged();
    }

    public void cleanup() {
        Log.d(TAG, "cleanup called, cancelling all active subtask calls.");
        for (Call<List<SubTaskResponse>> call : activeCalls.values()) {
            if (call != null && !call.isCanceled()) {
                call.cancel();
            }
        }
        activeCalls.clear();
        context = null; // Release context if it's activity context
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameProject, tvNameTask, tvDescTask, tvDueDate, tvDueTime, tvNameLabel;
        RadioButton statusButton;
        ImageButton imageButton;
        RecyclerView recyclerViewSubTasks;
        LinearLayout linearLayout;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameProject = itemView.findViewById(R.id.nameProject);
            tvNameTask = itemView.findViewById(R.id.nameTask);
            tvDescTask = itemView.findViewById(R.id.descTask);
            tvDueDate = itemView.findViewById(R.id.dueDate);
            tvDueTime = itemView.findViewById(R.id.dueTime);
            tvNameLabel = itemView.findViewById(R.id.nameLabel);
            statusButton = itemView.findViewById(R.id.statusButton);
            imageButton = itemView.findViewById(R.id.imageButton); // For expanding subtasks
            recyclerViewSubTasks = itemView.findViewById(R.id.recyclerViewSubTasks);
            linearLayout = itemView.findViewById(R.id.layoutTask);
        }
    }
}