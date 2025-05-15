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

import com.example.apptodo.alarm.DailyReminderReceiver;
import com.example.apptodo.alarm.TaskReminderReceiver;
import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.request.TaskRequest;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    public void scheduleTaskReminders(List<TaskResponse> tasks, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Check if we have permission to schedule exact alarms (only for Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // If the app can't schedule exact alarms, show a message to the user
                Toast.makeText(context, "App cannot schedule exact alarms. Please grant permission.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (TaskResponse task : tasks) {
            if (task.getReminder() != null && task.getReminder() && task.getReminderTime() != null) {
                String reminderTimeStr = task.getReminderTimeFormated();
                long reminderTime = parseReminderTime(reminderTimeStr);

                if (reminderTime != -1) {
                    // Lập lịch cho mỗi task có reminder
                    setTaskReminder(reminderTime, task.getTitle(), task.getId(), context);
                }
            }
        }
    }

    private void setTaskReminder(long reminderTime, String taskTitle, int taskId, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Create an Intent to trigger the DailyReminderReceiver
        Intent intent = new Intent(context, TaskReminderReceiver.class);
        intent.putExtra("taskTitle", taskTitle);  // Pass task title into the Intent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Schedule the alarm only if exact alarms are allowed
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
            // Định dạng đúng của reminderTime: "2025-05-15T15:00:00"
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date reminderDate = formatter.parse(reminderTimeStr);

            if (reminderDate != null) {
                return reminderDate.getTime();  // Trả về time dạng milliseconds
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return -1; // Nếu không parse được thì trả về -1
    }



}
