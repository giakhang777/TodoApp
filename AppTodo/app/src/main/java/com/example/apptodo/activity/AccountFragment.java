package com.example.apptodo.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.apptodo.R;

public class AccountFragment extends Fragment {

    private Button btnLogout;
    private SharedPreferences sharedPreferences;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo SharedPreferences để lưu thông tin người dùng
        sharedPreferences = getActivity().getSharedPreferences("login_prefs", getActivity().MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Gán Button Logout
        btnLogout = view.findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    private void logoutUser() {
        // Xóa thông tin đăng nhập trong SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("isLoggedIn");   // Xóa trạng thái đã đăng nhập
        editor.remove("username");     // Xóa tên người dùng
        editor.apply();

        // Hiển thị thông báo
        Toast.makeText(getActivity(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Quay lại màn hình đăng nhập
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();  // Đóng màn hình hiện tại để không quay lại được
    }
}
