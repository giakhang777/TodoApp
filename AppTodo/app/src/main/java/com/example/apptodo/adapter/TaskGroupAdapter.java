package com.example.apptodo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;
import com.example.apptodo.model.Progress;
import com.example.apptodo.model.TaskGroup;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class TaskGroupAdapter extends RecyclerView.Adapter<TaskGroupAdapter.TaskGroupViewHolder>{
    private List<Progress> list;
    private Context context;
    private LayoutInflater layout;

    public TaskGroupAdapter(Context context,List<Progress> list) {
        this.list = list;
        this.context = context;
        this.layout = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public TaskGroupAdapter.TaskGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = layout.inflate(R.layout.task_group_item, parent, false);
        return new TaskGroupViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskGroupAdapter.TaskGroupViewHolder holder, int position) {
        Progress progress = list.get(position);
        holder.imgLogo.setImageResource(progress.getImageResId());
        holder.desc1.setText(progress.getDesc1());

        holder.desc2.setText(progress.getDesc2());
        // Dữ liệu phần trăm
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(progress.getProgress(), ""));
        entries.add(new PieEntry(100-progress.getProgress(), ""));

        PieDataSet dataSet = new PieDataSet(entries, "Tiến độ công việc");
        dataSet.setColors(Color.BLUE, Color.TRANSPARENT);
        dataSet.setValueTextSize(16f);

        // Ẩn phần trăm trên biểu đồ
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        holder.pieChart.setData(data);

        // Cài đặt Donut Chart (Lõm ở giữa)
        holder.pieChart.setDrawHoleEnabled(true);
        holder.pieChart.setHoleRadius(75f);  // Kích thước lõm
        holder.pieChart.setTransparentCircleRadius(55f);
        holder.pieChart.setHoleColor(Color.TRANSPARENT);
        holder.pieChart.setCenterText("75%"); // Hiển thị phần trăm ở giữa
        holder.pieChart.setCenterTextSize(16f);
        holder.pieChart.setCenterTextColor(Color.WHITE);

        // Ẩn mô tả và legend (chú thích)
        holder.pieChart.getDescription().setEnabled(false);
        holder.pieChart.getLegend().setEnabled(false);

        // Cập nhật biểu đồ
        holder.pieChart.invalidate();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    class TaskGroupViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgLogo;
        private TextView desc1;
        private TextView desc2;
        private PieChart pieChart;

        public TaskGroupViewHolder(View itemView) {
            super(itemView);
            imgLogo =(ImageView) itemView.findViewById(R.id.imgLogo);
            desc1 =(TextView) itemView.findViewById(R.id.nameCategory);
            desc2 =(TextView) itemView.findViewById(R.id.countTask);
            pieChart =(PieChart) itemView.findViewById(R.id.chart);
        }
    }
}
