package com.example.apptodo.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.apptodo.R;

public class TaskReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "task_reminder_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Extract the task title from the Intent
        String taskTitle = intent.getStringExtra("taskTitle");

        // Handle the logic for the reminder
        if (taskTitle != null) {
            // Show a Toast (optional, you can remove it if you want only the notification)
            Toast.makeText(context, "Reminder for task: " + taskTitle, Toast.LENGTH_SHORT).show();

            // Show the notification
            showNotification(context, taskTitle);
            Log.d("TaskReminderReceiver", "Reminder triggered for task: " + taskTitle);
        } else {
            Log.w("TaskReminderReceiver", "No task title received in the intent.");
        }
    }

    private void showNotification(Context context, String taskTitle) {
        // Create NotificationManager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a NotificationChannel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Task Reminders";
            String description = "Notifications for task reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Your notification icon
                .setContentTitle("Task Reminder")
                .setContentText("Reminder for task: " + taskTitle)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) // Dismiss the notification when tapped
                .build();

        // Show the notification
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
