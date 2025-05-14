package com.example.apptodo.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.adapter.DateAdapter;
import com.example.apptodo.adapter.TaskAdapter;
import com.example.apptodo.model.DateItem;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;

public class CalendarFragment extends Fragment implements DateAdapter.OnDateClickListener, TaskAdapter.OnTaskStatusUpdatedListener {
    private RecyclerView datesRecyclerView, tasksRecyclerView;
    private TextView dateDisplay, emptyTasksText;
    private DateAdapter dateAdapter;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> taskList = new ArrayList<>();
    private TaskViewModel taskViewModel;
    private SharedUserViewModel sharedUserViewModel;
    private Integer userId;
    private Date selectedDateForTasks;
    private List<DateItem> dateItems = new ArrayList<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        datesRecyclerView = view.findViewById(R.id.datesRecyclerView);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        dateDisplay = view.findViewById(R.id.dateDisplay);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);

        taskAdapter = new TaskAdapter(getContext(), new ArrayList<>(), (TaskAdapter.OnItemClickListener) requireActivity(), this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tasksRecyclerView.setAdapter(taskAdapter);

        dateDisplay.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), receivedTasks -> {
            List<TaskResponse> filteredAndSortedTasks = new ArrayList<>();
            if (receivedTasks != null) {
                for (TaskResponse task : receivedTasks) {
                    if (Boolean.FALSE.equals(task.getCompleted())) {
                        filteredAndSortedTasks.add(task);
                    }
                }
                Collections.sort(filteredAndSortedTasks, (t1, t2) -> getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority()));
            }

            this.taskList.clear();
            this.taskList.addAll(filteredAndSortedTasks);
            taskAdapter.setTaskList(this.taskList);
            updateEmptyTasksVisibility();
        });

        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                if (this.taskList.isEmpty()) {
                    Toast.makeText(getContext(), "Error loading tasks: " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });

        taskViewModel.getTaskOperationResult().observe(getViewLifecycleOwner(), taskResponse -> {
            if (userId != null && selectedDateForTasks != null && isAdded()) {
                loadTasksForDate(selectedDateForTasks);
            }
        });

        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null && userResponse.getId() != null && isAdded()) {
                this.userId = userResponse.getId();
                setupDateList();

                if (selectedDateForTasks == null) {
                    selectedDateForTasks = Calendar.getInstance().getTime();
                }
                updateDateDisplay(selectedDateForTasks);
                loadTasksForDate(selectedDateForTasks);
            } else {
                if (isAdded()) {
                    Toast.makeText(getContext(), "User not logged in or data unavailable. Cannot load tasks.", Toast.LENGTH_LONG).show();
                }
                this.taskList.clear();
                taskAdapter.setTaskList(this.taskList);
                updateEmptyTasksVisibility();
            }
        });
    }

    private void setupDateList() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        dateItems.clear();
        for (int i = 0; i < 100; i++) {
            dateItems.add(new DateItem(calendar.getTime()));
            calendar.add(Calendar.DATE, 1);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(layoutManager);

        dateAdapter = new DateAdapter(dateItems, this);
        datesRecyclerView.setAdapter(dateAdapter);

        int todayPosition = -1;
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String todayStr = fullDateFormat.format(today);

        for (int i = 0; i < dateItems.size(); i++) {
            if (fullDateFormat.format(dateItems.get(i).getFullDate()).equals(todayStr)) {
                todayPosition = i;
                break;
            }
        }

        if (todayPosition != -1) {
            dateAdapter.updateSelectedPosition(todayPosition);
            datesRecyclerView.scrollToPosition(todayPosition);
            if (selectedDateForTasks == null) {
                selectedDateForTasks = dateItems.get(todayPosition).getFullDate();
                updateDateDisplay(selectedDateForTasks);
            }
        } else if (!dateItems.isEmpty() && selectedDateForTasks == null) {
            dateAdapter.updateSelectedPosition(0);
            selectedDateForTasks = dateItems.get(0).getFullDate();
            updateDateDisplay(selectedDateForTasks);
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        if (selectedDateForTasks != null) {
            calendar.setTime(selectedDateForTasks);
        }
        long minDateMillis = Calendar.getInstance().getTimeInMillis() - 1000;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, monthOfYear, dayOfMonth);
                    Date newSelectedDate = selectedCal.getTime();

                    selectedDateForTasks = newSelectedDate;
                    updateDateDisplay(selectedDateForTasks);
                    scrollToSelectedDateInStrip(selectedDateForTasks);
                    if (userId != null) {
                        loadTasksForDate(selectedDateForTasks);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.getDatePicker().setMinDate(minDateMillis);
        datePickerDialog.show();
    }

    private void scrollToSelectedDateInStrip(Date dateToScrollTo) {
        SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String dateToScrollToStr = fullDateFormat.format(dateToScrollTo);
        int targetPosition = -1;

        for (int i = 0; i < dateItems.size(); i++) {
            if (fullDateFormat.format(dateItems.get(i).getFullDate()).equals(dateToScrollToStr)) {
                targetPosition = i;
                break;
            }
        }

        if (targetPosition != -1) {
            dateAdapter.updateSelectedPosition(targetPosition);
            datesRecyclerView.smoothScrollToPosition(targetPosition);
        } else {
        }
    }

    private void updateDateDisplay(Date date) {
        if (date == null) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        int year = calendar.get(Calendar.YEAR);
        String dateText = monthName + " " + year;

        dateDisplay.setText(dateText);
    }


    @Override
    public void onDateClick(DateItem dateItem, int position) {
        dateAdapter.updateSelectedPosition(position);
        selectedDateForTasks = dateItem.getFullDate();

        updateDateDisplay(selectedDateForTasks);
        if (userId != null) {
            loadTasksForDate(selectedDateForTasks);
        } else {
            Toast.makeText(getContext(), "User ID not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTasksForDate(Date date) {
        if (date == null) {
            return;
        }
        if (userId == null) {
            if (isAdded()) Toast.makeText(getContext(), "User ID not available. Cannot load tasks.", Toast.LENGTH_SHORT).show();
            this.taskList.clear();
            taskAdapter.setTaskList(this.taskList);
            updateEmptyTasksVisibility();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(date);
        taskViewModel.loadTasksByDate(formattedDate, userId);
    }

    @Override
    public void onTaskStatusUpdated(int taskId, boolean completed) {
        if (sharedUserViewModel.getUser().getValue() != null && isAdded() && userId != null) {
            taskViewModel.changeTaskStatus(taskId, completed);
        } else {
        }
    }

    private void updateEmptyTasksVisibility() {
        boolean isEmpty = this.taskList.isEmpty();
        if (emptyTasksText != null && tasksRecyclerView != null) {
            emptyTasksText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            tasksRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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
