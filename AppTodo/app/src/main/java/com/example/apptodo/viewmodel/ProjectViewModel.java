package com.example.apptodo.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.apptodo.api.ProjectService;
import com.example.apptodo.model.response.ProjectResponse;
import com.example.apptodo.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectViewModel extends ViewModel {

    private final MutableLiveData<List<ProjectResponse>> projectList = new MutableLiveData<>(new ArrayList<>());

    private final MutableLiveData<ProjectResponse> singleProjectLiveData = new MutableLiveData<>();

    public LiveData<List<ProjectResponse>> getProjectList() {
        return projectList;
    }

    public LiveData<ProjectResponse> getSingleProject() {
        return singleProjectLiveData;
    }

    public void fetchProjects(int userId) {
        ProjectService service = RetrofitClient.getProjectService();
        service.getAllProjects(userId).enqueue(new Callback<List<ProjectResponse>>() {
            @Override
            public void onResponse(Call<List<ProjectResponse>> call, Response<List<ProjectResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    projectList.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<ProjectResponse>> call, Throwable t) {
            }
        });
    }

    public void addProject(ProjectResponse project) {
        List<ProjectResponse> current = projectList.getValue();
        if (current != null) {
            current.add(project);
            projectList.setValue(new ArrayList<>(current));
        }
    }

    public void removeProject(ProjectResponse project) {
        List<ProjectResponse> currentProjects = projectList.getValue();
        if (currentProjects != null) {
            currentProjects.remove(project);
            projectList.setValue(currentProjects);
        }
    }

    public void getProjectById(int projectId) {
        List<ProjectResponse> currentProjects = projectList.getValue();
        ProjectResponse foundProject = null;
        if (currentProjects != null) {
            for (ProjectResponse project : currentProjects) {
                if (project.getId() == projectId) {
                    foundProject = project;
                    break;
                }
            }
        }
        singleProjectLiveData.setValue(foundProject);
    }
}
