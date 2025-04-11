package com.example.apptodo.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.example.apptodo.R;
import com.example.apptodo.model.DateItem;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.DateViewHolder> {
    private final List<DateItem> dateItems;
    private int selectedPosition = 0;
    private final OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(DateItem dateItem, int position);
    }

    public DateAdapter(List<DateItem> dateItems, OnDateClickListener listener) {
        this.dateItems = dateItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        DateItem item = dateItems.get(position);
        holder.bind(item, position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            int clickedPosition = holder.getAdapterPosition();
            if (clickedPosition != RecyclerView.NO_POSITION) {
                updateSelectedPosition(clickedPosition);
                listener.onDateClick(dateItems.get(clickedPosition), clickedPosition);
            }
        });
    }

    public void updateSelectedPosition(int newPosition) {
        int previousSelected = selectedPosition;
        selectedPosition = newPosition;
        notifyItemChanged(previousSelected);
        notifyItemChanged(selectedPosition);
    }

    @Override
    public int getItemCount() {
        return dateItems.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        private final TextView monthTextView;
        private final TextView dayTextView;
        private final TextView dayOfWeekTextView;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            monthTextView = itemView.findViewById(R.id.monthTextView);
            dayTextView = itemView.findViewById(R.id.dayTextView);
            dayOfWeekTextView = itemView.findViewById(R.id.dayOfWeekTextView);
        }

        public void bind(DateItem item, boolean isSelected) {
            monthTextView.setText(item.getMonth());
            dayTextView.setText(item.getDay());
            dayOfWeekTextView.setText(item.getDayOfWeek());

            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.date_item_selected_bg);
                monthTextView.setTextColor(Color.WHITE);
                dayTextView.setTextColor(Color.WHITE);
                dayOfWeekTextView.setTextColor(Color.WHITE);
            } else {
                itemView.setBackgroundResource(R.drawable.date_item_bg);
                monthTextView.setTextColor(Color.BLACK);
                dayTextView.setTextColor(Color.BLACK);
                dayOfWeekTextView.setTextColor(Color.BLACK);
            }
        }
    }
}