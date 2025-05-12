package com.example.apptodo.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.adapter.DateAdapter;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.DateItem;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment implements DateAdapter.OnDateClickListener {

    private RecyclerView datesRecyclerView, tasksRecyclerView;
    private TextView dateDisplay, emptyTasksText;
    private DateAdapter dateAdapter;
    private TaskAdapter taskAdapter;
    private List<DateItem> dateItems = new ArrayList<>();
    private List<TaskResponse> taskList = new ArrayList<>();
    private TaskService taskService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        datesRecyclerView = view.findViewById(R.id.datesRecyclerView);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        dateDisplay = view.findViewById(R.id.dateDisplay);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);

        taskService = RetrofitClient.getTaskService();

        taskAdapter = new TaskAdapter(getContext(), taskList);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tasksRecyclerView.setAdapter(taskAdapter);

        dateDisplay.setOnClickListener(v -> showDatePickerDialog());

        setupDateList();

        return view;
    }

    private void setupDateList() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        for (int i = 0; i < 100; i++) {
            dateItems.add(new DateItem(calendar.getTime()));
            calendar.add(Calendar.DATE, 1);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(layoutManager);

        dateAdapter = new DateAdapter(dateItems, this);
        datesRecyclerView.setAdapter(dateAdapter);

        scrollToToday(today);
        updateDateDisplay(today);

        loadTasksForDate(today);
    }

    private void scrollToToday(Date today) {
        for (int i = 0; i < dateItems.size(); i++) {
            if (dateItems.get(i).getFullDate().equals(new DateItem(today).getFullDate())) {
                dateAdapter.updateSelectedPosition(i);
                datesRecyclerView.scrollToPosition(i);
                break;
            }
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        long todayInMillis = calendar.getTimeInMillis();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, monthOfYear, dayOfMonth);
                    updateDateDisplay(selectedDate.getTime());
                    scrollToSelectedDate(selectedDate.getTime());
                    loadTasksForDate(selectedDate.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(todayInMillis);
        datePickerDialog.show();
    }

    private void scrollToSelectedDate(Date selectedDate) {
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(selectedDate);

        for (int i = 0; i < dateItems.size(); i++) {
            Calendar itemCalendar = Calendar.getInstance();
            itemCalendar.setTime(dateItems.get(i).getFullDate());

            if (selectedCalendar.get(Calendar.YEAR) == itemCalendar.get(Calendar.YEAR) &&
                    selectedCalendar.get(Calendar.MONTH) == itemCalendar.get(Calendar.MONTH) &&
                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == itemCalendar.get(Calendar.DAY_OF_MONTH)) {

                dateAdapter.updateSelectedPosition(i);
                datesRecyclerView.smoothScrollToPosition(i);
                break;
            }
        }
    }

    private void updateDateDisplay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String monthName = getMonthName(calendar.get(Calendar.MONTH));
        int year = calendar.get(Calendar.YEAR);
        String dateText = monthName + " " + year;

        dateDisplay.setText(dateText);
    }

    private String getMonthName(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return months[month];
    }

    @Override
    public void onDateClick(DateItem dateItem, int position) {
        dateAdapter.updateSelectedPosition(position);
        updateDateDisplay(dateItem.getFullDate());
        loadTasksForDate(dateItem.getFullDate());
    }

    private void loadTasksForDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(date);

        taskService.getTasksByDate(formattedDate).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.clear();
                    for (TaskResponse task : response.body()) {
                        if (Boolean.FALSE.equals(task.getCompleted())) {
                            taskList.add(task);
                        }
                    }

                    Collections.sort(taskList, (t1, t2) -> getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority()));

                    taskAdapter.setTaskList(taskList);
                    updateEmptyTasksVisibility();
                } else {
                    taskList.clear();
                    taskAdapter.setTaskList(taskList);
                    updateEmptyTasksVisibility();
                    Toast.makeText(getContext(), "No tasks found for selected date", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                taskList.clear();
                taskAdapter.setTaskList(taskList);
                updateEmptyTasksVisibility();
                Toast.makeText(getContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyTasksVisibility() {
        if (taskList.isEmpty()) {
            emptyTasksText.setVisibility(View.VISIBLE);
            tasksRecyclerView.setVisibility(View.GONE);
        } else {
            emptyTasksText.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 3;
        switch (priority.toLowerCase()) {
            case "high": return 0;
            case "medium": return 1;
            case "low": return 2;
            default: return 3;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (taskAdapter != null) {
            taskAdapter.cleanup();
        }
        datesRecyclerView = null;
        tasksRecyclerView = null;
        dateDisplay = null;
        emptyTasksText = null;
        dateAdapter = null;
        taskAdapter = null;
    }
}