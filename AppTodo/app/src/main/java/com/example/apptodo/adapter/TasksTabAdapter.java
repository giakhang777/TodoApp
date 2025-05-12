package com.example.apptodo.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TasksTabAdapter extends FragmentStateAdapter {
    public TasksTabAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ExpiredTasksFragment();
        } else {
            return new CompletedTasksFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 tabs: Expired and Completed
    }
}