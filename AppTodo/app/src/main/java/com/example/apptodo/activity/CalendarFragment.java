package com.example.apptodo.activity;

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
import com.example.apptodo.model.DateItem;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.api.TaskService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalendarFragment extends Fragment implements DateAdapter.OnDateClickListener {

    private RecyclerView datesRecyclerView;
    private RecyclerView tasksRecyclerView;
    private DateAdapter dateAdapter;
    private TaskAdapter taskAdapter;
    private List<DateItem> dateItems = new ArrayList<>();
    private List<TaskResponse> taskList = new ArrayList<>();
    private TextView emptyTasksText;
    private TaskService taskService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        datesRecyclerView = view.findViewById(R.id.datesRecyclerView);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        emptyTasksText = view.findViewById(R.id.emptyTasksText);

        // Khởi tạo TaskService từ RetrofitClient
        taskService = RetrofitClient.getTaskService();

        setupDateList();
        setupTaskList();

        return view;
    }

    private void setupDateList() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        // Tạo danh sách 100 ngày từ hôm nay
        for (int i = 0; i < 100; i++) {
            dateItems.add(new DateItem(calendar.getTime()));
            calendar.add(Calendar.DATE, 1);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(layoutManager);

        dateAdapter = new DateAdapter(dateItems, this);
        datesRecyclerView.setAdapter(dateAdapter);

        // Cuộn đến ngày hôm nay
        scrollToToday(today);

        // Load task của ngày hôm nay mặc định
        loadTasksForDate(new DateItem(today).getFullDate());
    }

    private void setupTaskList() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(requireContext(), taskList);
        tasksRecyclerView.setAdapter(taskAdapter);
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

    @Override
    public void onDateClick(DateItem dateItem, int position) {
        dateAdapter.updateSelectedPosition(position);
        loadTasksForDate(dateItem.getFullDate());
    }

    private void loadTasksForDate(String selectedDate) {
        taskService.getTasksByDate(selectedDate).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    taskList.clear();
                    taskList.addAll(response.body());
                    taskAdapter.notifyDataSetChanged();
                    emptyTasksText.setVisibility(taskList.isEmpty() ? View.VISIBLE : View.GONE);
                } else {
                    Toast.makeText(getContext(), "Không tải được công việc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng hoặc máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
