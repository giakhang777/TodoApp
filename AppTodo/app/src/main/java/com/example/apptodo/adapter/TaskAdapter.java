package com.example.apptodo.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

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

    public TaskAdapter(Context context, List<TaskResponse> taskList) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.listener = null;
        this.statusUpdatedListener = null;
        this.subTaskService = RetrofitClient.getSubTaskService();
    }

    public TaskAdapter(Context context, List<TaskResponse> taskList, OnItemClickListener listener, OnTaskStatusUpdatedListener statusUpdatedListener) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.listener = listener;
        this.statusUpdatedListener = statusUpdatedListener;
        this.subTaskService = RetrofitClient.getSubTaskService();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_tasks, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskResponse task = taskList.get(position);
        holder.tvNameProject.setText(task.getProject() != null ? task.getProject() : "");
        holder.tvNameTask.setText(task.getTitle());
        holder.tvDescTask.setText(task.getDescription());
        holder.tvDueDate.setText(task.getDueDate() != null && !task.getDueDate().isEmpty() ? task.getDueDate() : "");
        holder.tvDueTime.setText(task.getReminderTime() != null ? task.getReminderTime().toString() : "No Time");
        holder.tvNameLabel.setText(task.getLabel() != null ? task.getLabel() : "");
        holder.statusButton.setChecked(Boolean.TRUE.equals(task.getCompleted()));

        holder.statusButton.setOnClickListener(null);
        holder.statusButton.setChecked(Boolean.TRUE.equals(task.getCompleted()));

        holder.statusButton.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) {
                return;
            }
            TaskResponse taskToUpdate = taskList.get(currentPosition);
            boolean isNowChecked = !(Boolean.TRUE.equals(taskToUpdate.getCompleted()));

            if (statusUpdatedListener != null) {
                statusUpdatedListener.onTaskStatusUpdated(taskToUpdate.getId(), isNowChecked);

                taskToUpdate.setCompleted(isNowChecked);
                holder.statusButton.setChecked(isNowChecked);
            } else {
                holder.statusButton.setChecked(Boolean.TRUE.equals(taskToUpdate.getCompleted()));
                Toast.makeText(context, "Task status listener not set", Toast.LENGTH_SHORT).show();
            }
        });

        final int buttonTintColor;

        switch (task.getPriority().toLowerCase()) {
            case "high":
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                holder.btnAddSubTask.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                buttonTintColor = ContextCompat.getColor(context, R.color.priority_high_dark);
                holder.statusButton.setButtonTintList(ColorStateList.valueOf(buttonTintColor));
                break;
            case "medium":
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                holder.btnAddSubTask.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                buttonTintColor = ContextCompat.getColor(context, R.color.priority_medium_dark);
                holder.statusButton.setButtonTintList(ColorStateList.valueOf(buttonTintColor));
                break;
            default:
                holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                holder.btnAddSubTask.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                buttonTintColor = ContextCompat.getColor(context, R.color.priority_low_dark);
                holder.statusButton.setButtonTintList(ColorStateList.valueOf(buttonTintColor));
                break;
        }

        Integer taskId = task.getId();
        if (taskId != null && activeCalls.containsKey(taskId)) {
            Call<List<SubTaskResponse>> existingCall = activeCalls.get(taskId);
            if (existingCall != null) {
                existingCall.cancel();
            }
            activeCalls.remove(taskId);
        }

        if (taskId != null && subTaskService != null) {
            Call<List<SubTaskResponse>> call = subTaskService.getSubTasksByTaskId(taskId);
            activeCalls.put(taskId, call);

            call.enqueue(new Callback<List<SubTaskResponse>>() {
                @Override
                public void onResponse(Call<List<SubTaskResponse>> call, Response<List<SubTaskResponse>> response) {
                    if (taskId != null) {
                        activeCalls.remove(taskId);
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && holder.recyclerViewSubTasks != null) {
                            SubTaskAdapter subTaskAdapter = new SubTaskAdapter(response.body(), (subTask, isChecked) -> {
                                // Handle subtask status change here or pass up to Fragment
                            }, buttonTintColor);
                            holder.recyclerViewSubTasks.setLayoutManager(new LinearLayoutManager(context));
                            holder.recyclerViewSubTasks.setAdapter(subTaskAdapter);
                            holder.recyclerViewSubTasks.setNestedScrollingEnabled(false);
                        }
                    } else {
                        if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && holder.recyclerViewSubTasks != null) {
                            holder.recyclerViewSubTasks.setAdapter(new SubTaskAdapter(new ArrayList<>(), null, buttonTintColor));
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<SubTaskResponse>> call, Throwable t) {
                    if (taskId != null) {
                        activeCalls.remove(taskId);
                    }
                    if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && holder.recyclerViewSubTasks != null) {
                        holder.recyclerViewSubTasks.setAdapter(new SubTaskAdapter(new ArrayList<>(), null, buttonTintColor));
                    }
                }
            });
        } else {
            if (holder.recyclerViewSubTasks != null) {
                holder.recyclerViewSubTasks.setAdapter(new SubTaskAdapter(new ArrayList<>(), null, buttonTintColor));
                holder.recyclerViewSubTasks.setVisibility(View.GONE);
                holder.imageButton.setVisibility(View.GONE);
            }
        }

        holder.imageButton.setOnClickListener(v -> {
            if (taskId != null && holder.recyclerViewSubTasks != null) {
                if (holder.recyclerViewSubTasks.getVisibility() == View.GONE) {
                    holder.recyclerViewSubTasks.setVisibility(View.VISIBLE);
                    holder.imageButton.setImageResource(R.drawable.baseline_expand_less_24);
                } else {
                    holder.recyclerViewSubTasks.setVisibility(View.GONE);
                    holder.imageButton.setImageResource(R.drawable.baseline_expand_more_24);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onItemClick(taskList.get(clickedPosition));
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull TaskViewHolder holder) {
        super.onViewRecycled(holder);
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
            TaskResponse task = taskList.get(position);
            Integer taskId = task.getId();
            if (taskId != null && activeCalls.containsKey(taskId)) {
                Call<List<SubTaskResponse>> existingCall = activeCalls.get(taskId);
                if (existingCall != null) {
                    existingCall.cancel();
                }
                activeCalls.remove(taskId);
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public void setTaskList(List<TaskResponse> taskList) {
        activeCalls.clear();
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void cleanup() {
        for (Call<List<SubTaskResponse>> call : activeCalls.values()) {
            if (call != null) {
                call.cancel();
            }
        }
        activeCalls.clear();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameProject, tvNameTask, tvDescTask, tvDueDate, tvDueTime, tvNameLabel;
        RadioButton statusButton;
        ImageButton imageButton, btnAddSubTask;
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
            imageButton = itemView.findViewById(R.id.imageButton);
            recyclerViewSubTasks = itemView.findViewById(R.id.recyclerViewSubTasks);
            linearLayout = itemView.findViewById(R.id.layoutTask);
            btnAddSubTask = itemView.findViewById(R.id.btnAddSubTask);
        }
    }
}