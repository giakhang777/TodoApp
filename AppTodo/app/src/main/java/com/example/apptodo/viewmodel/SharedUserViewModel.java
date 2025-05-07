package com.example.apptodo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.apptodo.model.User;
import com.example.apptodo.model.UserResponse;

public class SharedUserViewModel extends ViewModel {
    private final MutableLiveData<UserResponse> userLiveData = new MutableLiveData<>();

    public void setUser(UserResponse user) {
        userLiveData.setValue(user);
    }

    public LiveData<UserResponse> getUser() {
        return userLiveData;
    }
}
