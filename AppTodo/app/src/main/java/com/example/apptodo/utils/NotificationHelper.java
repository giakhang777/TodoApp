package com.example.apptodo.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.apptodo.R;

public class NotificationHelper {
    public static void showNotification(Context context, String title, String message) {
        String channelId = "todo_channel";
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Kiểm tra phiên bản Android và tạo kênh nếu chưa tồn tại
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = notificationManager.getNotificationChannel(channelId);
            if (channel == null) {
                // Nếu chưa có kênh, tạo kênh mới
                channel = new NotificationChannel(
                        channelId, "Todoist Reminder", NotificationManager.IMPORTANCE_HIGH);
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification) // Biểu tượng thông báo
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Thiết lập mặc định như âm thanh, rung, đèn

        // Hiển thị thông báo
        notificationManager.notify(1001, builder.build());
    }
}
