package com.example.apptodo.viewmodel;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.apptodo.api.LabelService;
import com.example.apptodo.model.request.LabelRequest;
import com.example.apptodo.model.response.LabelResponse;
import com.example.apptodo.retrofit.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LabelViewModel extends AndroidViewModel {
    private LabelService labelService;
    private MutableLiveData<List<LabelResponse>> labelsLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LabelViewModel(Application application) {
        super(application);
        labelService = RetrofitClient.getLabelService();
    }

    public LiveData<List<LabelResponse>> getLabels() {
        return labelsLiveData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadLabels(int userId) {
        labelService.getAllLabel(userId).enqueue(new Callback<List<LabelResponse>>() {
            @Override
            public void onResponse(Call<List<LabelResponse>> call, Response<List<LabelResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    labelsLiveData.setValue(response.body());
                } else {
                    errorMessage.setValue("Failed to load labels");
                }
            }

            @Override
            public void onFailure(Call<List<LabelResponse>> call, Throwable t) {
                errorMessage.setValue("Error: " + t.getMessage());
            }
        });
    }

    public void createLabel(int userId, String title) {
        LabelRequest request = new LabelRequest(userId, title);
        labelService.createLabel(request).enqueue(new Callback<LabelResponse>() {
            @Override
            public void onResponse(Call<LabelResponse> call, Response<LabelResponse> response) {
                if (response.isSuccessful()) {
                    loadLabels(userId);  // Reload labels after adding new one
                } else {
                    errorMessage.setValue("Failed to add label");
                }
            }

            @Override
            public void onFailure(Call<LabelResponse> call, Throwable t) {
                errorMessage.setValue("Error: " + t.getMessage());
            }
        });
    }

    public void deleteLabel(int labelId, int userId) {
        labelService.deleteLabel(labelId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadLabels(userId);  // Reload labels after deletion
                } else {
                    errorMessage.setValue("Failed to delete label");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                errorMessage.setValue("Error: " + t.getMessage());
            }
        });
    }

}
