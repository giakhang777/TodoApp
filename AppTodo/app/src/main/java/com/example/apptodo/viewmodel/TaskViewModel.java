package com.example.apptodo.viewmodel;

import android.util.Log;
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
                    errorMessage.setValue("Failed to load tasks: " + response.code() + " - " + response.message());
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
                    errorMessage.setValue("Failed to load tasks for date: " + response.code() + " - " + response.message());
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
                    errorMessage.setValue("Failed to load tasks for project: " + response.code() + " - " + response.message());
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
                    getTaskById(taskId); // Làm mới task sau khi cập nhật trạng thái
                } else {
                    errorMessage.setValue("Failed to change task status: " + response.code() + " - " + response.message());
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
                    errorMessage.setValue("Failed to create task: " + response.code() + " - " + response.message());
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
                    errorMessage.setValue("Failed to delete task: " + response.code() + " - " + response.message());
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
                    getTaskById(taskId); // Làm mới task sau khi cập nhật
                } else {
                    errorMessage.setValue("Failed to update task: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                errorMessage.setValue("Error updating task: " + t.getMessage());
            }
        });
    }

    public void getTaskById(int taskId) {
        Log.d("TaskViewModel", "Getting task by ID: " + taskId);
        if (taskId <= 0) {
            errorMessage.setValue("Invalid task ID: " + taskId);
            return;
        }
        taskService.getTaskById(taskId).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                Log.d("TaskViewModel", "Response for task ID " + taskId + ": " + response.code() + " - " + response.message());
                if (response.isSuccessful() && response.body() != null) {
                    TaskResponse updatedTask = response.body();
                    singleTaskLiveData.setValue(updatedTask);

                    List<TaskResponse> currentTasks = tasksLiveData.getValue();
                    if (currentTasks != null) {
                        List<TaskResponse> newTaskList = new ArrayList<>(currentTasks);
                        boolean found = false;
                        for (int i = 0; i < newTaskList.size(); i++) {
                            if (newTaskList.get(i).getId() != null && newTaskList.get(i).getId().equals(taskId)) {
                                newTaskList.set(i, updatedTask);
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            tasksLiveData.setValue(newTaskList);
                        }
                    }
                } else {
                    singleTaskLiveData.setValue(null);
                    String errorMsg = "Failed to get task details: " + response.code() + " - " + response.message();
                    errorMessage.setValue(errorMsg);
                    Log.e("TaskViewModel", "Failed to get task details for ID " + taskId + ": " + errorMsg);
                    // Làm mới danh sách task nếu không tìm thấy
                    loadAllTasks(1); // Thay 1 bằng userId thực tế
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                singleTaskLiveData.setValue(null);
                errorMessage.setValue("Error getting task details: " + t.getMessage());
                Log.e("TaskViewModel", "API failure for task ID " + taskId + ": " + t.getMessage());
            }
        });
    }

    public void clearTasksLiveData() {
        tasksLiveData.setValue(new ArrayList<>());
    }
}