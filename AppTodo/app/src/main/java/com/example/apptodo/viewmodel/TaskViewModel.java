package com.example.apptodo.viewmodel;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.apptodo.alarm.TaskReminderReceiver;
import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.request.TaskRequest;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.util.Locale;

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
                    taskOperationResult.setValue(response.body());
                    getTaskById(taskId);
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
                    taskOperationResult.setValue(response.body());
                    getTaskById(taskId);
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
                    } else {
                        // Nếu tasksLiveData là null, có thể khởi tạo và thêm task vào
                        List<TaskResponse> newTaskList = new ArrayList<>();
                        newTaskList.add(updatedTask);
                        tasksLiveData.setValue(newTaskList);
                    }
                } else {
                    singleTaskLiveData.setValue(null);
                    String errorMsg = "Failed to get task details: " + response.code() + " - " + response.message();
                    errorMessage.setValue(errorMsg);
                    Log.e("TaskViewModel", "Failed to get task details for ID " + taskId + ": " + errorMsg);
                    loadAllTasks(1); // Replace with real userId
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

    public void scheduleTaskReminders(List<TaskResponse> tasks, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "App cannot schedule exact alarms. Please grant permission.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (TaskResponse task : tasks) {
            if (task.getReminder() != null && task.getReminder() && task.getReminderTime() != null) {
                String reminderTimeStr = task.getReminderTimeFormated();
                long reminderTime = parseReminderTime(reminderTimeStr);

                if (reminderTime != -1) {
                    setTaskReminder(reminderTime, task.getTitle(), task.getId(), context);
                }
            }
        }
    }

    private void setTaskReminder(long reminderTime, String taskTitle, int taskId, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("taskTitle", taskTitle);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
            );
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime,
                    pendingIntent
            );
        }

        Log.d("TaskReminder", "Reminder set for task: " + taskTitle + " at: " + new Date(reminderTime));
    }

    private long parseReminderTime(String reminderTimeStr) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date reminderDate = formatter.parse(reminderTimeStr);
            if (reminderDate != null) {
                return reminderDate.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void clearTasksLiveData() {
        tasksLiveData.setValue(new ArrayList<>());
    }
}