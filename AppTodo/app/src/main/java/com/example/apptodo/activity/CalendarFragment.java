package com.example.apptodo.activity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.apptodo.R;
import com.example.apptodo.adapter.DateAdapter;
import com.example.apptodo.model.DateItem;

public class CalendarFragment extends Fragment implements DateAdapter.OnDateClickListener {

    private RecyclerView datesRecyclerView;
    private DateAdapter dateAdapter;
    private TextView dateDisplay;
    private List<DateItem> dateItems = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);
        datesRecyclerView = view.findViewById(R.id.datesRecyclerView);
        dateDisplay = view.findViewById(R.id.dateDisplay);

        // Thiết lập sự kiện click cho TextView để mở DatePickerDialog
        dateDisplay.setOnClickListener(v -> showDatePickerDialog());

        setupDateList();  // Thiết lập danh sách ngày
        return view;
    }

    // Tạo danh sách ngày từ hôm nay
    private void setupDateList() {
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        // Tạo danh sách 100 ngày bắt đầu từ hôm nay
        for (int i = 0; i < 100; i++) {
            dateItems.add(new DateItem(calendar.getTime()));
            calendar.add(Calendar.DATE, 1);
        }

        // Cấu hình RecyclerView với LinearLayoutManager theo chiều ngang
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        datesRecyclerView.setLayoutManager(layoutManager);

        dateAdapter = new DateAdapter(dateItems, this);
        datesRecyclerView.setAdapter(dateAdapter);

        // Cuộn đến ngày hôm nay
        scrollToToday(today);

        // Hiển thị ngày hôm nay trong TextView
        updateDateDisplay(today);
    }

    // Cuộn đến ngày hôm nay trong RecyclerView
    private void scrollToToday(Date today) {
        for (int i = 0; i < dateItems.size(); i++) {
            if (dateItems.get(i).getFullDate().equals(new DateItem(today).getFullDate())) {
                dateAdapter.updateSelectedPosition(i);
                datesRecyclerView.scrollToPosition(i);  // Hoặc dùng smoothScrollToPosition
                break;
            }
        }
    }

    // Hiển thị DatePickerDialog để người dùng chọn ngày
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();

        // Đặt ngày hôm nay là ngày tối thiểu người dùng có thể chọn
        long todayInMillis = calendar.getTimeInMillis();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, monthOfYear, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, monthOfYear, dayOfMonth);
                    updateDateDisplay(selectedDate.getTime());  // Cập nhật TextView

                    // Cuộn đến ngày đã chọn trong RecyclerView
                    scrollToSelectedDate(selectedDate.getTime());
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Đặt ngày hôm nay làm ngày tối thiểu
        datePickerDialog.getDatePicker().setMinDate(todayInMillis);

        datePickerDialog.show();
    }


    // Cuộn đến ngày được chọn trong RecyclerView
    private void scrollToSelectedDate(Date selectedDate) {
        Calendar selectedCalendar = Calendar.getInstance();
        selectedCalendar.setTime(selectedDate);

        for (int i = 0; i < dateItems.size(); i++) {
            Calendar itemCalendar = Calendar.getInstance();
            itemCalendar.setTime(dateItems.get(i).getFullDate());

            // So sánh ngày, tháng và năm (bỏ qua giờ, phút, giây)
            if (selectedCalendar.get(Calendar.YEAR) == itemCalendar.get(Calendar.YEAR) &&
                    selectedCalendar.get(Calendar.MONTH) == itemCalendar.get(Calendar.MONTH) &&
                    selectedCalendar.get(Calendar.DAY_OF_MONTH) == itemCalendar.get(Calendar.DAY_OF_MONTH)) {

                dateAdapter.updateSelectedPosition(i);  // Cập nhật vị trí đã chọn
                datesRecyclerView.smoothScrollToPosition(i);  // Cuộn mượt mà
                break;
            }
        }
    }

    // Cập nhật TextView để hiển thị tháng và năm
    private void updateDateDisplay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        String monthName = getMonthName(calendar.get(Calendar.MONTH));
        int year = calendar.get(Calendar.YEAR);
        String dateText = monthName + " " + year;

        dateDisplay.setText(dateText);  // Cập nhật TextView
    }

    // Lấy tên tháng từ chỉ số tháng
    private String getMonthName(int month) {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        return months[month];
    }

    // Xử lý khi chọn ngày từ RecyclerView
    @Override
    public void onDateClick(DateItem dateItem, int position) {
        dateAdapter.updateSelectedPosition(position);  // Cập nhật vị trí đã chọn
        updateDateDisplay(dateItem.getFullDate());  // Cập nhật TextView với ngày chọn

        // TODO: Load tasks cho ngày được chọn
        // loadTasksForDate(dateItem.getFullDate());
    }
}
