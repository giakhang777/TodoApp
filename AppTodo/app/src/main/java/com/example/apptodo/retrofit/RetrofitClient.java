package com.example.apptodo.retrofit;

import com.example.apptodo.api.TaskService;
import com.example.apptodo.api.UserService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            // Tạo một OkHttpClient với cấu hình theo dõi chuyển hướng
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true) // Bật theo dõi chuyển hướng HTTP
                    .followSslRedirects(true) // Theo dõi chuyển hướng SSL
                    .build();

            // Tạo Retrofit với OkHttpClient tùy chỉnh
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Đường dẫn API
                    .addConverterFactory(GsonConverterFactory.create()) // Chuyển JSON thành Object
                    .client(client) // Thêm OkHttpClient vào Retrofit
                    .build();
        }
        return retrofit;
    }

    public static UserService getApiUserService() {
        return getRetrofit().create(UserService.class);
    }
    public static TaskService getTaskService() {
        return getRetrofit().create(TaskService.class);
    }
}
