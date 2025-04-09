package com.example.apptodo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.apptodo.R;
import com.example.apptodo.model.Progress;

import java.util.List;

public class ProgressViewPageAdapter extends PagerAdapter {
    private List<Progress> imageList;

    public ProgressViewPageAdapter(List<Progress> imageList) {
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
        ImageView imageView = view.findViewById(R.id.imgLogo);
        TextView desc1 = view.findViewById(R.id.nameCategory);
        TextView desc2 = view.findViewById(R.id.nameTask);
        ProgressBar progressBar = view.findViewById(R.id.progressBar);
        Progress imageModel = imageList.get(position);

        imageView.setImageResource(imageModel.getImageResId());
        desc1.setText(imageModel.getDesc1().trim());
        desc2.setText(imageModel.getDesc2().trim());
        progressBar.setProgress(imageModel.getProgress());

        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
