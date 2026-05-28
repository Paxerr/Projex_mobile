package com.example.projex_mobile.api;

import com.example.projex_mobile.objects.DashboardOverview;
import com.example.projex_mobile.objects.RecentItem;
import com.example.projex_mobile.objects.TaskResponse;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
    Call<JsonObject> login(@Body Map<String, String> body);

    @POST("api/auth/register")
    Call<JsonObject> register(@Body Map<String, String> body);

    @GET("api/dashboard/overview")
    Call<DashboardOverview> getDashboardOverview(@Header("Authorization") String token);

    @GET("api/dashboard/my-tasks")
    Call<List<RecentItem>> getMyTasks(@Header("Authorization") String token);

    @GET("api/tasks/assigned")
    Call<TaskResponse> getAssignedTasks(@Header("Authorization") String token);

    @GET("api/projects")
    Call<JsonObject> getProjects(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("keyword") String keyword,
            @Query("status") String status,
            @Query("sortBy") String sortBy,
            @Query("sortOrder") String sortOrder
    );

    @GET("api/projects/{id}")
    Call<JsonObject> getProjectById(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @POST("api/projects")
    Call<JsonObject> createProject(
            @Header("Authorization") String token,
            @Body Map<String, Object> body
    );
    @GET("api/projects/{projectId}/tasks")
    Call<TaskResponse> getTasksByProject(
            @Header("Authorization") String token,
            @Path("projectId") int projectId
    );


}