package com.example.projex_mobile.fragments;

import android.annotation.SuppressLint;
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
import android.widget.ImageView;
import android.widget.TextView;

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
import com.example.projex_mobile.objects.QuickAccessItem;
import com.example.projex_mobile.objects.RecentItem;
import com.example.projex_mobile.objects.Task;
import com.example.projex_mobile.objects.TaskResponse;
import com.example.projex_mobile.objects.User;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvProgressPercent, tvDoneTasks, tvInProgressTasks, tvTodoTasks;
    private TextView tvRecentEmpty;
    private TextView tvAvatarText;
    private ImageView ivQuickAccessToggle;
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
    private boolean isQuickAccessExpanded = true;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupSearchBar(view);
        setupRecyclerViews();
        setupQuickAccessToggle();
        setupUserHeader(view);
        loadData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadQuickAccess();
        refreshUserHeader();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvAvatarText = view.findViewById(R.id.tvAvatarText);
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
        ivQuickAccessToggle = view.findViewById(R.id.ivQuickAccessToggle);
    }

    private void setupUserHeader(View view) {
        View userHeader = view.findViewById(R.id.userHeader);
        if (userHeader != null) {
            userHeader.setOnClickListener(v -> openAccountFragment());
        }
    }

    private void openAccountFragment() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, new AccountFragment())
                .addToBackStack(null)
                .commit();
    }

    private void refreshUserHeader() {
        if (!isAdded()) return;
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "");

        if (tvUserName != null) {
            tvUserName.setText(userName.isEmpty() ? "User" : userName);
        }

        if (tvAvatarText != null) {
            tvAvatarText.setText(makeAvatarText(userName));
        }
    }

    private void loadData() {
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");

        loadQuickAccess();
        loadMyTasksProgress();
        loadRecentMock();
    }

    private void loadUserProfile() {
        if (token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call,
                                   @NonNull Response<User> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String fullName = user.getFullName() != null ? user.getFullName() : "";

                    requireActivity()
                            .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("user_name", fullName)
                            .apply();

                    if (tvUserName != null) {
                        tvUserName.setText(fullName.isEmpty() ? "User" : fullName);
                    }
                    if (tvAvatarText != null) {
                        tvAvatarText.setText(makeAvatarText(fullName));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                refreshUserHeader();
            }
        });
    }

    private String makeAvatarText(String name) {
        if (name == null || name.trim().isEmpty()) return "User";
        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length()))
                    .toUpperCase(new Locale("vi", "VN"));
        }
        String first = words[0].substring(0, 1);
        String last = words[words.length - 1].substring(0, 1);
        return (first + last).toUpperCase(new Locale("vi", "VN"));
    }

    private void loadMyTasksProgress() {
        if (token == null || token.isEmpty()) {
            updateProgressCard(new ArrayList<>());
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.getAssignedTasks(token).enqueue(new Callback<TaskResponse>() {
            @Override
            public void onResponse(@NonNull Call<TaskResponse> call,
                                   @NonNull Response<TaskResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getItems() != null) {
                    updateProgressCard(response.body().getItems());
                } else {
                    updateProgressCard(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<TaskResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                updateProgressCard(new ArrayList<>());
            }
        });
    }

    private void updateProgressCard(List<Task> tasks) {
        int done = 0;
        int inProgress = 0;
        int assigned = 0;

        for (Task task : tasks) {
            String status = task.getStatus();
            if (status == null) {
                assigned++;
            } else if ("Done".equalsIgnoreCase(status)) {
                done++;
            } else if ("InProgress".equalsIgnoreCase(status)
                    || "In Progress".equalsIgnoreCase(status)) {
                inProgress++;
            } else {
                assigned++;
            }
        }

        int total = done + inProgress + assigned;
        float progressPercent = total == 0 ? 0f : (done * 100f / total);

        tvProgressPercent.setText(getString(R.string.progress_percent, progressPercent));
        tvDoneTasks.setText(getString(R.string.tasks_label, done));
        tvInProgressTasks.setText(getString(R.string.tasks_label, inProgress));
        tvTodoTasks.setText(getString(R.string.tasks_label, assigned));

        setupPieChart(done, inProgress, assigned);
    }

    private void setupPieChart(int done, int inProgress, int assigned) {
        if (pieChart == null) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        int total = done + inProgress + assigned;

        if (total == 0) {
            entries.add(new PieEntry(1f, "No tasks"));
        } else {
            if (done > 0) entries.add(new PieEntry(done, "Done"));
            if (inProgress > 0) entries.add(new PieEntry(inProgress, "In Progress"));
            if (assigned > 0) entries.add(new PieEntry(assigned, "Assigned"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        if (total == 0) {
            dataSet.setColor(Color.parseColor("#3A3A3A"));
        } else {
            ArrayList<Integer> colors = new ArrayList<>();
            if (done > 0) colors.add(Color.parseColor("#0FADFF"));
            if (inProgress > 0) colors.add(Color.parseColor("#EFEB3B"));
            if (assigned > 0) colors.add(Color.parseColor("#48FB98"));
            dataSet.setColors(colors);
        }

        dataSet.setDrawValues(false);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.getLegend().setEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(false);
        pieChart.setHoleRadius(58f);
        pieChart.setTransparentCircleRadius(62f);
        pieChart.setCenterText("");
        pieChart.invalidate();
        pieChart.animateY(1000);
    }

    private void setupRecyclerViews() {
        int orientation = getResources().getConfiguration().orientation;
        int swDp = getResources().getConfiguration().smallestScreenWidthDp;
        boolean isTablet = swDp >= 600;
        boolean useVerticalQuickAccess = isTablet
                && orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE;

        rvQuickAccess.setLayoutManager(new LinearLayoutManager(
                requireContext(),
                useVerticalQuickAccess
                        ? LinearLayoutManager.VERTICAL
                        : LinearLayoutManager.HORIZONTAL,
                false
        ));

        quickAccessAdapter = new QuickAccessAdapter(
                quickAccessList,
                useVerticalQuickAccess ? 1 : 0,
                item -> {
                    if ("My Tasks".equals(item.getName())) {
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(R.id.frame_container, new TaskFragment())
                                .addToBackStack(null)
                                .commit();
                    } else {
                        ProjectFragment projectFragment = new ProjectFragment();
                        Bundle bundle = new Bundle();
                        bundle.putInt("project_id", item.getId());
                        bundle.putString("project_name", item.getName());
                        projectFragment.setArguments(bundle);
                        requireActivity().getSupportFragmentManager()
                                .beginTransaction()
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
        ivQuickAccessToggle.setOnClickListener(v -> {
            isQuickAccessExpanded = !isQuickAccessExpanded;
            updateQuickAccessState();
        });
    }

    private void updateQuickAccessState() {
        ivQuickAccessToggle.setImageResource(
                isQuickAccessExpanded
                        ? R.drawable.ic_chevron_down
                        : R.drawable.ic_chevron_right
        );
        rvQuickAccess.setVisibility(isQuickAccessExpanded ? View.VISIBLE : View.GONE);
        if (quickAccessSection != null) quickAccessSection.requestLayout();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadQuickAccess() {
        int oldSize = quickAccessList.size();
        if (oldSize > 0) {
            quickAccessList.clear();
            quickAccessAdapter.notifyItemRangeRemoved(0, oldSize);
        }

        quickAccessList.add(new QuickAccessItem(991, "My Task", R.drawable.home_ic_task, "CÁ NHÂN"));

        SharedPreferences spacePrefs = requireContext()
                .getSharedPreferences("space_prefs", Context.MODE_PRIVATE);
        String favJson = spacePrefs.getString("favorite_ids", "[]");
        String projectsJson = spacePrefs.getString("projects_json", "[]");

        List<Integer> favoriteIds = new ArrayList<>();
        try {
            JSONArray favArr = new JSONArray(favJson);
            for (int i = 0; i < favArr.length(); i++) favoriteIds.add(favArr.getInt(i));
        } catch (JSONException ignored) {}

        try {
            JSONArray projectsArr = new JSONArray(projectsJson);
            for (int i = 0; i < projectsArr.length(); i++) {
                JSONObject obj = projectsArr.getJSONObject(i);
                int id = obj.optInt("id");
                String name = obj.optString("name", "");
                int iconRes = obj.optInt("iconRes", R.drawable.ic_logo);
                if (favoriteIds.contains(id)) {
                    quickAccessList.add(new QuickAccessItem(id, name, iconRes, "DỰ ÁN"));
                }
            }
        } catch (JSONException ignored) {}

        quickAccessAdapter.notifyDataSetChanged();
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) searchLayout.setHint(null);
                else if (!edtSearch.hasFocus()) searchLayout.setHint("Tìm kiếm");
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

    private void updateRecentState() {
        boolean hasItems = !recentList.isEmpty();
        recentLabel.setVisibility(View.VISIBLE);
        rvRecentActivity.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        tvRecentEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
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
        item4.setStatus("Assigned");
        recentList.add(item4);

        recentAdapter.notifyDataSetChanged();
        updateRecentState();
    }
}