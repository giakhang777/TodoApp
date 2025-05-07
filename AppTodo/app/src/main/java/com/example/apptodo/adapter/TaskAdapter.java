package com.example.apptodo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.model.response.TaskResponse;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private List<TaskResponse> taskList;
    private Context context;

    // Constructor
    public TaskAdapter(Context context, List<TaskResponse> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tasks, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        TaskResponse task = taskList.get(position);

        // Set project and title
        holder.projectTextView.setText(task.getProject());
        holder.titleTextView.setText(task.getTitle());

        // Set due date and time (split if necessary)
        holder.dueDateTextView.setText(task.getDueDate()); // You can split if you need to show day/time separately
        holder.dueTimeTextView.setText("12:00 PM"); // This can be customized based on your data

        // Set label
        holder.labelTextView.setText(task.getLabel());

        // Handle task status (completed/incomplete)
//        if (task.getCompleted()) {
//            holder.statusButton.setChecked(true);
//            holder.statusButton.setText("Completed");
//        } else {
//            holder.statusButton.setChecked(false);
//            holder.statusButton.setText("Incomplete");
//        }

        // Set expand button listener (if any additional action required)
        holder.expandButton.setOnClickListener(v -> {
            // Implement your action for expanding/collapsing task details
            // e.g., showing a dialog or expanding a detailed section
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView projectTextView, titleTextView, dueDateTextView, dueTimeTextView, labelTextView;
        RadioButton statusButton;
        ImageButton expandButton;

        public TaskViewHolder(View itemView) {
            super(itemView);

            // Bind views to their corresponding elements in item_tasks layout
            projectTextView = itemView.findViewById(R.id.nameProject);
            titleTextView = itemView.findViewById(R.id.nameTask);
            dueDateTextView = itemView.findViewById(R.id.dueDate);
            dueTimeTextView = itemView.findViewById(R.id.dueTime);
            labelTextView = itemView.findViewById(R.id.nameLabel);
            statusButton = itemView.findViewById(R.id.statusButton); // RadioButton for task completion status
            expandButton = itemView.findViewById(R.id.imageButton); // ImageButton to expand task details
        }
    }
}
