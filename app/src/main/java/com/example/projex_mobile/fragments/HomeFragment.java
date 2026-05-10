package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.adapter.QuickAccessAdapter;
import com.example.projex_mobile.adapter.RecentActivityAdapter;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.DashboardOverview;
import com.example.projex_mobile.objects.QuickAccessItem;
import com.example.projex_mobile.objects.RecentItem;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private TextView tvUserName, tvProgressPercent, tvDoneTasks, tvInProgressTasks, tvTestTasks, tvTodoTasks;
    private TextView tvRecentEmpty;
    private RecyclerView rvQuickAccess, rvRecentActivity;
    private ProgressBar progressBar;
    private View recentLabel;

    private QuickAccessAdapter quickAccessAdapter;
    private RecentActivityAdapter recentAdapter;
    private final List<QuickAccessItem> quickAccessList = new ArrayList<>();
    private final List<RecentItem> recentList = new ArrayList<>();
    private String token;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            loadDashboardOverview();
            loadRecentActivities();
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        initViews(view);
        setupRecyclerViews();
        loadData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        rvQuickAccess = view.findViewById(R.id.rvQuickAccess);
        rvRecentActivity = view.findViewById(R.id.rvRecentActivity);
        recentLabel = view.findViewById(R.id.recentLabel);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvDoneTasks = view.findViewById(R.id.tvDoneTasks);
        tvInProgressTasks = view.findViewById(R.id.tvInProgressTasks);
        tvTestTasks = view.findViewById(R.id.tvTestTasks);
        tvTodoTasks = view.findViewById(R.id.tvTodoTasks);
        tvRecentEmpty = view.findViewById(R.id.tvRecentEmpty);
    }

    private void setupRecyclerViews() {
        rvQuickAccess.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        quickAccessAdapter = new QuickAccessAdapter(quickAccessList);
        rvQuickAccess.setAdapter(quickAccessAdapter);

        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        recentAdapter = new RecentActivityAdapter(recentList);
        rvRecentActivity.setAdapter(recentAdapter);
    }

    private void loadData() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
        String userName = prefs.getString("user_name", "David");

        tvUserName.setText(userName);

        loadQuickAccess();
        updateProgressCardMock();
        updateRecentState();
    }

    private void loadQuickAccess() {
        quickAccessList.clear();
        quickAccessList.add(new QuickAccessItem(1, "My Tasks", R.drawable.ic_tasks, "CÁ NHÂN"));
        quickAccessList.add(new QuickAccessItem(2, "Projects", R.drawable.ic_document, "DỰ ÁN"));
        quickAccessList.add(new QuickAccessItem(3, "Reports", R.drawable.ic_attachment, "BÁO CÁO"));
        quickAccessList.add(new QuickAccessItem(4, "Team", R.drawable.ic_team, "ĐỘI NHÓM"));
        quickAccessAdapter.notifyDataSetChanged();
    }

    private void loadDashboardOverview() {
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getDashboardOverview(token).enqueue(new Callback<DashboardOverview>() {
            @Override
            public void onResponse(@NonNull Call<DashboardOverview> call, @NonNull Response<DashboardOverview> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateProgressCard(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardOverview> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi load dashboard: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadRecentActivities() {
        if (token == null || token.isEmpty()) {
            recentList.clear();
            recentAdapter.notifyDataSetChanged();
            updateRecentState();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getMyTasks(token).enqueue(new Callback<List<RecentItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<RecentItem>> call, @NonNull Response<List<RecentItem>> response) {
                recentList.clear();
                if (response.isSuccessful() && response.body() != null) {
                    recentList.addAll(response.body());
                }
                recentAdapter.notifyDataSetChanged();
                updateRecentState();
            }

            @Override
            public void onFailure(@NonNull Call<List<RecentItem>> call, @NonNull Throwable t) {
                recentList.clear();
                recentAdapter.notifyDataSetChanged();
                updateRecentState();
            }
        });
    }

    private void updateProgressCard(DashboardOverview data) {
        int totalTasks = data.getMyTasks();
        int doneTasks = data.getCompletedTasks();
        int inProgress = data.getInProgressTasks();
        double progress = totalTasks > 0 ? (doneTasks * 100.0 / totalTasks) : 0;

        progressBar.setProgress((int) progress);
        tvProgressPercent.setText(String.format("%.0f%%", progress));
        tvDoneTasks.setText(doneTasks + " Tasks");
        tvInProgressTasks.setText(inProgress + " Tasks");
        tvTestTasks.setText("5 Tasks");
        tvTodoTasks.setText(Math.max(totalTasks - doneTasks - inProgress, 0) + " Tasks");
    }

    private void updateRecentState() {
        boolean hasItems = recentList != null && !recentList.isEmpty();
        recentLabel.setVisibility(View.VISIBLE);
        rvRecentActivity.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        tvRecentEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void updateProgressCardMock() {
        progressBar.setProgress(40);
        tvProgressPercent.setText("40%");
        tvDoneTasks.setText("12 Tasks");
        tvInProgressTasks.setText("5 Tasks");
        tvTestTasks.setText("5 Tasks");
        tvTodoTasks.setText("8 Tasks");
    }

    private void loadRecentMock() {
        recentList.clear();
        RecentItem item = new RecentItem();
        item.setTitle("Vẽ Sequence diagram");
        item.setMessage("Dat updated a story");
        item.setTicketCode("GGSHOP-3");
        item.setAvatarText("DA");
        item.setTimeAgo("2h ago");
        item.setStatus("InProgress");
        recentList.add(item);

        recentAdapter.notifyDataSetChanged();
        updateRecentState();
    }
}