package com.example.apptodo.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.request.TaskRequest;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.io.IOException; // Thêm import này
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskViewModel extends ViewModel {

    private static final String TAG = "TaskViewModel";

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

    // Hàm helper để đọc errorBody
    private String getErrorBodyMessage(Response<?> response) {
        if (response.errorBody() != null) {
            try {
                return response.errorBody().string();
            } catch (IOException e) {
                Log.e(TAG, "Error reading errorBody", e);
                return "Error reading error body: " + e.getMessage();
            }
        }
        return "Unknown error body.";
    }

    public void loadAllTasks(int userId) {
        Log.d(TAG, "loadAllTasks called for userId: " + userId);
        taskService.getTasksByUser(userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                Log.d(TAG, "loadAllTasks - onResponse. Code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadAllTasks - Success. Tasks count: " + response.body().size());
                    tasksLiveData.setValue(response.body());
                } else {
                    tasksLiveData.setValue(new ArrayList<>());
                    String errorMsg = "Failed to load all tasks";
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "loadAllTasks - Error Body: " + errorBody);
                    } else if (response.body() == null) {
                        errorMsg += " (empty response body)";
                    }
                    Log.w(TAG, "loadAllTasks - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                Log.e(TAG, "loadAllTasks - onFailure: " + t.getMessage(), t);
                errorMessage.setValue("Error loading all tasks: " + t.getMessage());
                tasksLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void loadTasksByDate(String date, int userId) {
        Log.d(TAG, "loadTasksByDate called with date: " + date + ", userId: " + userId);
        taskService.getTasksByDate(date, userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                Log.d(TAG, "loadTasksByDate - onResponse. Code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadTasksByDate - Success. Tasks count: " + response.body().size());
                    tasksLiveData.setValue(response.body());
                    if (response.body().isEmpty()) {
                        Log.d(TAG, "loadTasksByDate - No tasks found for date: " + date);
                        // errorMessage.setValue("Không có công việc nào cho ngày " + date); // Tùy chọn: thông báo cho người dùng
                    }
                } else {
                    tasksLiveData.setValue(new ArrayList<>());
                    String errorMsg = "Failed to load tasks for date " + date;
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "loadTasksByDate - Error Body: " + errorBody); // Log error body
                    } else if (response.body() == null) {
                        errorMsg += " (empty response body or no tasks found)";
                    }
                    Log.w(TAG, "loadTasksByDate - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                Log.e(TAG, "loadTasksByDate - onFailure: " + t.getMessage(), t);
                errorMessage.setValue("Error loading tasks for date '" + date + "': " + t.getMessage());
                tasksLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void loadTasksByProject(int projectId) {
        Log.d(TAG, "loadTasksByProject called for projectId: " + projectId);
        taskService.getTasksByProject(projectId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                Log.d(TAG, "loadTasksByProject - onResponse. Code: " + response.code() + ", isSuccessful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadTasksByProject - Success. Tasks count: " + response.body().size());
                    tasksLiveData.setValue(response.body());
                } else {
                    tasksLiveData.setValue(new ArrayList<>());
                    String errorMsg = "Failed to load tasks for project";
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "loadTasksByProject - Error Body: " + errorBody);
                    } else if (response.body() == null) {
                        errorMsg += " (empty response body)";
                    }
                    Log.w(TAG, "loadTasksByProject - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                Log.e(TAG, "loadTasksByProject - onFailure: " + t.getMessage(), t);
                errorMessage.setValue("Error loading tasks for project: " + t.getMessage());
                tasksLiveData.setValue(new ArrayList<>());
            }
        });
    }

    public void changeTaskStatus(int taskId, boolean completed) {
        Log.d(TAG, "changeTaskStatus called for taskId: " + taskId + ", completed: " + completed);
        taskService.changeTaskStatus(taskId, completed).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                Log.d(TAG, "changeTaskStatus - onResponse. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "changeTaskStatus - Success. TaskId: " + response.body().getId());
                    taskOperationResult.setValue(response.body());
                } else {
                    String errorMsg = "Failed to change task status for taskId " + taskId;
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "changeTaskStatus - Error Body: " + errorBody);
                    }
                    Log.w(TAG, "changeTaskStatus - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Log.e(TAG, "changeTaskStatus - onFailure for taskId " + taskId + ": " + t.getMessage(), t);
                errorMessage.setValue("Error changing task status: " + t.getMessage());
            }
        });
    }

    // ... (các hàm createTask, deleteTask, updateTask, getTaskById giữ nguyên logic log lỗi tương tự) ...
    // Đảm bảo bạn cũng thêm phần getErrorBodyMessage() vào các hàm đó nếu chúng trả về lỗi HTTP.
    // Ví dụ cho createTask:
    public void createTask(TaskRequest taskRequest) {
        Log.d(TAG, "createTask called with title: " + taskRequest.getTitle());
        taskService.createTask(taskRequest).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                Log.d(TAG, "createTask - onResponse. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "createTask - Success. TaskId: " + response.body().getId());
                    taskOperationResult.setValue(response.body());
                } else {
                    String errorMsg = "Failed to create task";
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "createTask - Error Body: " + errorBody);
                    }
                    Log.w(TAG, "createTask - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Log.e(TAG, "createTask - onFailure: " + t.getMessage(), t);
                errorMessage.setValue("Error creating task: " + t.getMessage());
            }
        });
    }

    public void deleteTask(int taskId) {
        Log.d(TAG, "deleteTask called for taskId: " + taskId);
        taskService.deleteTask(taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "deleteTask - onResponse. Code: " + response.code());
                if (response.isSuccessful()) {
                    Log.d(TAG, "deleteTask - Success for taskId: " + taskId);
                    taskOperationResult.setValue(null);
                } else {
                    String errorMsg = "Failed to delete task " + taskId;
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "deleteTask - Error Body: " + errorBody);
                    }
                    Log.w(TAG, "deleteTask - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "deleteTask - onFailure for taskId " + taskId + ": " + t.getMessage(), t);
                errorMessage.setValue("Error deleting task: " + t.getMessage());
            }
        });
    }

    public void updateTask(int taskId, TaskRequest taskRequest) {
        Log.d(TAG, "updateTask called for taskId: " + taskId);
        taskService.updateTask(taskId, taskRequest).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                Log.d(TAG, "updateTask - onResponse. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "updateTask - Success. TaskId: " + response.body().getId());
                    taskOperationResult.setValue(response.body());
                } else {
                    String errorMsg = "Failed to update task " + taskId;
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "updateTask - Error Body: " + errorBody);
                    }
                    Log.w(TAG, "updateTask - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Log.e(TAG, "updateTask - onFailure for taskId " + taskId + ": " + t.getMessage(), t);
                errorMessage.setValue("Error updating task: " + t.getMessage());
            }
        });
    }

    public void getTaskById(int taskId) {
        Log.d(TAG, "getTaskById called for taskId: " + taskId);
        taskService.getTaskById(taskId).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                Log.d(TAG, "getTaskById - onResponse. Code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "getTaskById - Success. TaskId: " + response.body().getId());
                    singleTaskLiveData.setValue(response.body());
                } else {
                    singleTaskLiveData.setValue(null);
                    String errorMsg = "Failed to get details for task " + taskId;
                    if (!response.isSuccessful()) {
                        errorMsg += " (Code: " + response.code() + "): " + response.message();
                        String errorBody = getErrorBodyMessage(response);
                        errorMsg += " - Details: " + errorBody;
                        Log.e(TAG, "getTaskById - Error Body: " + errorBody);
                    }
                    Log.w(TAG, "getTaskById - " + errorMsg);
                    errorMessage.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<TaskResponse> call, Throwable t) {
                Log.e(TAG, "getTaskById - onFailure for taskId " + taskId + ": " + t.getMessage(), t);
                singleTaskLiveData.setValue(null);
                errorMessage.setValue("Error getting task details: " + t.getMessage());
            }
        });
    }
}