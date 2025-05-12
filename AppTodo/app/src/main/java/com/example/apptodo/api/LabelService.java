package com.example.apptodo.api;

import com.example.apptodo.model.request.LabelRequest;
import com.example.apptodo.model.request.ProjectRequest;
import com.example.apptodo.model.response.LabelResponse;
import com.example.apptodo.model.response.ProjectResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface LabelService {
    @POST("label")
    Call<LabelResponse> createLabel(@Body LabelRequest request);
    @GET("label/user/{userId}")
    Call<List<LabelResponse>> getAllLabel(@Path("userId") Integer userId);
    @DELETE("label/{id}")
    Call<Void> deleteLabel(@Path("id") Integer labelId);
}
