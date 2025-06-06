package com.example.apptodo.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateItem {
    private final String month;
    private final String day;
    private final String dayOfWeek;
    private final Date date; // Lưu trữ đối tượng Date

    public DateItem(Date date) {
        this.date = date;
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        this.month = monthFormat.format(date);
        this.day = dayFormat.format(date);
        this.dayOfWeek = dayOfWeekFormat.format(date);
    }

    // Getters
    public String getMonth() { return month; }
    public String getDay() { return day; }
    public String getDayOfWeek() { return dayOfWeek; }
    public Date getFullDate() { return date; }  // Trả về đối tượng Date thay vì String
    public Date getDate() { return date; }
}
