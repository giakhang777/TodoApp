package com.example.apptodo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.request.TaskRequest;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskViewModel extends ViewModel {

    private final TaskService taskService;

    private final MutableLiveData<List<TaskResponse>> tasksLiveData = new MutableLiveData<>();

    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<TaskResponse> taskOperationResult = new MutableLiveData<>();

    private final MutableLiveData<TaskResponse> singleTaskLiveData = new MutableLiveData<>();


    public TaskViewModel() {
        taskService = RetrofitClient.getTaskService();
    }

    public LiveData<List<TaskResponse>> getTasks() {
        return tasksLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<TaskResponse> getTaskOperationResult() {
        return taskOperationResult;
    }

    public LiveData<TaskResponse> getSingleTask() {
        return singleTaskLiveData;
    }


    public void loadAllTasks(int userId) {
        taskService.getTasksByUser(userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tasksLiveData.setValue(response.body());
                } else {
                    tasksLiveData.setValue(new ArrayList<>());
                    errorMessage.setValue("Failed to load tasks: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                errorMessage.setValue("Error loading tasks: " + t.getMessage());
                tasksLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void loadTasksByDate(String date, int userId) {
        taskService.getTasksByDate(date, userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tasksLiveData.setValue(response.body());
                } else {
                    tasksLiveData.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                errorMessage.setValue("Error loading tasks for date: " + t.getMessage());
                tasksLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void loadTasksByProject(int projectId) {
        taskService.getTasksByProject(projectId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tasksLiveData.setValue(response.body());
                } else {
                    tasksLiveData.setValue(new ArrayList<>());
                    errorMessage.setValue("Failed to load tasks for project: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                errorMessage.setValue("Error loading tasks for project: " + t.getMessage());
                tasksLiveData.setValue(new ArrayList<>());
            }
        });
    }


    public void changeTaskStatus(int taskId, boolean completed) {
        taskService.changeTaskStatus(taskId, completed).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskOperationResult.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to change task status: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                errorMessage.setValue("Error changing task status: " + t.getMessage());
            }
        });
    }

    public void createTask(TaskRequest taskRequest) {
        taskService.createTask(taskRequest).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskOperationResult.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to create task: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                errorMessage.setValue("Error creating task: " + t.getMessage());
            }
        });
    }

    public void deleteTask(int taskId) {
        taskService.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    taskOperationResult.setValue(null);
                } else {
                    errorMessage.setValue("Failed to delete task: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue("Error deleting task: " + t.getMessage());
            }
        });
    }

    public void updateTask(int taskId, TaskRequest taskRequest) {
        taskService.updateTask(taskId, taskRequest).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskOperationResult.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to update task: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                errorMessage.setValue("Error updating task: " + t.getMessage());
            }
        });
    }

    public void getTaskById(int taskId) {
        taskService.getTaskById(taskId).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    singleTaskLiveData.setValue(response.body());
                } else {
                    singleTaskLiveData.setValue(null);
                    errorMessage.setValue("Failed to get task details: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                singleTaskLiveData.setValue(null);
                errorMessage.setValue("Error getting task details: " + t.getMessage());
            }
        });
    }
}
