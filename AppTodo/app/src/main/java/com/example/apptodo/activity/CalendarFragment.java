package com.example.apptodo.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.adapter.DateAdapter;
import com.example.apptodo.model.DateItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarFragment extends Fragment implements DateAdapter.OnDateClickListener {
    private RecyclerView datesRecyclerView;
    private DateAdapter dateAdapter;
    private List<DateItem> dateItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        datesRecyclerView = view.findViewById(R.id.datesRecyclerView);
        setupDateList();
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
        // Xử lý khi chọn ngày
        dateAdapter.updateSelectedPosition(position);
        // TODO: Load tasks cho ngày được chọn
        // loadTasksForDate(dateItem.getFullDate());
    }
}