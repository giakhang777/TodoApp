package com.example.apptodo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.TaskGroupViewHolder> {

    private final Context context;
    private final LayoutInflater layoutInflater;
    private final List<ProjectResponse> projectList;

    public ProjectAdapter(Context context, List<ProjectResponse> initialList) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.projectList = new ArrayList<>(initialList); // Bảo vệ dữ liệu nội bộ
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

        try {
            holder.nameProject.setTextColor(Color.parseColor(project.getColor()));
        } catch (IllegalArgumentException e) {
            holder.nameProject.setTextColor(Color.BLACK); // fallback màu mặc định
        }

        // Chart hiển thị tạm thời 0% tiến độ
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(0f, "Done"));
        pieEntries.add(new PieEntry(100f, "Remain"));

        PieDataSet dataSet = new PieDataSet(pieEntries, null);
        dataSet.setColors(Color.BLUE, Color.TRANSPARENT);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        holder.pieChart.setData(pieData);
        holder.pieChart.setDrawHoleEnabled(true);
        holder.pieChart.setHoleRadius(75f);
        holder.pieChart.setTransparentCircleRadius(55f);
        holder.pieChart.setHoleColor(Color.TRANSPARENT);
        holder.pieChart.setCenterText("0%");
        holder.pieChart.setCenterTextSize(16f);
        holder.pieChart.setCenterTextColor(Color.WHITE);
        holder.pieChart.getDescription().setEnabled(false);
        holder.pieChart.getLegend().setEnabled(false);
        holder.pieChart.invalidate();
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }

    static class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        TextView nameProject;
        PieChart pieChart;

        public TaskGroupViewHolder(View itemView) {
            super(itemView);
            nameProject = itemView.findViewById(R.id.nameProject);
            pieChart = itemView.findViewById(R.id.chart);
        }
    }
}
