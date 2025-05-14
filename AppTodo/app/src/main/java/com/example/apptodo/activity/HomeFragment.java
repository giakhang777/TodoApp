package com.example.apptodo.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.apptodo.R;
import com.example.apptodo.adapter.InProgressViewPageAdapter;
import com.example.apptodo.adapter.ProjectAdapter;
import com.example.apptodo.model.Progress;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.model.response.TaskResponse;
import com.example.apptodo.viewmodel.ProjectViewModel;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.example.apptodo.viewmodel.TaskViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class HomeFragment extends Fragment implements ProjectAdapter.OnProjectClickListener {

    private RecyclerView rvProject;
    private TextView countGroup, countProgress, profileNameTextView, tvNoTaskToday;
    private ProjectAdapter projectAdapter;
    private List<ProjectResponse> listProject;
    private ViewPager viewPager;
    private List<Progress> listInProgress;
    private ImageView profileImageView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private SharedUserViewModel userViewModel;
    private TaskViewModel taskViewModel;
    private ProjectViewModel projectViewModel;
    private Button btnViewTask;
    private PieChart pieChart;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (viewPager != null && listInProgress != null && !listInProgress.isEmpty()) {
                int currentItem = viewPager.getCurrentItem();
                if (listInProgress.size() > 0) {
                    viewPager.setCurrentItem((currentItem + 1) % listInProgress.size());
                }
            }
            handler.postDelayed(this, 3000);
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
        taskViewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        profileNameTextView = view.findViewById(R.id.profileNameTextView);
        profileImageView = view.findViewById(R.id.imageUserHome);
        tvNoTaskToday = view.findViewById(R.id.tvNoTaskToday);
        btnViewTask = view.findViewById(R.id.btnViewTask);
        pieChart = view.findViewById(R.id.chart);
        setupPieChart();

        btnViewTask.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToCalendarFragment();
            }
        });

        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getId() != null) {
                profileNameTextView.setText(user.getUsername());
                String avatarUrl = user.getAvatar();
                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                    Glide.with(requireContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.defaulter_user)
                            .error(R.drawable.defaulter_user)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.defaulter_user);
                }
                getListTaskToday(user.getId());
                projectViewModel.fetchProjects(user.getId());
            } else {
                profileNameTextView.setText("Guest");
                profileImageView.setImageResource(R.drawable.defaulter_user);
                if(listInProgress != null) listInProgress.clear();
                if (viewPager != null && viewPager.getAdapter() != null) {
                    viewPager.getAdapter().notifyDataSetChanged();
                }
                if(countProgress != null) countProgress.setText("0");
                if(tvNoTaskToday != null) tvNoTaskToday.setVisibility(View.VISIBLE);
                if(viewPager != null) viewPager.setVisibility(View.GONE);
                updateOverallProgressPieChart(0);


                if(listProject != null) listProject.clear();
                if(projectAdapter != null) projectAdapter.updateData(listProject != null ? listProject : new ArrayList<>());
                if(countGroup != null) countGroup.setText("0");
                Toast.makeText(getContext(), "User not found or not logged in.", Toast.LENGTH_SHORT).show();
            }
        });

        taskViewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            if (listInProgress == null) listInProgress = new ArrayList<>();
            listInProgress.clear();
            float totalCompletionPercentageSum = 0;
            int totalTasksCount = 0;

            if (tasks != null) {
                for (TaskResponse task : tasks) {
                    if (!Boolean.TRUE.equals(task.getCompleted())) {
                        listInProgress.add(new Progress(
                                task.getTitle(),
                                task.getProject(),
                                task.getLabel(),
                                task.getPriority(),
                                task.getDescription(),
                                getProgress(task)
                        ));
                    }
                    totalCompletionPercentageSum += getProgress(task);
                    totalTasksCount++;
                }
            }

            float averageCompletionPercentage = (totalTasksCount > 0) ? (totalCompletionPercentageSum / totalTasksCount) : 0;
            updateOverallProgressPieChart(averageCompletionPercentage);

            if (listInProgress.isEmpty()) {
                if(tvNoTaskToday != null) tvNoTaskToday.setVisibility(View.VISIBLE);
                if(viewPager != null) viewPager.setVisibility(View.GONE);
            } else {
                if(tvNoTaskToday != null) tvNoTaskToday.setVisibility(View.GONE);
                if(viewPager != null) viewPager.setVisibility(View.VISIBLE);
            }

            if(viewPager != null && getContext() != null) {
                InProgressViewPageAdapter adapter = new InProgressViewPageAdapter(new ArrayList<>(listInProgress));
                viewPager.setAdapter(adapter);
            }
            if(countProgress != null) countProgress.setText(String.valueOf(listInProgress.size()));
        });


        taskViewModel.getSingleTask().observe(getViewLifecycleOwner(), updatedTask -> {
            if (updatedTask != null && userViewModel.getUser().getValue() != null && userViewModel.getUser().getValue().getId() != null) {
                getListTaskToday(userViewModel.getUser().getValue().getId());
                projectViewModel.fetchProjects(userViewModel.getUser().getValue().getId());
            }
        });


        taskViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                if (listInProgress != null && listInProgress.isEmpty()) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });

        projectViewModel.getProjectList().observe(getViewLifecycleOwner(), projects -> {
            if (listProject == null) listProject = new ArrayList<>();
            listProject.clear();
            if (projects != null) {
                listProject.addAll(projects);
            }
            if (projectAdapter != null) {
                projectAdapter.updateData(listProject);
            }
            if(countGroup != null) countGroup.setText(String.valueOf(listProject.size()));
        });

        setViewPager(view);
        setProject(view);
    }

    private int getProgress(TaskResponse task){
        if (task == null) return 0;
        int completedSubTasks = task.getCompletedSubTasks();
        int totalSubTasks = task.getTotalSubTasks();

        if (totalSubTasks == 0) {
            return Boolean.TRUE.equals(task.getCompleted()) ? 100 : 0;
        }
        return (completedSubTasks * 100) / totalSubTasks;
    }

    private void updateOverallProgressPieChart(float completionPercentage) {
        if (pieChart == null) return;
        if (Float.isNaN(completionPercentage) || Float.isInfinite(completionPercentage)) {
            completionPercentage = 0;
        }
        completionPercentage = Math.max(0f, Math.min(100f, completionPercentage));

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(completionPercentage, ""));
        entries.add(new PieEntry(Math.max(0f, 100f - completionPercentage), ""));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.WHITE, Color.TRANSPARENT);
        dataSet.setDrawValues(false);
        dataSet.setSliceSpace(0f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setCenterText(String.format(Locale.getDefault(), "%.0f%%", completionPercentage));
        pieChart.invalidate();
    }

    private void setupPieChart() {
        if (pieChart == null) return;
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleRadius(85f);
        pieChart.setTransparentCircleRadius(85f);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setTouchEnabled(false);
        pieChart.setUsePercentValues(false);
        updateOverallProgressPieChart(0);
    }

    private void getListTaskToday(Integer userId) {
        if (userId == null) {
            if (taskViewModel != null) {
                taskViewModel.clearTasksLiveData();
            }
            return;
        }
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());
        if (taskViewModel != null) {
            taskViewModel.loadTasksByDate(today, userId);
        }
    }

    private void setProject(View view) {
        rvProject = view.findViewById(R.id.rvTaskGroup);
        countGroup = view.findViewById(R.id.countGroup);
        listProject = new ArrayList<>();
        if (getContext() != null) {
            projectAdapter = new ProjectAdapter(getContext(), listProject, this);
            rvProject.setLayoutManager(new LinearLayoutManager(getContext()));
            rvProject.setAdapter(projectAdapter);
        }
    }

    private void setViewPager(View view) {
        viewPager = view.findViewById(R.id.viewpage);
        countProgress = view.findViewById(R.id.countProgress);
        listInProgress = new ArrayList<>();

        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
                @Override
                public void onPageSelected(int position) {
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 3000);
                }
                @Override
                public void onPageScrollStateChanged(int state) {}
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 3000);
        UserResponse currentUser = userViewModel != null ? userViewModel.getUser().getValue() : null;
        if (currentUser != null && currentUser.getId() != null) {
            getListTaskToday(currentUser.getId());
            if (projectViewModel != null) projectViewModel.fetchProjects(currentUser.getId());
        } else {
            getListTaskToday(null);
            if (projectViewModel != null ) {
                projectViewModel.clearProjectsLiveData();
                if (projectAdapter != null) {
                    projectAdapter.updateData(new ArrayList<>());
                }
                if (countGroup != null) {
                    countGroup.setText("0");
                }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onProjectClick(int projectId) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToProjectFragment(projectId);
        }
    }
}