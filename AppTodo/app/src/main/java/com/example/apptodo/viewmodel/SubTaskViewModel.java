package com.example.apptodo.viewmodel;

import android.util.Log;
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
    private Integer currentParentTaskId = null;
    private boolean isUpdating = false;

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
        if (isUpdating) return;
        this.currentParentTaskId = parentTaskId;
        Log.d("SubTaskViewModel", "Loading subtasks for task ID: " + parentTaskId);
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
        subTaskService.createSubTask(request).enqueue(new Callback<SubTaskResponse>() {
            @Override
            public void onResponse(Call<SubTaskResponse> call, Response<SubTaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtaskOperationResult.setValue(response.body());
                    if (currentParentTaskId != null) {
                        loadSubtasksForTask(currentParentTaskId);
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
        if (isUpdating) return;
        isUpdating = true;
        Log.d("SubTaskViewModel", "Changing subTask ID: " + subTaskId + ", completed: " + completed + ", currentParentTaskId: " + currentParentTaskId);
        subTaskService.changeSubTaskStatus(subTaskId, completed).enqueue(new Callback<SubTaskResponse>() {
            @Override
            public void onResponse(Call<SubTaskResponse> call, Response<SubTaskResponse> response) {
                Log.d("SubTaskViewModel", "Response code: " + response.code() + ", message: " + response.message());
                if (response.isSuccessful() && response.body() != null) {
                    subtaskOperationResult.setValue(response.body());
                    if (currentParentTaskId != null) {
                        loadSubtasksForTask(currentParentTaskId);
                    }
                } else {
                    errorMessage.setValue("Failed to change subtask status: " + response.code() + " - " + response.message());
                    Log.e("SubTaskViewModel", "Failed to change subtask status: " + response.code() + " - " + response.message());
                }
                isUpdating = false;
            }

            @Override
            public void onFailure(Call<SubTaskResponse> call, Throwable t) {
                errorMessage.setValue("Error changing subtask status: " + t.getMessage());
                Log.e("SubTaskViewModel", "API failure: " + t.getMessage());
                isUpdating = false;
            }
        });
    }
}