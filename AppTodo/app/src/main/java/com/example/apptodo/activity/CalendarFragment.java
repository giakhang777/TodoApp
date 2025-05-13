package com.example.apptodo.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable; // Import Nullable
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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

    private static final String TAG = "CalendarFragment"; // Logging Tag

    private RecyclerView datesRecyclerView, tasksRecyclerView;
    private TextView dateDisplay, emptyTasksText;
    private DateAdapter dateAdapter;
    private TaskAdapter taskAdapter;
    private List<TaskResponse> taskList = new ArrayList<>(); // This is the fragment's local list for the adapter
    private TaskViewModel taskViewModel;
    private SharedUserViewModel sharedUserViewModel;
    private Integer userId;
    private Date selectedDateForTasks; // Store the full Date object
    private List<DateItem> dateItems = new ArrayList<>();
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        datesRecyclerView = view.findViewById(R.id.datesRecyclerView);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        dateDisplay = view.findViewById(R.id.dateDisplay);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);

        // Initialize adapter with an empty list first
        taskAdapter = new TaskAdapter(getContext(), new ArrayList<>(), null, this);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        tasksRecyclerView.setAdapter(taskAdapter);

        dateDisplay.setOnClickListener(v -> showDatePickerDialog());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class); // Scoped to this fragment

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), receivedTasks -> {
            Log.d(TAG, "Task observer onChanged. Received tasks (raw from ViewModel) count: " + (receivedTasks != null ? receivedTasks.size() : "null"));

            List<TaskResponse> filteredAndSortedTasks = new ArrayList<>();
            if (receivedTasks != null) {
                for (TaskResponse task : receivedTasks) {
                    // Filter for tasks relevant to this fragment (e.g., not completed)
                    if (Boolean.FALSE.equals(task.getCompleted())) {
                        filteredAndSortedTasks.add(task);
                    }
                }
                Collections.sort(filteredAndSortedTasks, (t1, t2) -> getPriorityValue(t1.getPriority()) - getPriorityValue(t2.getPriority()));
            }

            Log.d(TAG, "Tasks after filtering/sorting for adapter: " + filteredAndSortedTasks.size());
            this.taskList.clear();
            this.taskList.addAll(filteredAndSortedTasks); // Update fragment's list
            taskAdapter.setTaskList(this.taskList); // Update adapter with the new list
            updateEmptyTasksVisibility();
        });

        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Log.e(TAG, "Error from ViewModel: " + errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                // Potentially clear the error message in ViewModel or handle it so it doesn't re-show on config change
                // taskViewModel.clearErrorMessage(); // You would need to add this method in ViewModel
            }
        });

        taskViewModel.getTaskOperationResult().observe(getViewLifecycleOwner(), taskResponse -> {
            // taskResponse can be the updated TaskResponse or null if it was a deletion
            Log.d(TAG, "TaskOperationResult observed. Reloading tasks for current date.");
            // Reload tasks for the currently selected date
            if (userId != null && selectedDateForTasks != null && isAdded()) {
                loadTasksForDate(selectedDateForTasks);
            }
        });

        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null && userResponse.getId() != null && isAdded()) {
                this.userId = userResponse.getId();
                Log.d(TAG, "User obtained. userId: " + this.userId);

                setupDateList(); // Setup date strip

                if (selectedDateForTasks == null) { // Initialize selectedDateForTasks if null
                    selectedDateForTasks = Calendar.getInstance().getTime(); // Default to today
                }
                updateDateDisplay(selectedDateForTasks); // Update month/year display
                loadTasksForDate(selectedDateForTasks); // Load tasks for the initially selected/default date
            } else {
                Log.w(TAG, "User not logged in or data unavailable. UserResponse: " + userResponse + ", isAdded: " + isAdded());
                if (isAdded()) { // Only show toast if fragment is added
                    Toast.makeText(getContext(), "User not logged in or data unavailable. Cannot load tasks.", Toast.LENGTH_LONG).show();
                }
                this.taskList.clear();
                taskAdapter.setTaskList(this.taskList);
                updateEmptyTasksVisibility();
            }
        });
    }

    private void setupDateList() {
        Log.d(TAG, "setupDateList");
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        dateItems.clear(); // Clear previous items if re-setting up
        for (int i = 0; i < 100; i++) { // Populate for next 100 days
            dateItems.add(new DateItem(calendar.getTime()));
            calendar.add(Calendar.DATE, 1);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(layoutManager);

        dateAdapter = new DateAdapter(dateItems, this);
        datesRecyclerView.setAdapter(dateAdapter);

        // Find today's position and scroll to it
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
            // If selectedDateForTasks is still null, set it to today and load
            if (selectedDateForTasks == null) {
                selectedDateForTasks = dateItems.get(todayPosition).getFullDate();
                updateDateDisplay(selectedDateForTasks);
                if (userId != null) loadTasksForDate(selectedDateForTasks);
            }
        } else if (!dateItems.isEmpty() && selectedDateForTasks == null) {
            // Default to the first date in the list if today is not in the generated range (should not happen with 100 days)
            dateAdapter.updateSelectedPosition(0);
            selectedDateForTasks = dateItems.get(0).getFullDate();
            updateDateDisplay(selectedDateForTasks);
            if (userId != null) loadTasksForDate(selectedDateForTasks);
        }
    }

    private void showDatePickerDialog() {
        Log.d(TAG, "showDatePickerDialog");
        Calendar calendar = Calendar.getInstance();
        // If a date is already selected, use it as the dialog's default
        if (selectedDateForTasks != null) {
            calendar.setTime(selectedDateForTasks);
        }
        long minDateMillis = Calendar.getInstance().getTimeInMillis(); // Today as min date

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar selectedCal = Calendar.getInstance();
                    selectedCal.set(year, monthOfYear, dayOfMonth);
                    Date newSelectedDate = selectedCal.getTime();

                    Log.d(TAG, "Date selected from picker: " + newSelectedDate);
                    selectedDateForTasks = newSelectedDate; // Update the stored selected date
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

        // datePickerDialog.getDatePicker().setMinDate(minDateMillis); // Optional: restrict to today and future
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
            Log.d(TAG, "Scrolling date strip to position: " + targetPosition);
            dateAdapter.updateSelectedPosition(targetPosition);
            datesRecyclerView.smoothScrollToPosition(targetPosition);
        } else {
            Log.d(TAG, "Selected date not found in current date strip, may need to repopulate or extend strip.");
            // Handle case where selected date is outside the current strip (e.g., repopulate dateItems)
            // For now, we assume it's within the 100 days.
        }
    }

    private void updateDateDisplay(Date date) {
        if (date == null) {
            Log.w(TAG, "updateDateDisplay called with null date");
            return;
        }
        Log.d(TAG, "updateDateDisplay for: " + date.toString());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String monthName = new SimpleDateFormat("MMMM", Locale.getDefault()).format(calendar.getTime());
        int year = calendar.get(Calendar.YEAR);
        String dateText = monthName + " " + year;

        dateDisplay.setText(dateText);
    }


    @Override
    public void onDateClick(DateItem dateItem, int position) {
        Log.d(TAG, "onDateClick: " + dateItem.getFullDate().toString() + " at position: " + position);
        dateAdapter.updateSelectedPosition(position); // Visually select in strip
        selectedDateForTasks = dateItem.getFullDate(); // Update stored selected date

        updateDateDisplay(selectedDateForTasks);
        if (userId != null) {
            loadTasksForDate(selectedDateForTasks);
        } else {
            Log.w(TAG, "onDateClick - UserId is null, cannot load tasks.");
            Toast.makeText(getContext(), "User ID not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTasksForDate(Date date) {
        if (date == null) {
            Log.w(TAG, "loadTasksForDate called with null date.");
            return;
        }
        if (userId == null) {
            Log.w(TAG, "loadTasksForDate - UserId is null. Cannot load tasks for date: " + date);
            if (isAdded()) Toast.makeText(getContext(), "User ID not available. Cannot load tasks.", Toast.LENGTH_SHORT).show();
            this.taskList.clear(); // Clear list if user is not available
            taskAdapter.setTaskList(this.taskList);
            updateEmptyTasksVisibility();
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate = sdf.format(date);
        Log.d(TAG, "loadTasksForDate: " + formattedDate + ", userId: " + userId);
        taskViewModel.loadTasksByDate(formattedDate, userId);
    }

    @Override
    public void onTaskStatusUpdated(int taskId, boolean completed) {
        Log.d(TAG, "onTaskStatusUpdated - TaskId: " + taskId + ", Completed: " + completed);
        if (sharedUserViewModel.getUser().getValue() != null && isAdded() && userId != null) {
            taskViewModel.changeTaskStatus(taskId, completed);
        } else {
            Log.w(TAG, "Cannot update task status - user not available or fragment not added.");
        }
    }

    private void updateEmptyTasksVisibility() {
        boolean isEmpty = this.taskList.isEmpty();
        Log.d(TAG, "updateEmptyTasksVisibility - taskList is empty: " + isEmpty);
        if (emptyTasksText != null && tasksRecyclerView != null) {
            emptyTasksText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            tasksRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }

    private int getPriorityValue(String priority) {
        if (priority == null) return 3; // Default for null priority
        switch (priority.toLowerCase()) {
            case "high": return 0;
            case "medium": return 1;
            case "low": return 2;
            default: return 3; // Default for unknown priority strings
        }
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        super.onDestroyView();
        if (taskAdapter != null) {
            taskAdapter.cleanup(); // Important for cancelling Retrofit calls in adapter
        }
        // Nullify views and adapters to help GC and prevent leaks
        datesRecyclerView = null;
        tasksRecyclerView = null;
        dateDisplay = null;
        emptyTasksText = null;
        dateAdapter = null;
        taskAdapter = null;
        // Do not nullify ViewModels here, they are managed by ViewModelProvider
    }
}