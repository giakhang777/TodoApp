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

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.TaskGroupViewHolder> {

    private final Context context;
    private final LayoutInflater layoutInflater;
    private final List<ProjectResponse> projectList;

    public ProjectAdapter(Context context, List<ProjectResponse> initialList) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.projectList = new ArrayList<>(initialList);
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

        // Đổi màu nền giữ lại drawable (bo góc, viền...)
        Drawable background = holder.itemContainer.getBackground();
        if (background instanceof GradientDrawable) {
            try {
                int color = Color.parseColor(project.getColor());
                ((GradientDrawable) background).setColor(color);
            } catch (IllegalArgumentException e) {
                ((GradientDrawable) background).setColor(Color.LTGRAY);
            }
        }

        // Tính toán tỷ lệ hoàn thành
        int totalTasks = project.getTotalTasks();
        int completedTasks = project.getCompletedTasks();
        float completionPercentage = totalTasks > 0 ? ((float) completedTasks / totalTasks) * 100 : 0;

        // Lấy màu từ resources (lavender)
        int lavenderColor = context.getResources().getColor(R.color.lavender, null);  // Lấy màu từ colors.xml

        // Setup PieChart với dữ liệu hoàn thành
        List<PieEntry> pieEntries = new ArrayList<>();
        pieEntries.add(new PieEntry(completionPercentage, "Done"));
        pieEntries.add(new PieEntry(100 - completionPercentage, "Remaining"));

        PieDataSet dataSet = new PieDataSet(pieEntries, null);
        dataSet.setColors(lavenderColor, Color.GRAY);  // Sử dụng màu lavender cho phần đã hoàn thành và màu xám cho phần còn lại
        dataSet.setDrawValues(false);  // Không hiển thị giá trị trên biểu đồ

        // Thêm đường viền nổi bật cho mỗi lát pie
        dataSet.setSliceSpace(5f);  // Tạo khoảng cách giữa các lát để đường viền được rõ hơn
        dataSet.setValueLineWidth(4f);  // Độ dày của đường viền trong các mảng
        dataSet.setValueLineColor(Color.WHITE);  // Màu đường viền của các lát pie

        PieData pieData = new PieData(dataSet);
        holder.pieChart.setData(pieData);
        holder.pieChart.setDrawHoleEnabled(true);  // Lỗ ở giữa
        holder.pieChart.setHoleRadius(75f);  // Kích thước lỗ giữa
        holder.pieChart.setTransparentCircleRadius(55f);  // Kích thước vòng tròn trong suốt
        holder.pieChart.setHoleColor(Color.TRANSPARENT);  // Màu lỗ giữa

        // Hiển thị tỷ lệ hoàn thành
        holder.pieChart.setCenterText(String.format("%.0f%%", completionPercentage));
        holder.pieChart.setCenterTextSize(16f);

        // Set màu chữ là màu đen
        holder.pieChart.setCenterTextColor(Color.BLACK);  // Đổi màu chữ phần trăm thành màu đen
        holder.pieChart.getDescription().setEnabled(false);  // Tắt mô tả của biểu đồ
        holder.pieChart.getLegend().setEnabled(false);  // Tắt biểu tượng legend

        // Set màu nhãn (phần trăm) của PieChart là màu đen
        holder.pieChart.setEntryLabelColor(Color.BLACK);  // Màu chữ nhãn là màu đen

        holder.pieChart.invalidate();  // Cập nhật PieChart
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
            itemContainer = itemView.findViewById(R.id.itemContainer); // View cần đổi màu
        }
    }
}
