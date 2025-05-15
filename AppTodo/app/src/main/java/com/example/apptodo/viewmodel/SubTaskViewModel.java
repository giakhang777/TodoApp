package com.example.apptodo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.apptodo.api.SubTaskService;
import com.example.apptodo.model.request.SubTaskRequest;
import com.example.apptodo.model.response.SubTaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubTaskViewModel extends ViewModel {

    private final SubTaskService subTaskService;
    private final MutableLiveData<List<SubTaskResponse>> subtasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<SubTaskResponse> subtaskOperationResult = new MutableLiveData<>();
    public Integer currentParentTaskId = null;

    public SubTaskViewModel() {
        subTaskService = RetrofitClient.getSubTaskService();
    }

    public LiveData<List<SubTaskResponse>> getSubtasks() {
        return subtasksLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<SubTaskResponse> getSubtaskOperationResult() {
        return subtaskOperationResult;
    }

    public Integer getCurrentParentTaskId() {
        return currentParentTaskId;
    }

    public void loadSubtasksForTask(int parentTaskId) {
        this.currentParentTaskId = parentTaskId;
        subTaskService.getSubTasksByTaskId(parentTaskId).enqueue(new Callback<List<SubTaskResponse>>() {
            @Override
            public void onResponse(Call<List<SubTaskResponse>> call, Response<List<SubTaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtasksLiveData.setValue(response.body());
                } else {
                    subtasksLiveData.setValue(new ArrayList<>());
                    errorMessage.setValue("Failed to load subtasks: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<SubTaskResponse>> call, Throwable t) {
                errorMessage.setValue("Error loading subtasks: " + t.getMessage());
                subtasksLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void createSubTask(SubTaskRequest request) {
        Integer parentId = request.getTask();
        subTaskService.createSubTask(request).enqueue(new Callback<SubTaskResponse>() {
            @Override
            public void onResponse(Call<SubTaskResponse> call, Response<SubTaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtaskOperationResult.setValue(response.body());
                    if (parentId != null) {
                        currentParentTaskId = parentId;
                    }
                } else {
                    errorMessage.setValue("Failed to create subtask: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubTaskResponse> call, Throwable t) {
                errorMessage.setValue("Error creating subtask: " + t.getMessage());
            }
        });
    }

    public void changeSubTaskStatus(int subTaskId, boolean completed) {
        subTaskService.changeSubTaskStatus(subTaskId, completed).enqueue(new Callback<SubTaskResponse>() {
            @Override
            public void onResponse(Call<SubTaskResponse> call, Response<SubTaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtaskOperationResult.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to change subtask status: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubTaskResponse> call, Throwable t) {
                errorMessage.setValue("Error changing subtask status: " + t.getMessage());
            }
        });
    }

    public void updateSubTask(Integer subTaskId, SubTaskRequest request) {
        Integer parentId = request.getTask(); // Giữ lại parentId để set cho currentParentTaskId
        subTaskService.updateSubTask(subTaskId, request).enqueue(new Callback<SubTaskResponse>() {
            @Override
            public void onResponse(Call<SubTaskResponse> call, Response<SubTaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtaskOperationResult.setValue(response.body());
                    if (parentId != null) { // Cập nhật currentParentTaskId nếu có
                        currentParentTaskId = parentId;
                    }
                } else {
                    errorMessage.setValue("Failed to update subtask: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubTaskResponse> call, Throwable t) {
                errorMessage.setValue("Error updating subtask: " + t.getMessage());
            }
        });
    }
    public void deleteSubTask(Integer subTaskId, Integer parentTaskIdOfDeletedSubtask) {
        if (subTaskId == null) {
            errorMessage.setValue("Subtask ID is null, cannot delete.");
            return;
        }
        // Thiết lập currentParentTaskId để các observers hoặc logic khác biết context
        this.currentParentTaskId = parentTaskIdOfDeletedSubtask;

        subTaskService.deleteSubTask(subTaskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Báo hiệu hoạt động thành công (ví dụ: để TaskAdapter làm mới Task cha)
                    subtaskOperationResult.setValue(null);
                    // Trực tiếp làm mới danh sách subtask cho Task cha hiện tại
                    if (parentTaskIdOfDeletedSubtask != null) {
                        loadSubtasksForTask(parentTaskIdOfDeletedSubtask);
                    }
                } else {
                    errorMessage.setValue("Failed to delete subtask: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue("Error deleting subtask: " + t.getMessage());
            }
        });
    }
}