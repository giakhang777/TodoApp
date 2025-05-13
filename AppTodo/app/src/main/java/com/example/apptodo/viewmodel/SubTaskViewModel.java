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

    private Integer currentTaskId = null;

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

    public void loadSubtasksForTask(int taskId) {
        currentTaskId = taskId;
        subTaskService.getSubTasksByTaskId(taskId).enqueue(new Callback<List<SubTaskResponse>>() {
            @Override
            public void onResponse(Call<List<SubTaskResponse>> call, Response<List<SubTaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtasksLiveData.setValue(response.body());
                } else {
                    subtasksLiveData.setValue(new ArrayList<>());
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
                    if (currentTaskId != null) {
                        loadSubtasksForTask(currentTaskId);
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

    public void updateSubTask(int subTaskId, SubTaskRequest request) {
        subTaskService.updateSubTask(subTaskId, request).enqueue(new Callback<SubTaskResponse>() {
            @Override
            public void onResponse(Call<SubTaskResponse> call, Response<SubTaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtaskOperationResult.setValue(response.body());
                    if (currentTaskId != null) {
                        loadSubtasksForTask(currentTaskId);
                    }
                } else {
                    errorMessage.setValue("Failed to update subtask: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubTaskResponse> call, Throwable t) {
                errorMessage.setValue("Error updating subtask: " + t.getMessage());
            }
        });
    }

    public void deleteSubTask(int subTaskId) {
        subTaskService.deleteSubTask(subTaskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    subtaskOperationResult.setValue(null);
                    if (currentTaskId != null) {
                        loadSubtasksForTask(currentTaskId);
                    }
                } else {
                    errorMessage.setValue("Failed to delete subtask: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue("Error deleting subtask: " + t.getMessage());
            }
        });
    }

    public void changeSubTaskStatus(int subTaskId, boolean completed) {
        subTaskService.changeSubTaskStatus(subTaskId, completed).enqueue(new Callback<SubTaskResponse>() {
            @Override
            public void onResponse(Call<SubTaskResponse> call, Response<SubTaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    subtaskOperationResult.setValue(response.body());
                    if (currentTaskId != null) {
                        loadSubtasksForTask(currentTaskId);
                    }
                } else {
                    errorMessage.setValue("Failed to change subtask status: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<SubTaskResponse> call, Throwable t) {
                errorMessage.setValue("Error changing subtask status: " + t.getMessage());
            }
        });
    }
}
