package com.example.apptodo.model.response;

import com.example.apptodo.model.BaseModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TaskResponse extends BaseModel {
    private Integer id;
    private String title;
    private String priority;
    private String dueDate;
    private String label;
    private String project;
    private String description;
    private Boolean reminder;
    private String reminderTime;
    private Boolean completed;
    private int totalSubTasks;
    private int completedSubTasks;

    public TaskResponse(Integer id, String project, String title, String priority, String dueDate,
                        String label, String description, Boolean reminder, String reminderTime, Boolean completed) {
        this.id = id;
        this.title = title;
        this.project = project;
        this.priority = priority;
        this.dueDate = dueDate;
        this.label = label;
        this.description = description;
        this.reminder = reminder;
        this.reminderTime = reminderTime;
        this.completed = completed;
    }

    public int getTotalSubTasks() {
        return totalSubTasks;
    }

    public void setTotalSubTasks(int totalSubTasks) {
        this.totalSubTasks = totalSubTasks;
    }

    public int getCompletedSubTasks() {
        return completedSubTasks;
    }

    public void setCompletedSubTasks(int completedSubTasks) {
        this.completedSubTasks = completedSubTasks;
    }

    // Getter và Setter cho reminderTime
    public String getReminderTime() {
        if (reminderTime != null && !reminderTime.isEmpty()) {
            try {
                // Chuyển reminderTime từ String thành Date
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                Date reminderDate = formatter.parse(reminderTime);

                // Lấy giờ và phút từ Date
                int hour = reminderDate.getHours();
                int minute = reminderDate.getMinutes();

                // Trả về chuỗi theo định dạng "HH:mm"
                return String.format("%02d:%02d", hour, minute);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getReminder() {
        return reminder;
    }

    public void setReminder(Boolean reminder) {
        this.reminder = reminder;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}

