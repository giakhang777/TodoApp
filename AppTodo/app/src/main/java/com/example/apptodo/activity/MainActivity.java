package com.example.apptodo.activity;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.apptodo.R;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.alarm.DailyReminderReceiver;
import com.example.apptodo.databinding.ActivityMainBinding;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.request.TaskRequest;
import com.example.apptodo.model.response.LabelResponse;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.LabelViewModel;
import com.example.apptodo.viewmodel.ProjectViewModel;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnItemClickListener {

    private ActivityMainBinding binding;
    private SharedUserViewModel sharedUserViewModel;
    private TaskViewModel taskViewModel;
    private ProjectViewModel projectViewModel;
    private LabelViewModel labelViewModel;

    private Dialog taskDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Kiểm tra quyền "Schedule Exact Alarms" từ Android 12 (API level 31)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                // Nếu không có quyền lên lịch báo thức chính xác, yêu cầu quyền từ người dùng
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intent);
                return; // Đợi người dùng cấp quyền và gọi lại mã tiếp theo sau
            }
        }

        // Kiểm tra và yêu cầu quyền "POST_NOTIFICATIONS" từ Android 13 (API level 33) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Yêu cầu quyền nếu chưa được cấp
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            } else {
                // Nếu quyền đã được cấp, có thể gửi thông báo
            //    testNotificationNow();
                scheduleDailyReminder();
            }
        } else {
            // Trên các phiên bản Android trước Android 13, không cần yêu cầu quyền này
         //   testNotificationNow();
            scheduleDailyReminder();
        }

        sharedUserViewModel = new ViewModelProvider(this).get(SharedUserViewModel.class);
        UserResponse user = (UserResponse) getIntent().getSerializableExtra("user");
        if (user != null) {
            sharedUserViewModel.setUser(user);
        } else {
            Toast.makeText(this, "User data not available. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        projectViewModel = new ViewModelProvider(this).get(ProjectViewModel.class);
        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);

        sharedUserViewModel.getUser().observe(this, userResponse -> {
            if (userResponse != null && userResponse.getId() != null) {
                int userId = userResponse.getId();
                if (projectViewModel.getProjectList().getValue() == null || projectViewModel.getProjectList().getValue().isEmpty()) {
                    projectViewModel.fetchProjects(userId);
                }
                if (labelViewModel.getLabels().getValue() == null || labelViewModel.getLabels().getValue().isEmpty()) {
                    labelViewModel.loadLabels(userId);
                }
            }
        });

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment(), false);
        }

        binding.bottomNavigationView.setBackground(null);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment(), false);
            } else if (itemId == R.id.calendar) {
                replaceFragment(new CalendarFragment(), true);
            } else if (itemId == R.id.tasks) {
                replaceFragment(new TasksFragment(), true);
            } else if (itemId == R.id.profile) {
                replaceFragment(new ProfileFragment(), true);
            }
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showAddTaskDialog());

        taskViewModel.getTaskOperationResult().observe(this, taskResponse -> {
            UserResponse currentUser = sharedUserViewModel.getUser().getValue();
            boolean wasDeleteOperation = (taskResponse == null && taskDialog != null && taskDialog.isShowing());

            if (taskResponse != null) {
                Toast.makeText(this, "Task operation successful: " + taskResponse.getTitle(), Toast.LENGTH_SHORT).show();
            } else if (wasDeleteOperation) {
                Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
            }

            if (currentUser != null && currentUser.getId() != null) {
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
                taskViewModel.loadTasksByDate(today, currentUser.getId());
            }
            if (taskDialog != null && taskDialog.isShowing()) {
                taskDialog.dismiss();
            }
        });
        taskViewModel.getTasks().observe(this, tasks -> {
            if (tasks != null && !tasks.isEmpty()) {
                // Gọi phương thức để lập lịch nhắc nhở cho các task đã tải về
                taskViewModel.scheduleTaskReminders(tasks, this);  // Truyền context vào đây
            }
        });

        taskViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, "Task error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });


    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp -> Gọi lại hai hàm
             //   testNotificationNow();
                scheduleDailyReminder();
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền để hiển thị thông báo", Toast.LENGTH_SHORT).show();
            }
        }
    }


    public void navigateToProjectFragment(int projectId) {
        ProjectFragment projectFragment = ProjectFragment.newInstance(projectId);
        replaceFragment(projectFragment, true);
    }

    public void navigateToCalendarFragment() {
        replaceFragment(new CalendarFragment(), true);
        binding.bottomNavigationView.setSelectedItemId(R.id.calendar);
    }


    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, fragment, fragment.getClass().getSimpleName());
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        transaction.commit();
    }

    private void showAddTaskDialog() {
        taskDialog = new Dialog(this);
        taskDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        taskDialog.setContentView(R.layout.bottomsheetlayout);

        ImageView cancelButton = taskDialog.findViewById(R.id.closeButton);
        cancelButton.setOnClickListener(v -> taskDialog.dismiss());

        TextInputEditText taskTitleEditText = taskDialog.findViewById(R.id.taskTitle);
        TextInputEditText taskDescriptionEditText = taskDialog.findViewById(R.id.taskDescription);
        Spinner prioritySpinner = taskDialog.findViewById(R.id.prioritySpinner);
        TextInputEditText scheduledDateEditText = taskDialog.findViewById(R.id.scheduledDate);
        TextInputEditText reminderTimeEditText = taskDialog.findViewById(R.id.reminderTime);
        Spinner projectSpinner = taskDialog.findViewById(R.id.projectSpinner);
        Spinner labelSpinner = taskDialog.findViewById(R.id.labelSpinner);
        Button saveButton = taskDialog.findViewById(R.id.saveButton);
        Button deleteButton = taskDialog.findViewById(R.id.deleteButton);
        if (deleteButton != null) {
            deleteButton.setVisibility(View.GONE);
        }

        SimpleDateFormat sdfToday = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdfToday.format(Calendar.getInstance().getTime());
        scheduledDateEditText.setText(today);

        String[] priorities = {"Low", "Medium", "High"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        projectViewModel.getProjectList().observe(this, projectList -> {
            List<String> projectNames = new ArrayList<>();
            projectNames.add("Select Project");
            if (projectList != null) {
                for (ProjectResponse project : projectList) {
                    projectNames.add(project.getName());
                }
            }
            ArrayAdapter<String> projectArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectNames);
            projectArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            projectSpinner.setAdapter(projectArrayAdapter);
        });

        labelViewModel.getLabels().observe(this, labelList -> {
            List<String> labelTitles = new ArrayList<>();
            labelTitles.add("Select Label");
            if (labelList != null) {
                for (LabelResponse label : labelList) {
                    labelTitles.add(label.getTitle());
                }
            }
            ArrayAdapter<String> labelArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labelTitles);
            labelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            labelSpinner.setAdapter(labelArrayAdapter);
        });

        scheduledDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                if (!scheduledDateEditText.getText().toString().isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date d = sdf.parse(scheduledDateEditText.getText().toString());
                    if (d != null) calendar.setTime(d);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view1, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedSelectedDate = sdf.format(selectedDate.getTime());
                        scheduledDateEditText.setText(formattedSelectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        reminderTimeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            try {
                if (!reminderTimeEditText.getText().toString().isEmpty()) {
                    String[] timeParts = reminderTimeEditText.getText().toString().split(":");
                    if (timeParts.length == 2) {
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view1, hourOfDay, selectedMinute) -> {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, selectedMinute);
                        reminderTimeEditText.setText(time);
                    },
                    hour,
                    currentMinute,
                    true
            );
            timePickerDialog.show();
        });

        saveButton.setText("SAVE");

        saveButton.setOnClickListener(v -> {
            String title = taskTitleEditText.getText().toString().trim();
            String description = taskDescriptionEditText.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();
            String dueDate = scheduledDateEditText.getText().toString();
            String reminderTimeStr = reminderTimeEditText.getText().toString();
            String selectedProjectName = projectSpinner.getSelectedItemPosition() > 0 ? projectSpinner.getSelectedItem().toString() : null;
            String selectedLabelTitle = labelSpinner.getSelectedItemPosition() > 0 ? labelSpinner.getSelectedItem().toString() : null;

            if (title.isEmpty()) {
                taskTitleEditText.setError("Title is required");
                return;
            }
            if (dueDate.isEmpty()){
                scheduledDateEditText.setError("Due date is required");
                return;
            }

            UserResponse currentUser = sharedUserViewModel.getUser().getValue();
            if (currentUser == null || currentUser.getId() == null) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                return;
            }
            int userId = currentUser.getId();
            Integer projectId = null;
            if (selectedProjectName != null && projectViewModel.getProjectList().getValue() != null) {
                for (ProjectResponse project : projectViewModel.getProjectList().getValue()) {
                    if (project.getName().equals(selectedProjectName)) {
                        projectId = project.getId();
                        break;
                    }
                }
            }
            Integer labelId = null;
            if (selectedLabelTitle != null && labelViewModel.getLabels().getValue() != null) {
                for (LabelResponse label : labelViewModel.getLabels().getValue()) {
                    if (label.getTitle().equals(selectedLabelTitle)) {
                        labelId = label.getId();
                        break;
                    }
                }
            }
            TaskRequest taskRequest = new TaskRequest(
                    title,
                    userId,
                    priority,
                    dueDate,
                    description.isEmpty() ? null : description,
                    !reminderTimeStr.isEmpty(),
                    reminderTimeStr.isEmpty() ? null : dueDate + "T" + reminderTimeStr + ":00",
                    projectId,
                    labelId
            );
            taskViewModel.createTask(taskRequest);
        });

        taskDialog.show();
        if (taskDialog.getWindow() != null) {
            taskDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            taskDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            taskDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            taskDialog.getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    private void showEditTaskDialog(TaskResponse taskToEdit) {
        if (taskToEdit == null || taskToEdit.getId() == null) {
            Toast.makeText(this, "Invalid task for editing.", Toast.LENGTH_SHORT).show();
            return;
        }

        taskDialog = new Dialog(this);
        taskDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        taskDialog.setContentView(R.layout.bottomsheetlayout);

        ImageView cancelButton = taskDialog.findViewById(R.id.closeButton);
        cancelButton.setOnClickListener(v -> taskDialog.dismiss());

        TextInputEditText taskTitleEditText = taskDialog.findViewById(R.id.taskTitle);
        TextInputEditText taskDescriptionEditText = taskDialog.findViewById(R.id.taskDescription);
        Spinner prioritySpinner = taskDialog.findViewById(R.id.prioritySpinner);
        TextInputEditText scheduledDateEditText = taskDialog.findViewById(R.id.scheduledDate);
        TextInputEditText reminderTimeEditText = taskDialog.findViewById(R.id.reminderTime);
        Spinner projectSpinner = taskDialog.findViewById(R.id.projectSpinner);
        Spinner labelSpinner = taskDialog.findViewById(R.id.labelSpinner);
        Button saveButton = taskDialog.findViewById(R.id.saveButton);
        Button deleteButton = taskDialog.findViewById(R.id.deleteButton);
        if (deleteButton != null) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                if (taskToEdit.getId() != null) {
                    taskViewModel.deleteTask(taskToEdit.getId());
                } else {
                    Toast.makeText(this, "Cannot delete task without ID", Toast.LENGTH_SHORT).show();
                }
            });
        }

        String[] priorities = {"Low", "Medium", "High"};
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, priorities);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(priorityAdapter);

        String taskSelectedProjectName = taskToEdit.getProject();
        String taskSelectedLabelTitle = taskToEdit.getLabel();

        projectViewModel.getProjectList().observe(this, projectList -> {
            List<String> projectNames = new ArrayList<>();
            projectNames.add("Select Project");
            int selectedProjectPosition = 0;
            if (projectList != null) {
                for (ProjectResponse project : projectList) {
                    projectNames.add(project.getName());
                }
                if (taskSelectedProjectName != null) {
                    for (int i = 0; i < projectNames.size(); i++) {
                        if (projectNames.get(i).equals(taskSelectedProjectName)) {
                            selectedProjectPosition = i;
                            break;
                        }
                    }
                }
            }
            ArrayAdapter<String> projectArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, projectNames);
            projectArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            projectSpinner.setAdapter(projectArrayAdapter);
            if (projectNames.size() > selectedProjectPosition) {
                projectSpinner.setSelection(selectedProjectPosition);
            }
        });

        labelViewModel.getLabels().observe(this, labelList -> {
            List<String> labelTitles = new ArrayList<>();
            labelTitles.add("Select Label");
            int selectedLabelPosition = 0;
            if (labelList != null && !labelList.isEmpty()) {
                for (LabelResponse label : labelList) {
                    labelTitles.add(label.getTitle());
                }
                if (taskSelectedLabelTitle != null) {
                    for (int i = 0; i < labelTitles.size(); i++) {
                        if (labelTitles.get(i).equals(taskSelectedLabelTitle)) {
                            selectedLabelPosition = i;
                            break;
                        }
                    }
                }
            }
            ArrayAdapter<String> labelArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labelTitles);
            labelArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            labelSpinner.setAdapter(labelArrayAdapter);
            if (labelTitles.size() > selectedLabelPosition) {
                labelSpinner.setSelection(selectedLabelPosition);
            } else {
                labelSpinner.setSelection(0);
            }
        });

        scheduledDateEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (taskToEdit.getDueDate() != null && !taskToEdit.getDueDate().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Date dueDate = sdf.parse(taskToEdit.getDueDate());
                    if (dueDate != null) {
                        calendar.setTime(dueDate);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    this,
                    (view1, year, month, dayOfMonth) -> {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        String formattedSelectedDate = sdf.format(selectedDate.getTime());
                        scheduledDateEditText.setText(formattedSelectedDate);
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });

        reminderTimeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            if (taskToEdit.getReminderTime() != null && !taskToEdit.getReminderTime().isEmpty()) {
                try {
                    String[] timeParts = taskToEdit.getReminderTime().split(":");
                    if (timeParts.length == 2) {
                        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                        calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    this,
                    (view1, hourOfDay, selectedMinute) -> {
                        String time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, selectedMinute);
                        reminderTimeEditText.setText(time);
                    },
                    hour,
                    currentMinute,
                    true
            );
            timePickerDialog.show();
        });

        taskTitleEditText.setText(taskToEdit.getTitle());
        taskDescriptionEditText.setText(taskToEdit.getDescription());

        String taskPriority = taskToEdit.getPriority();
        if (taskPriority != null) {
            int priorityPosition = 0;
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equalsIgnoreCase(taskPriority)) {
                    priorityPosition = i;
                    break;
                }
            }
            prioritySpinner.setSelection(priorityPosition);
        }

        if (taskToEdit.getDueDate() != null) {
            scheduledDateEditText.setText(taskToEdit.getDueDate());
        }
        if (taskToEdit.getReminderTime() != null) {
            reminderTimeEditText.setText(taskToEdit.getReminderTime());
        }

        saveButton.setText("UPDATE");
        saveButton.setOnClickListener(v -> {
            String title = taskTitleEditText.getText().toString().trim();
            String description = taskDescriptionEditText.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();
            String dueDate = scheduledDateEditText.getText().toString();
            String reminderTimeStr = reminderTimeEditText.getText().toString();
            String selectedProjectNameFromSpinner = projectSpinner.getSelectedItemPosition() > 0 ? projectSpinner.getSelectedItem().toString() : null;
            String selectedLabelTitleFromSpinner = labelSpinner.getSelectedItemPosition() > 0 ? labelSpinner.getSelectedItem().toString() : null;

            if (title.isEmpty()) {
                taskTitleEditText.setError("Task title is required");
                return;
            }
            if (dueDate.isEmpty()){
                scheduledDateEditText.setError("Due date is required");
                return;
            }

            UserResponse currentUser = sharedUserViewModel.getUser().getValue();
            if (currentUser == null || currentUser.getId() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            int userId = currentUser.getId();
            Integer projectId = null;
            if (selectedProjectNameFromSpinner != null && projectViewModel.getProjectList().getValue() != null) {
                for (ProjectResponse project : projectViewModel.getProjectList().getValue()) {
                    if (project.getName().equals(selectedProjectNameFromSpinner)) {
                        projectId = project.getId();
                        break;
                    }
                }
            }
            Integer labelId = null;
            if (selectedLabelTitleFromSpinner != null && labelViewModel.getLabels().getValue() != null) {
                for (LabelResponse label : labelViewModel.getLabels().getValue()) {
                    if (label.getTitle().equals(selectedLabelTitleFromSpinner)) {
                        labelId = label.getId();
                        break;
                    }
                }
            }
            TaskRequest taskRequest = new TaskRequest(
                    title,
                    userId,
                    priority,
                    dueDate,
                    description.isEmpty() ? null : description,
                    !reminderTimeStr.isEmpty(),
                    reminderTimeStr.isEmpty() ? null : dueDate + "T" + reminderTimeStr + ":00",
                    projectId,
                    labelId
            );
            taskViewModel.updateTask(taskToEdit.getId(), taskRequest);
        });

        taskDialog.show();
        if (taskDialog.getWindow() != null) {
            taskDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            taskDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            taskDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            taskDialog.getWindow().setGravity(Gravity.BOTTOM);
        }
    }

    private void scheduleDailyReminder() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, DailyReminderReceiver.class);  // receiver của bạn
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,7);
        calendar.set(Calendar.MINUTE, 30);
        calendar.set(Calendar.SECOND, 0);

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_YEAR, 1); // Nếu đã qua 7h, đặt lịch cho ngày hôm sau
        }
        Log.d("TimeNow", new Date().toString());
        Log.d("TestNotification", "Alarm set for: " + calendar.getTime());
        // Đặt lịch báo thức (AlarmManager) lặp lại mỗi ngày lúc 7h sáng
        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                pendingIntent
        );
    }




    @Override
    public void onItemClick(TaskResponse task) {
        showEditTaskDialog(task);
    }
}