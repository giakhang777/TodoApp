package com.example.apptodo.api;

import com.example.apptodo.model.request.SubTaskRequest;
import com.example.apptodo.model.response.SubTaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface SubTaskService {

    @POST("subtask")
    Call<SubTaskResponse> createSubTask(@Body SubTaskRequest request);

    @GET("subtask/task/{taskId}")
    Call<List<SubTaskResponse>> getSubTasksByTaskId(@Path("taskId") Integer taskId);

    @PUT("subtask/{subTaskId}")
    Call<SubTaskResponse> updateSubTask(@Path("subTaskId") Integer subTaskId, @Body SubTaskRequest request);

    @DELETE("subtask/{subTaskId}")
    Call<Void> deleteSubTask(@Path("subTaskId") Integer subTaskId);

    @PATCH("subtask/{subTaskId}/status")
    Call<SubTaskResponse> changeSubTaskStatus(@Path("subTaskId") Integer subTaskId, @Body Boolean completed);
}
