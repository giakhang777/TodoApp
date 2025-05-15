package com.example.apptodo.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.apptodo.R;
import com.example.apptodo.model.request.SubTaskRequest;
import com.example.apptodo.model.response.SubTaskResponse;
import com.example.apptodo.viewmodel.SubTaskViewModel;
import java.util.ArrayList;
import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {
    private List<SubTaskResponse> subTaskList;
    private OnSubTaskCheckedChangeListener listener;
    private int parentTaskButtonColor;
    private Context context;
    private SubTaskViewModel subTaskViewModel;
    Integer parentTaskId; // Thay đổi từ private thành package-private (hoặc public) hoặc thêm getter

    public interface OnSubTaskCheckedChangeListener {
        void onSubTaskCheckedChanged(SubTaskResponse subTask, boolean isChecked);
    }

    public SubTaskAdapter(Context context, List<SubTaskResponse> subTaskList,
                          SubTaskViewModel subTaskViewModel, Integer parentTaskId,
                          OnSubTaskCheckedChangeListener listener, int parentTaskButtonColor) {
        this.context = context;
        this.subTaskList = (subTaskList != null) ? new ArrayList<>(subTaskList) : new ArrayList<>();
        this.subTaskViewModel = subTaskViewModel;
        this.parentTaskId = parentTaskId;
        this.listener = listener;
        this.parentTaskButtonColor = parentTaskButtonColor;
    }

    // Getter cho parentTaskId
    public Integer getParentTaskId() {
        return parentTaskId;
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
        if (subTask == null) {
            holder.itemView.setVisibility(View.GONE);
            return;
        }
        holder.itemView.setVisibility(View.VISIBLE);
        holder.bind(subTask);
    }

    @Override
    public int getItemCount() {
        return subTaskList != null ? subTaskList.size() : 0;
    }

    public void setSubTaskList(List<SubTaskResponse> newSubTaskList) {
        if (newSubTaskList == null) {
            this.subTaskList = new ArrayList<>();
        } else {
            this.subTaskList = new ArrayList<>(newSubTaskList);
        }
        notifyDataSetChanged();
    }

    private void showUpdateSubTaskDialog(SubTaskResponse subTaskToUpdate) {
        if (context == null || subTaskViewModel == null || subTaskToUpdate == null || subTaskToUpdate.getId() == null || parentTaskId == null) {
            if (context != null) {
                Toast.makeText(context, "Cannot update subtask: Missing information.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_add_subtask, null);
        builder.setView(dialogView);

        final EditText subTaskNameEditText = dialogView.findViewById(R.id.et_subtask_name);
        subTaskNameEditText.setText(subTaskToUpdate.getTitle());
        subTaskNameEditText.setSelection(subTaskNameEditText.getText().length());

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newSubTaskTitle = subTaskNameEditText.getText().toString().trim();
            if (!newSubTaskTitle.isEmpty()) {
                if (newSubTaskTitle.equals(subTaskToUpdate.getTitle())) {
                    Toast.makeText(context, "No changes made to subtask title.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    return;
                }
                SubTaskRequest subTaskRequest = new SubTaskRequest(newSubTaskTitle, parentTaskId);
                subTaskViewModel.currentParentTaskId = parentTaskId;
                subTaskViewModel.updateSubTask(subTaskToUpdate.getId(), subTaskRequest);
            } else {
                Toast.makeText(context, "Subtask title cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Delete", (dialog, which) -> {
            subTaskViewModel.deleteSubTask(subTaskToUpdate.getId(), parentTaskId);
            Toast.makeText(context, "Delete sub task successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public class SubTaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvSubTaskTitle;
        RadioButton cbSubTaskCompleted;

        public SubTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSubTaskTitle = itemView.findViewById(R.id.tv_subtask_title);
            cbSubTaskCompleted = itemView.findViewById(R.id.cb_subtask_completed);
        }

        public void bind(SubTaskResponse subTask) {
            tvSubTaskTitle.setText(subTask.getTitle());
            cbSubTaskCompleted.setOnCheckedChangeListener(null);
            cbSubTaskCompleted.setChecked(Boolean.TRUE.equals(subTask.getCompleted()));
            cbSubTaskCompleted.setButtonTintList(ColorStateList.valueOf(parentTaskButtonColor));

            cbSubTaskCompleted.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) return;
                SubTaskResponse currentSubTask = subTaskList.get(currentPosition);
                boolean isNowChecked = !Boolean.TRUE.equals(currentSubTask.getCompleted());
                if (listener != null && currentSubTask.getId() != null) {
                    listener.onSubTaskCheckedChanged(currentSubTask, isNowChecked);
                }
            });

            tvSubTaskTitle.setOnClickListener(v -> {
                int currentPosition = getAdapterPosition();
                if (currentPosition != RecyclerView.NO_POSITION) {
                    SubTaskResponse subTaskToUpdate = subTaskList.get(currentPosition);
                    showUpdateSubTaskDialog(subTaskToUpdate);
                }
            });
        }
    }
}
