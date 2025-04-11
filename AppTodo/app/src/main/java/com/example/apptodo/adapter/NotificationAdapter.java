package com.example.apptodo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.apptodo.R;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<String> notificationList;
    private OnNotificationClickListener listener;

    // Thêm Listener để xử lý click
    public NotificationAdapter(List<String> notificationList, OnNotificationClickListener listener) {
        this.notificationList = notificationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String notification = notificationList.get(position);
        holder.tvNotification.setText(notification);

        // Xử lý click vào từng thông báo
        holder.itemView.setOnClickListener(v -> {
            // Gọi listener để xử lý click
            if (listener != null) {
                listener.onNotificationClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNotification;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNotification = itemView.findViewById(R.id.tv_notification);
        }
    }

    // Interface cho sự kiện click vào thông báo
    public interface OnNotificationClickListener {
        void onNotificationClick(int position);
    }
}

