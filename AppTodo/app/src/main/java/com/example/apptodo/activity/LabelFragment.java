package com.example.apptodo.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import com.example.apptodo.R;
import com.example.apptodo.adapter.LabelAdapter;
import com.example.apptodo.model.response.LabelResponse;
import com.example.apptodo.viewmodel.LabelViewModel;
import com.example.apptodo.viewmodel.SharedUserViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class LabelFragment extends Fragment {

    private ListView labelListView;
    private LabelAdapter labelAdapter;
    private ArrayList<LabelResponse> labelList;
    private LabelViewModel labelViewModel;
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

        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);
        sharedUserViewModel = new ViewModelProvider(requireActivity()).get(SharedUserViewModel.class);

        sharedUserViewModel.getUser().observe(getViewLifecycleOwner(), userResponse -> {
            if (userResponse != null) {
                int userId = userResponse.getId();
                labelViewModel.loadLabels(userId);

                labelViewModel.getLabels().observe(getViewLifecycleOwner(), labels -> {
                    labelList.clear();
                    labelList.addAll(labels);
                    labelAdapter.notifyDataSetChanged();
                });

                labelViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
                    if (error != null) {
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });

                addButton.setOnClickListener(v -> showAddLabelDialog(userId));
            }
        });

        labelListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            LabelResponse labelToDelete = labelList.get(position);
            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Label")
                    .setMessage("Do you want to delete \"" + labelToDelete.getTitle() + "\"?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        labelViewModel.deleteLabel(labelToDelete.getId(), sharedUserViewModel.getUser().getValue().getId());
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

        return view;
    }

    private void showAddLabelDialog(int userId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_label, null);
        builder.setView(dialogView);

        EditText editTextLabel = dialogView.findViewById(R.id.et_label_name);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String labelName = editTextLabel.getText().toString().trim();
            if (!labelName.isEmpty()) {
                labelViewModel.createLabel(userId, labelName);
            } else {
                Toast.makeText(getContext(), "Label name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}
