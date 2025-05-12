package com.example.apptodo.api;



import com.example.apptodo.model.request.ProjectRequest;
import com.example.apptodo.model.response.ProjectResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface ProjectService {

    @POST("project")
    Call<ProjectResponse> createProject(@Body ProjectRequest request);
    @GET("project/user/{userId}")
    Call<List<ProjectResponse>> getAllProjects(@Path("userId") Integer userId);
}
