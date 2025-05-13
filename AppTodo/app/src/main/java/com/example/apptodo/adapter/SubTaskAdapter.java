package com.example.apptodo.adapter;

import android.content.res.ColorStateList;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.model.response.SubTaskResponse;

import java.util.List;
import java.util.ArrayList; // Import ArrayList

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {

    private static final String TAG = "SubTaskAdapter"; // Logging Tag

    private List<SubTaskResponse> subTaskList;
    private OnSubTaskCheckedChangeListener listener;
    private int parentTaskButtonColor;

    public interface OnSubTaskCheckedChangeListener {
        void onSubTaskCheckedChanged(SubTaskResponse subTask, boolean isChecked);
    }

    public SubTaskAdapter(List<SubTaskResponse> subTaskList, OnSubTaskCheckedChangeListener listener, int parentTaskButtonColor) {
        this.subTaskList = (subTaskList != null) ? subTaskList : new ArrayList<>(); // Handle null input
        this.listener = listener;
        this.parentTaskButtonColor = parentTaskButtonColor;
        Log.d(TAG, "SubTaskAdapter initialized. SubTask count: " + this.subTaskList.size());
    }

    @NonNull
    @Override
    public SubTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask, parent, false);
        Log.d(TAG, "onCreateViewHolder called");
        return new SubTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTaskViewHolder holder, int position) {
        SubTaskResponse subTask = subTaskList.get(position);
        Log.d(TAG, "onBindViewHolder for subtask position: " + position + ", Title: " + subTask.getTitle());

        holder.tvSubTaskTitle.setText(subTask.getTitle());

        holder.rbSubTaskCompleted.setOnCheckedChangeListener(null); // Remove previous listener
        holder.rbSubTaskCompleted.setChecked(subTask.getCompleted() != null && subTask.getCompleted());
        holder.rbSubTaskCompleted.setButtonTintList(ColorStateList.valueOf(parentTaskButtonColor));

        holder.rbSubTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return; // Check for valid position
            SubTaskResponse currentSubTask = subTaskList.get(holder.getAdapterPosition()); // Get current subtask
            Log.d(TAG, "SubTask ID " + currentSubTask.getId() + " onCheckedChanged, isChecked: " + isChecked);
            if (listener != null) {
                listener.onSubTaskCheckedChanged(currentSubTask, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = subTaskList != null ? subTaskList.size() : 0;
        // Log.d(TAG, "getItemCount called. Returning: " + count); // Can be noisy
        return count;
    }

    public void setSubTaskList(List<SubTaskResponse> newSubTaskList) {
        this.subTaskList = (newSubTaskList != null) ? new ArrayList<>(newSubTaskList) : new ArrayList<>();
        Log.d(TAG, "setSubTaskList called. New subtask count: " + this.subTaskList.size());
        notifyDataSetChanged();
    }

    public static class SubTaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubTaskTitle;
        RadioButton rbSubTaskCompleted;

        public SubTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubTaskTitle = itemView.findViewById(R.id.tv_subtask_title);
            rbSubTaskCompleted = itemView.findViewById(R.id.rb_subtask_completed);
        }
    }
}