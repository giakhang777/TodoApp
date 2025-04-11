package com.example.apptodo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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

public class HomeFragment extends Fragment {

    private RecyclerView rvTaskGroup;
    private TextView countGroup, countProgress;
    private TaskGroupAdapter taskGroupAdapter;
    private List<Progress> listTaskGroup;
    private ViewPager viewPager;
    private List<Progress> listInProgress;
    private Handler handler = new Handler();

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && listInProgress != null) {
                int currentItem = viewPager.getCurrentItem();
                viewPager.setCurrentItem((currentItem + 1) % listInProgress.size());
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setPieChart(view);
        setViewPager(view);
        setTaskGroup(view);
    }

    private void setTaskGroup(View view){
        rvTaskGroup = view.findViewById(R.id.rvTaskGroup);
        countGroup = view.findViewById(R.id.countGroup);
        listTaskGroup = new ArrayList<>();
        listTaskGroup.add(new Progress(R.drawable.ic_account,"quang cao","lam viec 1",50));
        listTaskGroup.add(new Progress(R.drawable.ic_notification,"coffee","lam viec 2", 75));
        listTaskGroup.add(new Progress(R.drawable.ic_visibility_off,"pizza","lam viec 3", 80));
        listTaskGroup.add(new Progress(R.drawable.ic_visibility_on,"ngon","lam viec 4", 92));
        taskGroupAdapter = new TaskGroupAdapter(getContext(), listTaskGroup);
        rvTaskGroup.setAdapter(taskGroupAdapter);
        rvTaskGroup.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        countGroup.setText(String.valueOf(listTaskGroup.size()));
    }

    private void setViewPager(View view){
        viewPager = view.findViewById(R.id.viewpage);
        countProgress = view.findViewById(R.id.countProgress);
        listInProgress = getList();
        ProgressViewPageAdapter adapter = new ProgressViewPageAdapter(listInProgress);
        viewPager.setAdapter(adapter);

        handler.postDelayed(runnable, 3000);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageSelected(int position) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            }
            @Override public void onPageScrollStateChanged(int state) {}
        });

        countProgress.setText(String.valueOf(listInProgress.size()));
    }

    private void setPieChart(View view){
        PieChart pieChart = view.findViewById(R.id.chart);
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(75, ""));
        entries.add(new PieEntry(25, ""));

        PieDataSet dataSet = new PieDataSet(entries, "Tiến độ công việc");
        dataSet.setColors(Color.WHITE, Color.TRANSPARENT);
        dataSet.setValueTextSize(16f);
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(75f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterText("75%");
        pieChart.setCenterTextSize(16f);
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
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
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 3000);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
