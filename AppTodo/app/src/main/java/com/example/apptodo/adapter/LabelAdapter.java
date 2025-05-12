package com.example.apptodo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.apptodo.model.response.LabelResponse;

import java.util.List;

public class LabelAdapter extends ArrayAdapter<LabelResponse> {

    private Context context;
    private List<LabelResponse> labels;

    public LabelAdapter(Context context, List<LabelResponse> labels) {
        super(context, 0, labels);
        this.context = context;
        this.labels = labels;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        LabelResponse label = getItem(position);

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(label.getTitle());  // Chỉ hiển thị title

        return convertView;
    }
}
