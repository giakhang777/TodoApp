package com.example.apptodo.api;

import com.example.apptodo.model.EmailRequest;
import com.example.apptodo.model.LoginRequest;
import com.example.apptodo.model.OTPRequest;
import com.example.apptodo.model.ResetPassword;
import com.example.apptodo.model.SignUpRequest;
import com.example.apptodo.model.User;
import com.example.apptodo.model.UserResponse;

import java.util.Map;
import okhttp3.ResponseBody;
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
    Call<Map<String,String>>resetPassword(@Body ResetPassword request);
    @POST("user/login")
    Call<UserResponse> login(@Body LoginRequest request);

}
