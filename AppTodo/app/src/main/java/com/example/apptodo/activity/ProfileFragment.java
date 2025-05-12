package com.example.apptodo.activity;

import android.app.AlertDialog;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.apptodo.R;
import com.example.apptodo.api.ProjectService;
import com.example.apptodo.model.UserResponse;
import com.example.apptodo.model.request.ProjectRequest;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.viewmodel.ProjectViewModel;
import com.example.apptodo.viewmodel.SharedUserViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private List<ProjectResponse> projectList = new ArrayList<>();
    private TextView profileNameTextView;
    private ImageView profileImageView;
    private ListView projectListView;
    private SharedUserViewModel userViewModel;
    private ProjectViewModel projectViewModel;

    private String selectedColor = "#000000";

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // ViewModel
        userViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);
        projectViewModel = new ViewModelProvider(requireActivity()).get(ProjectViewModel.class);

        // Views
        profileImageView = view.findViewById(R.id.profile_image);
        profileNameTextView = view.findViewById(R.id.profile_name);
        ImageButton btnNotification = view.findViewById(R.id.btn_notification);
        Button btnMyAccount = view.findViewById(R.id.btn_my_account);
        Button btnAddProject = view.findViewById(R.id.btn_add_project);
        Button btnCompleted = view.findViewById(R.id.btn_completed);
        Button btnLabel = view.findViewById(R.id.btn_labels);
        projectListView = view.findViewById(R.id.project_list_view);

        // Observe user and load avatar + projects
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                profileNameTextView.setText(user.getUsername());
                if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    Glide.with(requireContext())
                            .load(user.getAvatar())
                            .placeholder(R.drawable.defaulter_user)
                            .into(profileImageView);
                }

                projectViewModel.fetchProjects(user.getId());
            }
        });

        // Observe projects from ViewModel
        projectViewModel.getProjectList().observe(getViewLifecycleOwner(), projects -> {
            projectList.clear();
            projectList.addAll(projects);
            updateProjectList();
        });

        // Listeners
        btnMyAccount.setOnClickListener(v -> openFragment(new AccountFragment()));
        btnAddProject.setOnClickListener(v -> showAddProjectDialog());
        btnCompleted.setOnClickListener(v -> openFragment(new HistoryFragment()));
        btnLabel.setOnClickListener(v -> openFragment(new LabelFragment()));
        btnNotification.setOnClickListener(v -> showNotificationPopup(v));

        return view;
    }

    private void updateProjectList() {
        ArrayAdapter<ProjectResponse> adapter = new ArrayAdapter<ProjectResponse>(getContext(), android.R.layout.simple_list_item_1, projectList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView text = row.findViewById(android.R.id.text1);

                ProjectResponse project = getItem(position);
                if (project != null && text != null) {
                    text.setText(project.getName());
                    try {
                        text.setTextColor(android.graphics.Color.parseColor(project.getColor()));
                    } catch (IllegalArgumentException e) {
                        text.setTextColor(android.graphics.Color.BLACK);
                    }
                }
                return row;
            }
        };
        projectListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void showAddProjectDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_project, null);
        EditText etProjectName = dialogView.findViewById(R.id.et_project_name);
        View viewSelectedColor = dialogView.findViewById(R.id.view_selected_color);
        LinearLayout layoutColorPicker = dialogView.findViewById(R.id.layout_color_picker);

        layoutColorPicker.setOnClickListener(v -> showColorPickerDialog(viewSelectedColor));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Add New Project")
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String projectName = etProjectName.getText().toString().trim();

            if (projectName.isEmpty()) {
                etProjectName.setError("Project name is required");
                return;
            }

            UserResponse user = userViewModel.getUser().getValue();
            if (user == null) {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            ProjectRequest request = new ProjectRequest(user.getId(), projectName, selectedColor);
            ProjectService service = RetrofitClient.getProjectService();

            service.createProject(request).enqueue(new Callback<ProjectResponse>() {
                @Override
                public void onResponse(Call<ProjectResponse> call, Response<ProjectResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        projectViewModel.addProject(response.body());
                        Toast.makeText(getContext(), "Project created successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to create project", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ProjectResponse> call, Throwable t) {
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showColorPickerDialog(View viewSelectedColor) {
        final String[] colors = {
                "#FF0000", "#00FF00", "#0000FF", "#FFFF00", "#FF00FF", "#00FFFF",
                "#000000", "#FFFFFF", "#808080", "#C0C0C0", "#800000", "#008000"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Color");

        HorizontalScrollView scrollView = new HorizontalScrollView(requireContext());
        LinearLayout colorLayout = new LinearLayout(requireContext());
        colorLayout.setOrientation(LinearLayout.HORIZONTAL);
        colorLayout.setPadding(16, 16, 16, 16);
        scrollView.addView(colorLayout);

        AlertDialog colorDialog = builder.setView(scrollView).create();

        for (String color : colors) {
            ImageView colorView = new ImageView(requireContext());
            int size = 100;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(10, 0, 10, 0);
            colorView.setLayoutParams(params);

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(android.graphics.Color.parseColor(color));
            drawable.setSize(size, size);
            colorView.setBackground(drawable);

            colorView.setOnClickListener(v -> {
                selectedColor = color;

                GradientDrawable selectedDrawable = new GradientDrawable();
                selectedDrawable.setShape(GradientDrawable.OVAL);
                selectedDrawable.setColor(android.graphics.Color.parseColor(color));
                selectedDrawable.setSize(24, 24);
                viewSelectedColor.setBackground(selectedDrawable);

                colorDialog.dismiss();
            });

            colorLayout.addView(colorView);
        }

        colorDialog.show();
    }

    private void showNotificationPopup(View anchor) {
        // TODO: Hiển thị popup thông báo
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
