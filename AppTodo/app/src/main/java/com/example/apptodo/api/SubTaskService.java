package com.example.apptodo.api;

import com.example.apptodo.model.response.SubTaskResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface SubTaskService {
    @GET("subtask/task/{taskId}")
    Call<List<SubTaskResponse>> getSubTasksByTaskId(@Path("taskId") Integer taskId);
}