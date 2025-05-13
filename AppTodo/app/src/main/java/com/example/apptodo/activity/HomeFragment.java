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
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.apptodo.R;
import com.example.apptodo.adapter.InProgressViewPageAdapter;
import com.example.apptodo.adapter.ProjectAdapter;
import com.example.apptodo.model.Progress;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.ProjectViewModel;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.time.LocalDate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private RecyclerView rvProject;
    private TextView countGroup, countProgress, profileNameTextView, tvNoTaskToday;
    private ProjectAdapter projectAdapter;
    private List<ProjectResponse> listProject;
    private ViewPager viewPager;
    private List<Progress> listInProgress;
    private ImageView profileImageView;
    private Handler handler = new Handler();
    private SharedUserViewModel userViewModel;
    private TaskViewModel taskViewModel;
    private ProjectViewModel projectViewModel;
    private Button btnViewTask;


    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && listInProgress != null && !listInProgress.isEmpty()) {
                int currentItem = viewPager.getCurrentItem();
                viewPager.setCurrentItem((currentItem + 1) % listInProgress.size());
            }
        }
    };

    public HomeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        taskViewModel = new ViewModelProvider(this).get(TaskViewModel.class);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        profileNameTextView = view.findViewById(R.id.profileNameTextView);
        profileImageView = view.findViewById(R.id.imageUserHome);
        tvNoTaskToday = view.findViewById(R.id.tvNoTaskToday);
        btnViewTask = view.findViewById(R.id.btnViewTask);

        btnViewTask.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, new CalendarFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getId() != null) {
                profileNameTextView.setText(user.getUsername());
                String avatarUrl = user.getAvatar();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.defaulter_user)
                            .into(profileImageView);
                }
                getListTaskToday(user.getId());
                projectViewModel.fetchProjects(user.getId());
            } else {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
            }
        });

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), new Observer<List<TaskResponse>>() {
            @Override
            public void onChanged(List<TaskResponse> tasks) {
                listInProgress.clear();
                if (tasks != null) {
                    for (TaskResponse task : tasks) {
                        if (!Boolean.TRUE.equals(task.getCompleted())) {
                            listInProgress.add(new Progress(
                                    task.getTitle(),
                                    task.getProject(),
                                    task.getLabel(),
                                    task.getPriority(),
                                    task.getDescription(),
                                    60
                            ));
                        }
                    }
                }

                if (listInProgress.isEmpty()) {
                    tvNoTaskToday.setVisibility(View.VISIBLE);
                    viewPager.setVisibility(View.GONE);
                } else {
                    tvNoTaskToday.setVisibility(View.GONE);
                    viewPager.setVisibility(View.VISIBLE);
                }
                InProgressViewPageAdapter adapter = new InProgressViewPageAdapter(listInProgress);
                viewPager.setAdapter(adapter);
                countProgress.setText(String.valueOf(listInProgress.size()));

                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            }
        });

        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String errorMessage) {
                if (errorMessage != null && !errorMessage.isEmpty() && listInProgress.isEmpty()) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        projectViewModel.getProjectList().observe(getViewLifecycleOwner(), projects -> {
            if (projects != null) {
                listProject.clear();
                listProject.addAll(projects);
                if (projectAdapter != null) {
                    projectAdapter.updateData(listProject);
                }
                countGroup.setText(String.valueOf(listProject.size()));
            }
        });


        setPieChart(view);
        setViewPager(view);
        setProject(view);
    }

    private void getListTaskToday(Integer userId) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        taskViewModel.loadTasksByDate(today, userId);
    }

    private void setProject(View view) {
        rvProject = view.findViewById(R.id.rvTaskGroup);
        countGroup = view.findViewById(R.id.countGroup);
        listProject = new ArrayList<>();

        projectAdapter = new ProjectAdapter(getContext(), listProject);
        rvProject.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProject.setAdapter(projectAdapter);
    }

    private void setViewPager(View view) {
        viewPager = view.findViewById(R.id.viewpage);
        countProgress = view.findViewById(R.id.countProgress);
        listInProgress = new ArrayList<>();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
            @Override public void onPageSelected(int position) {
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            }
            @Override public void onPageScrollStateChanged(int state) {}
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
