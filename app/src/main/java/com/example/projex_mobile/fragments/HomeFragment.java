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
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

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
    private TextInputLayout searchLayout;
    private TextInputEditText edtSearch;

    private QuickAccessAdapter quickAccessAdapter;
    private RecentActivityAdapter recentAdapter;
    private final List<QuickAccessItem> quickAccessList = new ArrayList<>();
    private final List<RecentItem> recentList = new ArrayList<>();
    private String token;
    private static final boolean IS_MOCK_MODE = true;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (IS_MOCK_MODE) {
                updateProgressCardMock();
            } else {
                loadDashboardOverview();
                loadRecentActivities();
            }
            handler.postDelayed(this, 5000);
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupSearchBar(view);
        setupRecyclerViews();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(refreshRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
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
        searchLayout = view.findViewById(R.id.searchLayout);
        edtSearch = view.findViewById(R.id.edtSearch);
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

        if (IS_MOCK_MODE) {
            loadRecentMock();
        } else {
            loadRecentActivities();
        }
    }
    private void setupSearchBar(View view) {
        if (searchLayout == null || edtSearch == null) return;

        searchLayout.setHint("Tìm kiếm");
        edtSearch.setCursorVisible(false);

        edtSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                searchLayout.setHint(null);
                edtSearch.setCursorVisible(true);
            } else {
                CharSequence text = edtSearch.getText();
                if (text == null || text.toString().trim().isEmpty()) {
                    searchLayout.setHint("Tìm kiếm");
                }
                edtSearch.setCursorVisible(false);
            }
        });

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    searchLayout.setHint(null);
                } else if (!edtSearch.hasFocus()) {
                    searchLayout.setHint("Tìm kiếm");
                }
            }
        });

        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && edtSearch.isFocused()) {
                Rect rect = new Rect();
                edtSearch.getGlobalVisibleRect(rect);
                if (!rect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    edtSearch.clearFocus();
                    edtSearch.setCursorVisible(false);
                    v.performClick();
                }
            }
            return false;
        });
    }
    private void loadQuickAccess() {
        int oldSize = quickAccessList.size();
        if (oldSize > 0) {
            quickAccessList.clear();
            quickAccessAdapter.notifyItemRangeRemoved(0, oldSize);
        }

        quickAccessList.add(new QuickAccessItem(1, "My Tasks", R.drawable.home_ic_task, "CÁ NHÂN"));
        quickAccessList.add(new QuickAccessItem(2, "Projects", R.drawable.ic_document, "DỰ ÁN"));
        quickAccessList.add(new QuickAccessItem(3, "Reports", R.drawable.ic_attachment, "BÁO CÁO"));
        quickAccessList.add(new QuickAccessItem(4, "Team", R.drawable.ic_team, "ĐỘI NHÓM"));
        quickAccessAdapter.notifyItemRangeInserted(0, quickAccessList.size());
    }

    private void loadDashboardOverview() {
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getDashboardOverview(token).enqueue(new Callback<>() {
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
            int oldSize = recentList.size();
            recentList.clear();
            if (oldSize > 0) {
                recentAdapter.notifyItemRangeRemoved(0, oldSize);
            }
            updateRecentState();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getMyTasks(token).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<RecentItem>> call, @NonNull Response<List<RecentItem>> response) {
                int oldSize = recentList.size();
                recentList.clear();
                if (oldSize > 0) {
                    recentAdapter.notifyItemRangeRemoved(0, oldSize);
                }

                if (response.isSuccessful() && response.body() != null) {
                    recentList.addAll(response.body());
                    recentAdapter.notifyItemRangeInserted(0, recentList.size());
                }
                updateRecentState();
            }

            @Override
            public void onFailure(@NonNull Call<List<RecentItem>> call, @NonNull Throwable t) {
                int oldSize = recentList.size();
                recentList.clear();
                if (oldSize > 0) {
                    recentAdapter.notifyItemRangeRemoved(0, oldSize);
                }
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
        tvProgressPercent.setText(getString(R.string.progress_percent, progress));
        tvDoneTasks.setText(getString(R.string.tasks_label, doneTasks));
        tvInProgressTasks.setText(getString(R.string.tasks_label, inProgress));
        tvTestTasks.setText(getString(R.string.tasks_label, 5));
        tvTodoTasks.setText(getString(R.string.tasks_label, Math.max(totalTasks - doneTasks - inProgress, 0)));
    }

    private void updateRecentState() {
        boolean hasItems = !recentList.isEmpty();
        recentLabel.setVisibility(View.VISIBLE);
        rvRecentActivity.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        tvRecentEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }


    //test
    private void updateProgressCardMock() {
        progressBar.setProgress(40);
        tvProgressPercent.setText(getString(R.string.progress_percent, 40f));
        tvDoneTasks.setText(getString(R.string.tasks_label, 12));
        tvInProgressTasks.setText(getString(R.string.tasks_label, 5));
        tvTestTasks.setText(getString(R.string.tasks_label, 5));
        tvTodoTasks.setText(getString(R.string.tasks_label, 8));
    }

    private void loadRecentMock() {
        int oldSize = recentList.size();
        recentList.clear();
        if (oldSize > 0) {
            recentAdapter.notifyItemRangeRemoved(0, oldSize);
        }

        RecentItem item1 = new RecentItem();
        item1.setTitle("Vẽ Sequence diagram");
        item1.setMessage("Dat updated a story");
        item1.setTicketCode("GGSHOP-3");
        item1.setAvatarText("DA");
        item1.setTimeAgo("2h ago");
        item1.setStatus("InProgress");
        recentList.add(item1);

        recentAdapter.notifyItemRangeInserted(0, recentList.size());
        updateRecentState();
    }
}