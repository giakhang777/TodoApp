package com.example.apptodo.api;

import com.example.apptodo.model.request.TaskRequest;
import com.example.apptodo.model.response.TaskResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface TaskService {
    @POST("/api/task")
    Call<TaskResponse> createTask(@Body TaskRequest taskRequest);

    @GET("/api/task/{id}")
    Call<TaskResponse> getTaskById(@Path("id") int taskId);

    @GET("/api/task/project/{projectId}")
    Call<List<TaskResponse>> getTasksByProject(@Path("projectId") int projectId);

    @PUT("/api/task/{id}")
    Call<TaskResponse> updateTask(@Path("id") int taskId, @Body TaskRequest taskRequest);

    @DELETE("/api/task/{id}")
    Call<Void> deleteTask(@Path("id") int taskId);

    @PATCH("/api/task/{taskId}/status")
    Call<TaskResponse> changeTaskStatus(@Path("taskId") int taskId, @Body Boolean completed);

    @GET("/api/task/date/{date}")
    Call<List<TaskResponse>> getTasksByDate(@Path("date") String date);
}
