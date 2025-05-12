package com.example.apptodo.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.apptodo.R;
import com.example.apptodo.adapter.LabelAdapter;
import com.example.apptodo.api.LabelService;
import com.example.apptodo.model.request.LabelRequest;
import com.example.apptodo.model.response.LabelResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LabelFragment extends Fragment {

    private ListView labelListView;
    private LabelAdapter labelAdapter;
    private ArrayList<LabelResponse> labelList;
    private LabelService labelService;
    private SharedUserViewModel sharedUserViewModel;

    public LabelFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_label, container, false);

        FloatingActionButton addButton = view.findViewById(R.id.addButton);
        labelListView = view.findViewById(R.id.labelListView);

        labelList = new ArrayList<>();
        labelAdapter = new LabelAdapter(getContext(), labelList);
        labelListView.setAdapter(labelAdapter);

        labelService = RetrofitClient.getLabelService();
        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);

        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null) {
                int userId = userResponse.getId();
                loadLabelsFromApi(userId);

                addButton.setOnClickListener(v -> showAddLabelDialog(userId));
            }
        });

        labelListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            LabelResponse labelToDelete = labelList.get(position);
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Label")
                    .setMessage("Do you want to delete \"" + labelToDelete.getTitle() + "\"?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        labelService.deleteLabel(labelToDelete.getId()).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(getContext(), "Deleted: " + labelToDelete.getTitle(), Toast.LENGTH_SHORT).show();
                                    loadLabelsFromApi(sharedUserViewModel.getUser().getValue().getId());
                                } else {
                                    Toast.makeText(getContext(), "Failed to delete label", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null) {
                loadLabelsFromApi(userResponse.getId());
            }
        });
    }

    private void loadLabelsFromApi(int userId) {
        labelService.getAllLabel(userId).enqueue(new Callback<List<LabelResponse>>() {
            @Override
            public void onResponse(Call<List<LabelResponse>> call, Response<List<LabelResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    labelList.clear();
                    labelList.addAll(response.body());
                    labelAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load labels", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<LabelResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddLabelDialog(int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_label, null);
        builder.setView(dialogView);

        EditText editTextLabel = dialogView.findViewById(R.id.et_label_name);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String labelName = editTextLabel.getText().toString().trim();
            if (!labelName.isEmpty()) {
                createLabel(userId, labelName);
            } else {
                Toast.makeText(getContext(), "Label name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void createLabel(int userId, String title) {
        LabelRequest request = new LabelRequest(userId, title);
        labelService.createLabel(request).enqueue(new Callback<LabelResponse>() {
            @Override
            public void onResponse(Call<LabelResponse> call, Response<LabelResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Label added", Toast.LENGTH_SHORT).show();
                    loadLabelsFromApi(userId);
                } else {
                    Toast.makeText(getContext(), "Failed to add label", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LabelResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
