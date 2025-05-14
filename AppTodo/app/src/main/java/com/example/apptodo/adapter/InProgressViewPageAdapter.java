package com.example.apptodo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.apptodo.R;
import com.example.apptodo.model.Progress;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class InProgressViewPageAdapter extends PagerAdapter {
    private List<Progress> imageList;

    public InProgressViewPageAdapter(List<Progress> imageList) {
        // Sắp xếp theo priority: High -> Medium -> Low
        Collections.sort(imageList, new Comparator<Progress>() {
            @Override
            public int compare(Progress o1, Progress o2) {
                return getPriorityValue(o1.getPriority()) - getPriorityValue(o2.getPriority());
            }

            private int getPriorityValue(String priority) {
                if (priority == null) return 3;
                switch (priority.toLowerCase()) {
                    case "high": return 0;
                    case "medium": return 1;
                    case "low": return 2;
                    default: return 3;
                }
            }
        });

        this.imageList = imageList;
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.in_progress_item, container, false);

        TextView label = view.findViewById(R.id.nameLabel);
        TextView project = view.findViewById(R.id.nameProject);
        TextView task = view.findViewById(R.id.nameTask);
        TextView desc = view.findViewById(R.id.nameDesc);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        LinearLayout rootLayout = view.findViewById(R.id.linearLayput);

        Progress model = imageList.get(position);

        label.setText(model.getNameLabel());
        project.setText(model.getNameProject());
        task.setText(model.getNameTask());
        desc.setText(model.getDescription());
        progressBar.setProgress(model.getProgress());

        // Đặt màu nền theo độ ưu tiên
        String priority = model.getPriority();
        if (priority != null) {
            switch (priority.toLowerCase()) {
                case "high":
                    rootLayout.setBackgroundColor(Color.parseColor("#FFCDD2"));
                    break;
                case "medium":
                    rootLayout.setBackgroundColor(Color.parseColor("#FFE0B2"));
                    break;
                case "low":
                    rootLayout.setBackgroundColor(Color.parseColor("#BBDEFB"));
                    break;
            }
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
