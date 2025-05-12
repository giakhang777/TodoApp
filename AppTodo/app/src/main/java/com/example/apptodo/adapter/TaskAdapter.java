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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.api.SubTaskService;
import com.example.apptodo.api.TaskService;
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
    private TaskService taskService;

    public interface OnItemClickListener {
        void onItemClick(TaskResponse task);
    }

    public interface OnTaskStatusUpdatedListener {
        void onTaskStatusUpdated();
    }

    public TaskAdapter(Context context, List<TaskResponse> taskList) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.listener = null;
        this.statusUpdatedListener = null;
        this.taskService = RetrofitClient.getTaskService();
    }

    public TaskAdapter(Context context, List<TaskResponse> taskList, OnItemClickListener listener, OnTaskStatusUpdatedListener statusUpdatedListener) {
        this.context = context;
        this.taskList = taskList != null ? taskList : new ArrayList<>();
        this.listener = listener;
        this.statusUpdatedListener = statusUpdatedListener;
        this.taskService = RetrofitClient.getTaskService();
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
        holder.tvDueDate.setText(task.getDueDate() != null ? task.getDueDate().toString() : "");
        holder.tvDueTime.setText(task.getReminderTime() != null ? task.getReminderTime().toString() : "No Time");
        holder.tvNameLabel.setText(task.getLabel() != null ? task.getLabel() : "");
        holder.statusButton.setChecked(task.getCompleted() != null && task.getCompleted());

        // Xử lý sự kiện click RadioButton để cập nhật trạng thái completed
        holder.statusButton.setOnClickListener(null); // tránh set nhiều lần
        holder.statusButton.setChecked(task.getCompleted() != null && task.getCompleted());

        holder.statusButton.setOnClickListener(v -> {
            boolean isNowChecked = !task.getCompleted(); // toggle trạng thái

            taskService.changeTaskStatus(task.getId(), isNowChecked).enqueue(new Callback<TaskResponse>() {
                @Override
                public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        task.setCompleted(response.body().getCompleted());
                        holder.statusButton.setChecked(response.body().getCompleted());

                        String statusMessage = response.body().getCompleted() ? "completed" : "not completed";
                        Toast.makeText(context, "Task status updated to " + statusMessage, Toast.LENGTH_SHORT).show();

                        if (statusUpdatedListener != null) {
                            statusUpdatedListener.onTaskStatusUpdated();
                        }
                    } else {
                        holder.statusButton.setChecked(task.getCompleted() != null && task.getCompleted());
                        Toast.makeText(context, "Failed to update task status: " + response.message(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TaskResponse> call, Throwable t) {
                    holder.statusButton.setChecked(task.getCompleted() != null && task.getCompleted());
                    Toast.makeText(context, "Error updating task status: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        });


        // Hủy yêu cầu cũ nếu có (cho subTask)
        Integer taskId = task.getId();
        if (activeCalls.containsKey(taskId)) {
            activeCalls.get(taskId).cancel();
            activeCalls.remove(taskId);
        }

        // Lấy danh sách subTask từ API
        SubTaskService subTaskService = RetrofitClient.getSubTaskService();
        Call<List<SubTaskResponse>> call = subTaskService.getSubTasksByTaskId(taskId);
        activeCalls.put(taskId, call);

        call.enqueue(new Callback<List<SubTaskResponse>>() {
            @Override
            public void onResponse(Call<List<SubTaskResponse>> call, Response<List<SubTaskResponse>> response) {
                activeCalls.remove(taskId);
                if (response.isSuccessful() && response.body() != null) {
                    if (holder.getAdapterPosition() != RecyclerView.NO_POSITION && holder.recyclerViewSubTasks != null) {
                        SubTaskAdapter subTaskAdapter = new SubTaskAdapter(response.body(), (subTask, isChecked) -> {
                            // Xử lý cập nhật trạng thái completed của subTask (có thể thêm sau)
                        });
                        holder.recyclerViewSubTasks.setLayoutManager(new LinearLayoutManager(context));
                        holder.recyclerViewSubTasks.setAdapter(subTaskAdapter);
                        holder.recyclerViewSubTasks.setNestedScrollingEnabled(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SubTaskResponse>> call, Throwable t) {
                activeCalls.remove(taskId);
            }
        });

        // Xử lý click ImageButton để mở/đóng subTask
        holder.imageButton.setOnClickListener(v -> {
            if (holder.recyclerViewSubTasks != null) {
                if (holder.recyclerViewSubTasks.getVisibility() == View.GONE) {
                    holder.recyclerViewSubTasks.setVisibility(View.VISIBLE);
                    holder.imageButton.setImageResource(R.drawable.baseline_expand_less_24);
                } else {
                    holder.recyclerViewSubTasks.setVisibility(View.GONE);
                    holder.imageButton.setImageResource(R.drawable.baseline_expand_more_24);
                }
            }
        });

        if (listener != null) {
            holder.itemView.setOnClickListener(v -> listener.onItemClick(task));
        }

        // Hiển thị độ ưu tiên
        if (task.getPriority() != null) {
            switch (task.getPriority().toLowerCase()) {
                case "high":
                    holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                    holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_high));
                    holder.statusButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.priority_high_dark)));
                    break;
                case "medium":
                    holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                    holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_medium));
                    holder.statusButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.priority_medium_dark)));
                    break;
                case "low":
                    holder.linearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                    holder.imageButton.setBackgroundColor(ContextCompat.getColor(context, R.color.priority_low));
                    holder.statusButton.setButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.priority_low_dark)));
                    break;
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull TaskViewHolder holder) {
        super.onViewRecycled(holder);
        int position = holder.getAdapterPosition();
        if (position != RecyclerView.NO_POSITION && position < taskList.size()) {
            Integer taskId = taskList.get(position).getId();
            if (activeCalls.containsKey(taskId)) {
                activeCalls.get(taskId).cancel();
                activeCalls.remove(taskId);
            }
        }
    }

    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    public void setTaskList(List<TaskResponse> taskList) {
        for (Call<List<SubTaskResponse>> call : activeCalls.values()) {
            call.cancel();
        }
        activeCalls.clear();

        this.taskList = taskList != null ? taskList : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void cleanup() {
        for (Call<List<SubTaskResponse>> call : activeCalls.values()) {
            call.cancel();
        }
        activeCalls.clear();
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
            imageButton = itemView.findViewById(R.id.imageButton);
            recyclerViewSubTasks = itemView.findViewById(R.id.recyclerViewSubTasks);
            linearLayout = itemView.findViewById(R.id.layoutTask);
        }
    }
}