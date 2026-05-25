package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvProgressPercent, tvDoneTasks, tvInProgressTasks, tvTestTasks, tvTodoTasks;
    private TextView tvRecentEmpty, tvViewAll;
    private RecyclerView rvQuickAccess, rvRecentActivity;
    private PieChart pieChart;
    private View recentLabel;
    private View quickAccessSection;
    private TextInputLayout searchLayout;
    private TextInputEditText edtSearch;

    private QuickAccessAdapter quickAccessAdapter;
    private RecentActivityAdapter recentAdapter;
    private final List<QuickAccessItem> quickAccessList = new ArrayList<>();
    private final List<RecentItem> recentList = new ArrayList<>();
    private String token;
    private static final boolean IS_MOCK_MODE = true;
    private boolean isQuickAccessExpanded = true;

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
        setupQuickAccessToggle();
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadQuickAccess();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        rvQuickAccess = view.findViewById(R.id.rvQuickAccess);
        rvRecentActivity = view.findViewById(R.id.rvRecentActivity);
        recentLabel = view.findViewById(R.id.recentLabel);
        quickAccessSection = view.findViewById(R.id.quickAccessSession);
        pieChart = view.findViewById(R.id.pieChart);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvDoneTasks = view.findViewById(R.id.tvDoneTasks);
        tvInProgressTasks = view.findViewById(R.id.tvInProgressTasks);
        tvTodoTasks = view.findViewById(R.id.tvTodoTasks);
        tvRecentEmpty = view.findViewById(R.id.tvRecentEmpty);
        searchLayout = view.findViewById(R.id.searchLayout);
        edtSearch = view.findViewById(R.id.edtSearch);
        tvViewAll = view.findViewById(R.id.tvViewAll);
    }

    private void setupRecyclerViews() {
        int orientation = getResources().getConfiguration().orientation;
        int swDp = getResources().getConfiguration().smallestScreenWidthDp;
        boolean isTablet = swDp >= 600;
        boolean useVerticalQuickAccess = isTablet && orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE;

        rvQuickAccess.setLayoutManager(
                new LinearLayoutManager(
                        requireContext(),
                        useVerticalQuickAccess ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        quickAccessAdapter = new QuickAccessAdapter(quickAccessList, useVerticalQuickAccess ? 1 : 0, item -> {
            if ("My Tasks".equals(item.getName())) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, new TaskFragment())
                        .addToBackStack(null)
                        .commit();
            } else if ("Favorite".equals(item.getName())) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, new SpaceListFragment())
                        .addToBackStack(null)
                        .commit();
            } else {
                ProjectFragment projectFragment = new ProjectFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("project_id", item.getId());
                bundle.putString("project_name", item.getName());
                projectFragment.setArguments(bundle);

                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_container, projectFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        rvQuickAccess.setAdapter(quickAccessAdapter);
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentActivity.setNestedScrollingEnabled(false);
        rvRecentActivity.setHasFixedSize(false);
        recentAdapter = new RecentActivityAdapter(recentList);
        rvRecentActivity.setAdapter(recentAdapter);
    }

    private void setupQuickAccessToggle() {
        updateQuickAccessState();
        tvViewAll.setOnClickListener(v -> {
            isQuickAccessExpanded = !isQuickAccessExpanded;
            updateQuickAccessState();
        });
    }

    private void updateQuickAccessState() {
        tvViewAll.setText("XEM TẤT CẢ");
        tvViewAll.setTextColor(Color.parseColor(isQuickAccessExpanded ? "#85ADFF" : "#6B7280"));
        rvQuickAccess.setVisibility(isQuickAccessExpanded ? View.VISIBLE : View.GONE);
        if (quickAccessSection != null) {
            quickAccessSection.requestLayout();
        }
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

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

        quickAccessList.add(new QuickAccessItem(991, "My Tasks", R.drawable.home_ic_task, "CÁ NHÂN"));

        SharedPreferences spacePrefs = requireContext().getSharedPreferences("space_prefs", Context.MODE_PRIVATE);
        List<Integer> favoriteIds = new ArrayList<>();
        String favJson = spacePrefs.getString("favorite_ids", "[]");
        try {
            JSONArray arr = new JSONArray(favJson);
            for (int i = 0; i < arr.length(); i++) {
                favoriteIds.add(arr.getInt(i));
            }
        } catch (JSONException ignored) {}

        List<QuickAccessItem> allAvailableProjects = new ArrayList<>();
        allAvailableProjects.add(new QuickAccessItem(1, "GGshop", R.drawable.login_logo_github, "DỰ ÁN"));
        allAvailableProjects.add(new QuickAccessItem(2, "gg_projex", R.drawable.login_logo_pj_rm_bg, "DỰ ÁN"));
        allAvailableProjects.add(new QuickAccessItem(3, "GGweb", R.drawable.logo_library_web, "DỰ ÁN"));

        for (QuickAccessItem project : allAvailableProjects) {
            if (favoriteIds.contains(project.getId())) {
                quickAccessList.add(project);
            }
        }

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
            if (oldSize > 0) recentAdapter.notifyItemRangeRemoved(0, oldSize);
            updateRecentState();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getMyTasks(token).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<RecentItem>> call, @NonNull Response<List<RecentItem>> response) {
                int oldSize = recentList.size();
                recentList.clear();
                if (oldSize > 0) recentAdapter.notifyItemRangeRemoved(0, oldSize);

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
                if (oldSize > 0) recentAdapter.notifyItemRangeRemoved(0, oldSize);
                updateRecentState();
            }
        });
    }

    private void setupPieChart(int done, int inProgress, int todo) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(done, "Done"));
        entries.add(new PieEntry(inProgress, "In Progress"));
        entries.add(new PieEntry(todo, "To Do"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#0FADFF"),
                Color.parseColor("#EFEB3B"),
                Color.parseColor("#48FB98")
        );
        dataSet.setDrawValues(false);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
        pieChart.animateY(1000);
    }

    private void updateProgressCard(DashboardOverview data) {
        int totalTasks = data.getMyTasks();
        int doneTasks = data.getCompletedTasks();
        int inProgress = data.getInProgressTasks();
        int todoTasks = Math.max(totalTasks - doneTasks - inProgress, 0);

        double progress = totalTasks > 0 ? (doneTasks * 100.0 / totalTasks) : 0;

        tvProgressPercent.setText(getString(R.string.progress_percent, progress));
        tvDoneTasks.setText(getString(R.string.tasks_label, doneTasks));
        tvInProgressTasks.setText(getString(R.string.tasks_label, inProgress));
        tvTodoTasks.setText(getString(R.string.tasks_label, todoTasks));
        setupPieChart(doneTasks, inProgress, todoTasks);
    }

    private void updateRecentState() {
        boolean hasItems = !recentList.isEmpty();
        recentLabel.setVisibility(View.VISIBLE);
        rvRecentActivity.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        tvRecentEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void updateProgressCardMock() {
        int doneTasks = 12;
        int inProgress = 5;
        int todoTasks = 8;

        tvProgressPercent.setText(getString(R.string.progress_percent, 40f));
        tvDoneTasks.setText(getString(R.string.tasks_label, doneTasks));
        tvInProgressTasks.setText(getString(R.string.tasks_label, inProgress));
        tvTodoTasks.setText(getString(R.string.tasks_label, todoTasks));
        setupPieChart(doneTasks, inProgress, todoTasks);
    }

    private void loadRecentMock() {
        recentList.clear();

        RecentItem item1 = new RecentItem();
        item1.setTitle("Vẽ Sequence diagram");
        item1.setMessage("Dat updated a story");
        item1.setTicketCode("GGSHOP-3");
        item1.setAvatarText("DA");
        item1.setTimeAgo("2h ago");
        item1.setStatus("InProgress");
        recentList.add(item1);

        RecentItem item2 = new RecentItem();
        item2.setTitle("Vẽ UseCase");
        item2.setMessage("TAnh updated a story");
        item2.setTicketCode("GGSHOP-3");
        item2.setAvatarText("TA");
        item2.setTimeAgo("11h ago");
        item2.setStatus("Done");
        recentList.add(item2);

        RecentItem item3 = new RecentItem();
        item3.setTitle("Phân tích thiết kế");
        item3.setMessage("Quat updated a story");
        item3.setTicketCode("GGSHOP-3");
        item3.setAvatarText("QU");
        item3.setTimeAgo("1d ago");
        item3.setStatus("Done");
        recentList.add(item3);

        RecentItem item4 = new RecentItem();
        item4.setTitle("Vẽ Activity diagram");
        item4.setMessage("Dang updated a story");
        item4.setTicketCode("GGSHOP-3");
        item4.setAvatarText("DG");
        item4.setTimeAgo("4d ago");
        item4.setStatus("Todo");
        recentList.add(item4);

        recentAdapter.notifyDataSetChanged();
        updateRecentState();
    }
}