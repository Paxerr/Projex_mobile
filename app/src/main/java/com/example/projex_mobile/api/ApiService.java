package com.example.projex_mobile.api;

import com.example.projex_mobile.objects.DashboardOverview;
import com.example.projex_mobile.objects.Project;
import com.example.projex_mobile.objects.RecentItem;
import com.example.projex_mobile.objects.Task;
import com.example.projex_mobile.objects.TaskResponse;
import com.example.projex_mobile.objects.User;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/auth/login")
    Call<JsonObject> login(@Body Map<String, String> body);

    @POST("api/auth/register")
    Call<JsonObject> register(@Body Map<String, String> body);

    @POST("api/auth/forgot-password")
    Call<JsonObject> forgotPassword(@Body Map<String, String> body);

    @POST("api/auth/verify-reset-code")
    Call<JsonObject> verifyResetCode(@Body Map<String, String> body);

    @POST("api/auth/reset-password")
    Call<JsonObject> resetPassword(@Body Map<String, String> body);

    @GET("api/auth/me")
    Call<User> getProfile(@Header("Authorization") String token);

    @PUT("api/auth/profile")
    Call<JsonObject> updateProfile(
            @Header("Authorization") String token,
            @Body Map<String, String> body
    );

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

    @PUT("api/tasks/{id}")
    Call<Task> updateTask(
            @Header("Authorization") String token,
            @Path("id") int id,
            @Body Map<String, Object> body
    );
    @GET("api/tasks/{taskId}")
    Call<Task> getTaskById(
            @Header("Authorization") String token,
            @Path("taskId") int taskId
    );
    @POST("api/projects/{projectId}/tasks")
    Call<Void> createTask(
            @Header("Authorization") String token,
            @Path("projectId") int projectId,
            @Body Map<String, Object> body
    );

    @POST("api/projects/{projectId}/members/by-email")
    Call<JsonObject> addProjectMemberByEmail(
            @Header("Authorization") String token,
            @Path("projectId") int projectId,
            @Body Map<String, String> body
    );

    @PUT("api/projects/{projectId}/members/{userId}/role")
    Call<JsonObject> updateProjectMemberRole(
            @Header("Authorization") String token,
            @Path("projectId") int projectId,
            @Path("userId") int userId,
            @Body Map<String, String> body
    );

    @DELETE("api/projects/{projectId}/members/{userId}")
    Call<JsonObject> removeProjectMember(
            @Header("Authorization") String token,
            @Path("projectId") int projectId,
            @Path("userId") int userId
    );
    @GET("api/projects/{id}")
    Call<Project> getProjectDetail(
            @Header("Authorization") String token,
            @Path("id") int id
    );
    @POST("api/tasks/{taskId}/assignments")
    Call<JsonObject> addTaskAssignments(
            @Header("Authorization") String token,
            @Path("taskId") int taskId,
            @Body Map<String, Object> body
    );

    @DELETE("api/tasks/{taskId}/assignments/{userId}")
    Call<JsonObject> removeTaskAssignment(
            @Header("Authorization") String token,
            @Path("taskId") int taskId,
            @Path("userId") int userId
    );
}
