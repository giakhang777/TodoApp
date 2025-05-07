package com.example.apptodo.api;

import com.example.apptodo.model.request.EmailRequest;
import com.example.apptodo.model.request.LoginRequest;
import com.example.apptodo.model.request.OTPRequest;
import com.example.apptodo.model.request.SignUpRequest;
import com.example.apptodo.model.UserResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserService {
    @POST("auth/register")
    Call<Map<String, String>> signUpPostForm(@Body SignUpRequest user);
    @POST("auth/verify-code")
    Call<Map<String, String>> verifyCode(@Body OTPRequest request);
    @POST("auth/forgot-password/verify-code")
    Call<Map<String,String>> verifyResetCode(@Body OTPRequest request);
    @POST("auth/forgot-password")
    Call<Map<String,String>>forgotPassword(@Body EmailRequest request);
    @POST("auth/reset-password")
    Call<Map<String,String>>resetPassword(@Body SignUpRequest.ResetPassword request);
    @POST("user/login")
    Call<UserResponse> login(@Body LoginRequest request);

}
