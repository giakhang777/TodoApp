package com.example.apptodo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.apptodo.R;
import com.example.apptodo.adapter.InProgressViewPageAdapter;
import com.example.apptodo.adapter.ProjectAdapter;
import com.example.apptodo.api.TaskService;
import com.example.apptodo.model.Progress;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView rvProject;
    private TextView countGroup, countProgress, profileNameTextView;
    private ProjectAdapter ProjectAdapter;
    private List<Progress> listProject;
    private ViewPager viewPager;
    private List<Progress> listInProgress;
    private ImageView profileImageView;
    private Handler handler = new Handler();
    private SharedUserViewModel userViewModel;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && listInProgress != null && !listInProgress.isEmpty()) {
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

        userViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        profileNameTextView = view.findViewById(R.id.profileNameTextView);
        profileImageView = view.findViewById(R.id.imageUserHome);

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                profileNameTextView.setText(user.getUsername());
                String avatarUrl = user.getAvatar();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.defaulter_user)
                            .into(profileImageView);
                }
            }
        });

        setPieChart(view);
        setViewPager(view);
        setProject(view);
    }

    private void setProject(View view) {
        rvProject = view.findViewById(R.id.rvTaskGroup);
        countGroup = view.findViewById(R.id.countGroup);
        listProject = new ArrayList<>();
        listProject.add(new Progress("quang cao","quang cao", "quang cao", "lam viec 1","lam viec 1",  50));
        listProject.add(new Progress("quang cao","quang cao", "coffee", "lam viec 2","lam viec 1",  75));
        listProject.add(new Progress("quang cao","quang cao", "pizza", "lam viec 3","lam viec 1",  80));
        listProject.add(new Progress("quang cao","quang cao", "ngon", "lam viec 4","lam viec 1",  92));
        listProject.add(new Progress("quang cao","quang cao", "ngon", "lam viec 4","lam viec 1",  92));
        listProject.add(new Progress("quang cao","quang cao", "ngon", "lam viec 4","lam viec 1",  92));
        listProject.add(new Progress("quang cao","quang cao", "ngon", "lam viec 4","lam viec 1",  92));


        ProjectAdapter = new ProjectAdapter(getContext(), listProject);
        rvProject.setAdapter(ProjectAdapter);
        rvProject.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        countGroup.setText(String.valueOf(listProject.size()));
    }

    private void setViewPager(View view) {
        viewPager = view.findViewById(R.id.viewpage);
        countProgress = view.findViewById(R.id.countProgress);
        listInProgress = new ArrayList<>();
        getListTaskFromApi();
        handler.postDelayed(runnable, 3000);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageSelected(int position) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            }
            @Override public void onPageScrollStateChanged(int state) {}
        });
    }

    private void getListTaskFromApi() {
//        String today = LocalDate.now().toString();
        String today = "2025-05-17";

        TaskService taskService = RetrofitClient.getRetrofit().create(TaskService.class);

        taskService.getTasksByDate(today).enqueue(new Callback<List<TaskResponse>>() {
            @Override
            public void onResponse(Call<List<TaskResponse>> call, Response<List<TaskResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TaskResponse> tasks = response.body();
                    listInProgress.clear();

                    // Lọc task chưa hoàn thành
                    List<TaskResponse> unfinishedTasks = new ArrayList<>();
                    for (TaskResponse task : tasks) {
                        if (!task.getCompleted()) { // Giả sử phương thức isCompleted() trả về trạng thái
                            unfinishedTasks.add(task);
                        }
                    }

                    for (int i = 0; i < unfinishedTasks.size(); i++) {
                        TaskResponse task = unfinishedTasks.get(i);
                        listInProgress.add(new Progress(
                                task.getTitle(),
                                task.getProject(),
                                task.getLabel(),
                                task.getPriority(),
                                task.getDescription(),
                                60
                        ));
                    }

                    InProgressViewPageAdapter adapter = new InProgressViewPageAdapter(listInProgress);
                    viewPager.setAdapter(adapter);
                    countProgress.setText(String.valueOf(listInProgress.size()));
                } else {
                    Toast.makeText(getContext(), "Không có task hôm nay", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TaskResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi gọi API", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setPieChart(View view) {
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
