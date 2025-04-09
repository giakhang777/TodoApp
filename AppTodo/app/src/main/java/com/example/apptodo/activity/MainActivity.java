package com.example.apptodo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.apptodo.R;
import com.example.apptodo.adapter.ProgressViewPageAdapter;
import com.example.apptodo.adapter.TaskGroupAdapter;
import com.example.apptodo.model.Progress;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView rvTaskGroup;
    private TextView countGroup,countProgress;
    private TaskGroupAdapter taskGroupAdapter;
    private List<Progress> listTaskGroup;
    private ViewPager viewPager;
    private List<Progress> listInProgress;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager.getCurrentItem() == listInProgress.size() - 1) {
                viewPager.setCurrentItem(0);
            } else {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setPieChart();
        setViewPager();
        setTaskGroup();
    }
    private void setTaskGroup(){
        rvTaskGroup = findViewById(R.id.rvTaskGroup);
        countGroup = findViewById(R.id.countGroup);
        listTaskGroup = new ArrayList<>();
        listTaskGroup.add(new Progress(R.drawable.ic_account,"quang cao","lam viec 1",50));
        listTaskGroup.add(new Progress(R.drawable.ic_notification,"coffee","lam viec 2", 75));
        listTaskGroup.add(new Progress(R.drawable.ic_visibility_off,"pizza","lam viec 3", 80));
        listTaskGroup.add(new Progress(R.drawable.ic_visibility_on,"ngon","lam viec 4", 92));
        taskGroupAdapter =new TaskGroupAdapter(this, listTaskGroup);
        rvTaskGroup.setAdapter(taskGroupAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvTaskGroup.setLayoutManager(linearLayoutManager);
        countGroup.setText(listTaskGroup.size()+"");
    }
    private void setViewPager(){
        viewPager = findViewById(R.id.viewpage);
        countProgress = findViewById(R.id.countProgress);
        listInProgress = getList();
        ProgressViewPageAdapter adapter = new ProgressViewPageAdapter(listInProgress);
        viewPager.setAdapter(adapter);

        // Liên kết ViewPager với CircleIndicator

        handler.postDelayed(runnable, 3000);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        countProgress.setText(listInProgress.size()+"");
    }
    private void setPieChart(){
        PieChart pieChart = findViewById(R.id.chart);

        // Dữ liệu phần trăm
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(75, ""));
        entries.add(new PieEntry(25, ""));

        PieDataSet dataSet = new PieDataSet(entries, "Tiến độ công việc");
        dataSet.setColors(Color.WHITE, Color.TRANSPARENT);
        dataSet.setValueTextSize(16f);

        // Ẩn phần trăm trên biểu đồ
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);

        // Cài đặt Donut Chart (Lõm ở giữa)
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(75f);  // Kích thước lõm
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("75%"); // Hiển thị phần trăm ở giữa
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.WHITE);

        // Ẩn mô tả và legend (chú thích)
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);

        // Cập nhật biểu đồ
        pieChart.invalidate();
    }
    private List<Progress> getList() {
        List<Progress> list = new ArrayList<>();
        list.add(new Progress(R.drawable.ic_account,"quang cao","lam viec 1",50));
        list.add(new Progress(R.drawable.ic_notification,"coffee","lam viec 2", 75));
        list.add(new Progress(R.drawable.ic_visibility_off,"pizza","lam viec 3", 80));
        list.add(new Progress(R.drawable.ic_visibility_on,"ngon","lam viec 4", 92));

        return list;
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 3000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}