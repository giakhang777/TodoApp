package com.example.apptodo.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DailyReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "DailyReminderReceiver"; // Đặt tag để dễ theo dõi trong Logcat

    @Override
    public void onReceive(Context context, Intent intent) {

        TaskService taskService = RetrofitClient.getTaskService();

        // Lấy thông tin người dùng từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        String username = prefs.getString("username", "User");

        // Kiểm tra xem userId có hợp lệ không
        if (userId == -1) {
            Log.w("DailyReminderReceiver", "UserId is invalid, skipping notification.");
            return; // Nếu không có userId, không làm gì cả
        }


        // Lấy ngày hôm nay
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Gọi API để lấy số task hôm nay
        taskService.getTasksByDate(today, userId).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful()) {
                    int taskCount = 0;
                    if (response.body() != null) {
                        taskCount = response.body().size();  // Lấy số task hôm nay
                    }
                    // Tạo thông báo cá nhân hóa
                    String message = "Good morning, " + username + "! You have " + taskCount + " task" + (taskCount != 1 ? "s" : "") + " today.";
                    Log.d("DailyReminderReceiver", "Notification message: " + message);
                    NotificationHelper.showNotification(context, "Daily Reminder", message);  // Hiển thị thông báo
                } else {
                    Log.e("DailyReminderReceiver", "Failed to retrieve tasks. Error code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                Log.e("DailyReminderReceiver", "API call failed: " + t.getMessage());
            }
        });
    }

}
