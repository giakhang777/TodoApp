package com.example.apptodo.adapter;

import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptodo.R;
import com.example.apptodo.model.response.SubTaskResponse;
import java.util.ArrayList;
import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {
    private List<SubTaskResponse> subTaskList;
    private OnSubTaskCheckedChangeListener listener;
    private int parentTaskButtonColor;

    public interface OnSubTaskCheckedChangeListener {
        void onSubTaskCheckedChanged(SubTaskResponse subTask, boolean isChecked);
    }

    public SubTaskAdapter(List<SubTaskResponse> subTaskList, OnSubTaskCheckedChangeListener listener, int parentTaskButtonColor) {
        this.subTaskList = (subTaskList != null) ? new ArrayList<>(subTaskList) : new ArrayList<>();
        this.listener = listener;
        this.parentTaskButtonColor = parentTaskButtonColor;
    }

    public void setParentTaskButtonColor(int color) {
        this.parentTaskButtonColor = color;
        notifyDataSetChanged();
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
        if (subTask == null) return;

        holder.tvSubTaskTitle.setText(subTask.getTitle());
        holder.cbSubTaskCompleted.setOnCheckedChangeListener(null);
        holder.cbSubTaskCompleted.setChecked(Boolean.TRUE.equals(subTask.getCompleted()));
        holder.cbSubTaskCompleted.setButtonTintList(ColorStateList.valueOf(parentTaskButtonColor));

        holder.cbSubTaskCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (holder.getAdapterPosition() == RecyclerView.NO_POSITION) return;
            SubTaskResponse currentSubTask = subTaskList.get(holder.getAdapterPosition());
            if (listener != null && currentSubTask.getId() != null) {
                listener.onSubTaskCheckedChanged(currentSubTask, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subTaskList != null ? subTaskList.size() : 0;
    }

    public void setSubTaskList(List<SubTaskResponse> newSubTaskList) {
        if (newSubTaskList == null) {
            newSubTaskList = new ArrayList<>();
        }
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SubTaskDiffCallback(this.subTaskList, newSubTaskList));
        this.subTaskList = new ArrayList<>(newSubTaskList);
        diffResult.dispatchUpdatesTo(this);
    }

    private static class SubTaskDiffCallback extends DiffUtil.Callback {
        private final List<SubTaskResponse> oldList;
        private final List<SubTaskResponse> newList;

        SubTaskDiffCallback(List<SubTaskResponse> oldList, List<SubTaskResponse> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId().equals(newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            SubTaskResponse oldSubTask = oldList.get(oldItemPosition);
            SubTaskResponse newSubTask = newList.get(newItemPosition);
            return oldSubTask.getTitle().equals(newSubTask.getTitle()) &&
                    oldSubTask.getCompleted().equals(newSubTask.getCompleted());
        }
    }

    public static class SubTaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubTaskTitle;
        CheckBox cbSubTaskCompleted;

        public SubTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubTaskTitle = itemView.findViewById(R.id.tv_subtask_title);
            cbSubTaskCompleted = itemView.findViewById(R.id.cb_subtask_completed);
        }
    }
}