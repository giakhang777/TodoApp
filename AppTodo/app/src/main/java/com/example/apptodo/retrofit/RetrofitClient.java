package com.example.apptodo.retrofit;

import com.example.apptodo.api.LabelService;
import com.example.apptodo.api.ProjectService;
import com.example.apptodo.api.SubTaskService;
import com.example.apptodo.api.TaskService;
import com.example.apptodo.api.UserService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor; // Thêm import này
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    private static final String BASE_URL = "http://10.0.2.2:8080/api/";

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .addInterceptor(loggingInterceptor);

            OkHttpClient client = httpClientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static UserService getApiUserService() {
        return getRetrofit().create(UserService.class);
    }

    public static ProjectService getProjectService() {
        return getRetrofit().create(ProjectService.class);
    }
    public static LabelService getLabelService(){
        return getRetrofit().create(LabelService.class);
    }

    public static TaskService getTaskService() {
        return getRetrofit().create(TaskService.class);
    }

    public static SubTaskService getSubTaskService() {
        return getRetrofit().create(SubTaskService.class);
    }
}