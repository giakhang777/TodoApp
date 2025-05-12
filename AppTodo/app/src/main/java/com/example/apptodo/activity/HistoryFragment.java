package com.example.apptodo.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.apptodo.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HistoryFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private com.example.apptodo.activity.TasksTabAdapter tabAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        tabAdapter = new com.example.apptodo.activity.TasksTabAdapter(this);
        viewPager.setAdapter(tabAdapter);

        // Thêm listener để đồng bộ màu icon và text
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTabColors(tabLayout.getSelectedTabPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // Không cần xử lý riêng, sẽ được cập nhật trong onTabSelected
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                updateTabColors(tabLayout.getSelectedTabPosition());
            }

            private void updateTabColors(int selectedPosition) {
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getCustomView() != null) {
                        TextView tabText = tab.getCustomView().findViewById(R.id.tab_text);
                        ImageView tabIcon = tab.getCustomView().findViewById(R.id.tab_icon);
                        if (i == selectedPosition) {
                            tabText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
                            tabIcon.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            tabText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
                            tabIcon.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.darker_gray), android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                    }
                }
            }
        });

        // Liên kết TabLayout với ViewPager2 và thêm layout tùy chỉnh
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Inflate layout tùy chỉnh cho tab
            View customView = inflater.inflate(R.layout.tab_custom, null);
            ImageView tabIcon = customView.findViewById(R.id.tab_icon);
            TextView tabText = customView.findViewById(R.id.tab_text);

            if (position == 0) {
                tabText.setText("Expired");
                tabIcon.setImageResource(R.drawable.baseline_event_busy_24);
            } else {
                tabText.setText("Completed");
                tabIcon.setImageResource(R.drawable.baseline_event_available_24);
            }

            // Đặt màu mặc định (trước khi chọn)
            tabText.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
            tabIcon.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.darker_gray), android.graphics.PorterDuff.Mode.SRC_IN);

            tab.setCustomView(customView);
        }).attach();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tabLayout = null;
        viewPager = null;
        tabAdapter = null;
    }
}