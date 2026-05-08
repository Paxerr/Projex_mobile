package com.example.project_mobile.api;

import com.example.projex_mobile.objects.DashboardOverview;
import com.example.projex_mobile.objects.RecentActivity;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface ApiService {
    @GET("api/dashboard/overview")
    Call<DashboardOverview> getDashboardOverview(@Header("Authorization") String token);

    @GET("api/dashboard/my-tasks")
    Call<List<RecentActivity>> getMyTasks(@Header("Authorization") String token);
}