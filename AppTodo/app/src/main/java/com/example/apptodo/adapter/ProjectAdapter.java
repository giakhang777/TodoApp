package com.example.apptodo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.model.response.ProjectResponse;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.TaskGroupViewHolder> {

    private final Context context;
    private final LayoutInflater layoutInflater;
    private final List<ProjectResponse> projectList;
    private final OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(int projectId); // Pass Project ID
    }

    public ProjectAdapter(Context context, List<ProjectResponse> initialList, OnProjectClickListener listener) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.projectList = new ArrayList<>(initialList);
        this.listener = listener;
    }

    public void updateData(List<ProjectResponse> newProjects) {
        projectList.clear();
        projectList.addAll(newProjects);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.project_item, parent, false);
        return new TaskGroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskGroupViewHolder holder, int position) {
        ProjectResponse project = projectList.get(position);

        holder.nameProject.setText(project.getName());

        Drawable background = holder.itemContainer.getBackground();
        if (background instanceof GradientDrawable) {
            try {
                int color = Color.parseColor(project.getColor());
                ((GradientDrawable) background).setColor(color);
            } catch (IllegalArgumentException e) {
                ((GradientDrawable) background).setColor(Color.LTGRAY);
            }
        }

        int totalTasks = project.getTotalTasks();
        int completedTasks = project.getCompletedTasks();
        float completionPercentage = totalTasks > 0 ? ((float) completedTasks / totalTasks) * 100 : 0;

        int lavenderColor = context.getResources().getColor(R.color.lavender, null);

        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(completionPercentage, ""));
        pieEntries.add(new PieEntry(100 - completionPercentage, ""));

        PieDataSet dataSet = new PieDataSet(pieEntries, null);
        dataSet.setColors(lavenderColor, Color.GRAY);
        dataSet.setDrawValues(false);

        dataSet.setSliceSpace(5f);
        dataSet.setValueLineWidth(4f);
        dataSet.setValueLineColor(Color.WHITE);

        PieData pieData = new PieData(dataSet);
        holder.pieChart.setData(pieData);
        holder.pieChart.setDrawHoleEnabled(true);
        holder.pieChart.setHoleRadius(75f);
        holder.pieChart.setTransparentCircleRadius(55f);
        holder.pieChart.setHoleColor(Color.TRANSPARENT);

        holder.pieChart.setCenterText(String.format(Locale.getDefault(),"%.0f%%", completionPercentage));
        holder.pieChart.setCenterTextSize(16f);

        holder.pieChart.setCenterTextColor(Color.BLACK);
        holder.pieChart.getDescription().setEnabled(false);
        holder.pieChart.getLegend().setEnabled(false);

        holder.pieChart.setEntryLabelColor(Color.BLACK);

        holder.pieChart.invalidate();

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProjectClick(project.getId()); // Pass Project ID
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        TextView nameProject;
        PieChart pieChart;
        LinearLayout itemContainer;

        public TaskGroupViewHolder(View itemView) {
            super(itemView);
            nameProject = itemView.findViewById(R.id.nameProject);
            pieChart = itemView.findViewById(R.id.chart);
            itemContainer = itemView.findViewById(R.id.itemContainer);
        }
    }
}
