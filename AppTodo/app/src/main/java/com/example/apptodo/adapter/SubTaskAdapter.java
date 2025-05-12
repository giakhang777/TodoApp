package com.example.apptodo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.model.response.SubTaskResponse;

import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {

    private List<SubTaskResponse> subTaskList;
    private OnSubTaskCheckedChangeListener listener;

    public interface OnSubTaskCheckedChangeListener {
        void onSubTaskCheckedChanged(SubTaskResponse subTask, boolean isChecked);
    }

    public SubTaskAdapter(List<SubTaskResponse> subTaskList, OnSubTaskCheckedChangeListener listener) {
        this.subTaskList = subTaskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subtask, parent, false);
        return new SubTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTaskViewHolder holder, int position) {
        SubTaskResponse subTask = subTaskList.get(position);
        holder.tvSubTaskTitle.setText(subTask.getTitle());
        holder.rbSubTaskCompleted.setChecked(subTask.getCompleted() != null ? subTask.getCompleted() : false);

        holder.rbSubTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onSubTaskCheckedChanged(subTask, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subTaskList != null ? subTaskList.size() : 0;
    }

    public void setSubTaskList(List<SubTaskResponse> subTaskList) {
        this.subTaskList = subTaskList;
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