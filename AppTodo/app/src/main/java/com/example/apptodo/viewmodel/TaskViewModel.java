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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskViewModel extends ViewModel {

    private final TaskService taskService;
    private final MutableLiveData<List<TaskResponse>> tasksLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<TaskResponse> taskOperationResult = new MutableLiveData<>();
    private final MutableLiveData<TaskResponse> singleTaskLiveData = new MutableLiveData<>();

    // Biến cờ để tránh gọi changeTaskStatus lặp lại không cần thiết
    private final Map<Integer, Boolean> isAutoUpdatingStatus = new HashMap<>();


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
        // Kiểm tra xem có phải đang tự động cập nhật không để tránh vòng lặp
        if (isAutoUpdatingStatus.getOrDefault(taskId, false)) {
            Log.d("TaskViewModel", "Skipping changeTaskStatus for task ID " + taskId + " due to auto-update flag.");
            isAutoUpdatingStatus.remove(taskId); // Reset cờ sau khi kiểm tra
            return;
        }

        Log.d("TaskViewModel", "Manually changing task status for ID " + taskId + " to " + completed);
        taskService.changeTaskStatus(taskId, completed).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    getTaskById(taskId); // Quan trọng: Lấy lại task sau khi thay đổi trạng thái
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
                    taskOperationResult.setValue(response.body()); // Giữ lại để thông báo tạo task thành công
                    // Sau khi tạo, có thể cần load lại danh sách task chung
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
                    taskOperationResult.setValue(null); // Sử dụng null để báo hiệu xóa thành công
                    // Load lại danh sách task sau khi xóa
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
                    // taskOperationResult.setValue(response.body()); // Không dùng cái này nữa
                    getTaskById(taskId); // Lấy lại task sau khi cập nhật để xử lý logic
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
                    singleTaskLiveData.setValue(updatedTask); // Cập nhật LiveData cho task đơn lẻ

                    // Cập nhật task này trong danh sách tasksLiveData
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
                        } else {
                            // Nếu không tìm thấy (ví dụ task mới được lấy về), thêm vào
                            // Hoặc load lại toàn bộ danh sách nếu logic yêu cầu
                        }
                    } else {
                        // Nếu tasksLiveData là null, có thể khởi tạo và thêm task vào
                        List<TaskResponse> newTaskList = new ArrayList<>();
                        newTaskList.add(updatedTask);
                        tasksLiveData.setValue(newTaskList);
                    }


                    // THỰC HIỆN LOGIC TỰ ĐỘNG HOÀN THÀNH TASK CHA
                    if (updatedTask.getTotalSubTasks() > 0) {
                        boolean allSubTasksCompleted = updatedTask.getCompletedSubTasks() == updatedTask.getTotalSubTasks();
                        if (allSubTasksCompleted && !Boolean.TRUE.equals(updatedTask.getCompleted())) {
                            Log.d("TaskViewModel", "All subtasks completed for task ID " + taskId + ". Auto-completing parent task.");
                            isAutoUpdatingStatus.put(taskId, true); // Đặt cờ trước khi gọi
                            changeTaskStatus(taskId, true);
                        } else if (!allSubTasksCompleted && Boolean.TRUE.equals(updatedTask.getCompleted())) {
                            // Trường hợp: Task cha đang hoàn thành, nhưng không phải tất cả subtask đều hoàn thành
                            // (ví dụ: người dùng bỏ check 1 subtask)
                            Log.d("TaskViewModel", "Not all subtasks completed for task ID " + taskId + ". Auto-uncompleting parent task.");
                            isAutoUpdatingStatus.put(taskId, true); // Đặt cờ trước khi gọi
                            changeTaskStatus(taskId, false);
                        }
                    }
                    // Sau khi xử lý logic tự động, thông báo taskOperationResult để UI (ví dụ: TaskAdapter) biết rằng task đã được xử lý
                    // và có thể cần cập nhật lại (ví dụ: reload subtask nếu task cha được mở rộng)
                    // Điều này rất quan trọng để TaskAdapter biết rằng getTaskById đã hoàn tất và có thể an toàn để thực hiện các hành động tiếp theo.
                    taskOperationResult.setValue(updatedTask);


                } else {
                    singleTaskLiveData.setValue(null);
                    String errorMsg = "Failed to get task details: " + response.code() + " - " + response.message();
                    errorMessage.setValue(errorMsg);
                    Log.e("TaskViewModel", "Failed to get task details for ID " + taskId + ": " + errorMsg);
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