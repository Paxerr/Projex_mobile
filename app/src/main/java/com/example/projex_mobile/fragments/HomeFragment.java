package com.example.projex_mobile.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import com.example.projex_mobile.objects.Project;
import com.example.projex_mobile.objects.QuickAccessItem;
import com.example.projex_mobile.objects.RecentAccessResponse;
import com.example.projex_mobile.objects.RecentItem;
import com.example.projex_mobile.objects.Task;
import com.example.projex_mobile.objects.TaskResponse;
import com.example.projex_mobile.objects.User;
import com.example.projex_mobile.utils.PrefKeyHelper;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private TextView tvUserName, tvProgressPercent, tvDoneTasks, tvInProgressTasks, tvTodoTasks;
    private TextView tvRecentEmpty;
    private TextView tvAvatarText;
    private ImageView ivAvatar;
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
    private int recentLoadGeneration = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
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
        refreshUserHeader();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvAvatarText = view.findViewById(R.id.tvAvatarText);
        ivAvatar = view.findViewById(R.id.ivAvatar);
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

    private boolean hasSession() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
        return token != null && !token.trim().isEmpty();
    }

    private void loadData() {
        loadQuickAccess();
        refreshUserHeader();

        if (!hasSession()) {
            updateProgressCard(new ArrayList<>());
            clearRecent();
            return;
        }

        loadUserProfile();
        loadRecentAndProgress();
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

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "");
        String avatarUrl = prefs.getString("avatar_url", "");

        if (tvUserName != null) {
            tvUserName.setText(userName.isEmpty() ? "User" : userName);
        }

        if (tvAvatarText != null) {
            tvAvatarText.setText(makeAvatarText(userName));
        }

        setupAvatarImage(avatarUrl);
    }

    private void clearRecent() {
        recentLoadGeneration++;
        recentList.clear();

        if (recentAdapter != null) {
            recentAdapter.notifyDataSetChanged();
        }

        updateRecentState();
    }

    private void loadUserProfile() {
        if (!hasSession()) return;

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String fullName = user.getFullName() != null ? user.getFullName() : "";

                    requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("user_name", fullName)
                            .putString("avatar_url", user.getAvatarUrl() != null ? user.getAvatarUrl() : "")
                            .apply();

                    if (tvUserName != null) {
                        tvUserName.setText(fullName.isEmpty() ? "User" : fullName);
                    }

                    if (tvAvatarText != null) {
                        tvAvatarText.setText(makeAvatarText(fullName));
                    }

                    setupAvatarImage(user.getAvatarUrl());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                refreshUserHeader();
            }
        });
    }

    private void setupAvatarImage(String avatarUrl) {
        View view = getView();
        if (view == null) return;

        FrameLayout cardAvatar = view.findViewById(R.id.cardAvatar);
        if (cardAvatar == null) return;

        if (ivAvatar == null) {
            ivAvatar = view.findViewById(R.id.ivAvatar);
        }

        if (ivAvatar == null) {
            com.google.android.material.imageview.ShapeableImageView shapeableAvatar =
                    new com.google.android.material.imageview.ShapeableImageView(requireContext());
            shapeableAvatar.setId(R.id.ivAvatar);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            shapeableAvatar.setLayoutParams(params);
            shapeableAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            shapeableAvatar.setVisibility(View.GONE);

            float density = getResources().getDisplayMetrics().density;
            int cornerRadiusPx = (int) (24 * density);
            shapeableAvatar.setShapeAppearanceModel(
                    shapeableAvatar.getShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(cornerRadiusPx)
                            .build()
            );

            cardAvatar.addView(shapeableAvatar);
            ivAvatar = shapeableAvatar;
        }

        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            ivAvatar.setVisibility(View.VISIBLE);

            if (tvAvatarText != null) {
                tvAvatarText.setVisibility(View.GONE);
            }

            EditProfileFragment.loadImage(avatarUrl, ivAvatar);
        } else {
            ivAvatar.setVisibility(View.GONE);

            if (tvAvatarText != null) {
                tvAvatarText.setVisibility(View.VISIBLE);
            }
        }
    }

    private String makeAvatarText(String name) {
        if (name == null || name.trim().isEmpty()) return "User";

        String[] words = name.trim().split("\\s+");
        if (words.length == 1) {
            return words[0]
                    .substring(0, Math.min(2, words[0].length()))
                    .toUpperCase(new Locale("vi", "VN"));
        }

        String first = words[0].substring(0, 1);
        String last = words[words.length - 1].substring(0, 1);
        return (first + last).toUpperCase(new Locale("vi", "VN"));
    }

    private void updateProgressCard(List<Task> tasks) {
        int done = 0;
        int inProgress = 0;
        int todo = 0;

        for (Task task : tasks) {
            if (task == null) continue;

            String status = task.getStatus();
            status = status == null ? "" : status.trim();

            if (status.equalsIgnoreCase("Done")) {
                done++;
            } else if (status.equalsIgnoreCase("InProgress")
                    || status.equalsIgnoreCase("In Progress")) {
                inProgress++;
            } else {
                todo++;
            }
        }

        int total = done + inProgress + todo;
        float progressPercent = total == 0 ? 0f : (done * 100f / total);

        tvProgressPercent.setText(String.format(Locale.getDefault(), "%.0f%%", progressPercent));
        tvDoneTasks.setText(getString(R.string.tasks_label, done));
        tvInProgressTasks.setText(getString(R.string.tasks_label, inProgress));
        tvTodoTasks.setText(getString(R.string.tasks_label, todo));

        setupPieChart(done, inProgress, todo);
    }

    private void setupPieChart(int done, int inProgress, int todo) {
        if (pieChart == null) return;

        ArrayList<PieEntry> entries = new ArrayList<>();
        int total = done + inProgress + todo;

        if (total == 0) {
            entries.add(new PieEntry(1f, "No tasks"));
        } else {
            if (done > 0) entries.add(new PieEntry(done, "Done"));
            if (inProgress > 0) entries.add(new PieEntry(inProgress, "In Progress"));
            if (todo > 0) entries.add(new PieEntry(todo, "To Do"));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        if (total == 0) {
            dataSet.setColor(Color.parseColor("#3A3A3A"));
        } else {
            ArrayList<Integer> colors = new ArrayList<>();
            if (done > 0) colors.add(Color.parseColor("#0FADFF"));
            if (inProgress > 0) colors.add(Color.parseColor("#EFEB3B"));
            if (todo > 0) colors.add(Color.parseColor("#48FB98"));
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
                useVerticalQuickAccess ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL,
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
                }
        );
        rvQuickAccess.setAdapter(quickAccessAdapter);

        rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentActivity.setNestedScrollingEnabled(false);
        rvRecentActivity.setHasFixedSize(false);

        recentAdapter = new RecentActivityAdapter(recentList, this::openTaskDetail);
        rvRecentActivity.setAdapter(recentAdapter);
    }

    private void openTaskDetail(RecentItem item) {
        if (item == null) return;

        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("task_id", item.getId());
        bundle.putInt("project_id", item.getProjectId());
        bundle.putString("project_name", item.getProjectName());
        fragment.setArguments(bundle);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit();
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
                isQuickAccessExpanded ? R.drawable.ic_chevron_down : R.drawable.ic_chevron_right
        );
        rvQuickAccess.setVisibility(isQuickAccessExpanded ? View.VISIBLE : View.GONE);

        if (quickAccessSection != null) {
            quickAccessSection.requestLayout();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadQuickAccess() {
        quickAccessList.clear();
        quickAccessList.add(new QuickAccessItem(991, "My Tasks", R.drawable.home_ic_task, "CÁ NHÂN"));

        if (!hasSession()) {
            if (quickAccessAdapter != null) {
                quickAccessAdapter.notifyDataSetChanged();
            }

            if (quickAccessSection != null) {
                quickAccessSection.setVisibility(View.VISIBLE);
            }

            return;
        }

        SharedPreferences spacePrefs = requireContext()
                .getSharedPreferences("space_prefs", Context.MODE_PRIVATE);

        String favJson = spacePrefs.getString(
                PrefKeyHelper.userScopedKey(requireContext(), "favorite_ids"),
                "[]"
        );
        String projectsJson = spacePrefs.getString(
                PrefKeyHelper.userScopedKey(requireContext(), "projects_json"),
                "[]"
        );

        List<Integer> favoriteIds = new ArrayList<>();
        try {
            JSONArray favArr = new JSONArray(favJson);
            for (int i = 0; i < favArr.length(); i++) {
                favoriteIds.add(favArr.getInt(i));
            }
        } catch (JSONException ignored) {
        }

        try {
            JSONArray projectsArr = new JSONArray(projectsJson);
            for (int i = 0; i < projectsArr.length(); i++) {
                JSONObject obj = projectsArr.getJSONObject(i);

                int id = obj.optInt("id", -1);
                String name = obj.optString("name", "").trim();
                int iconRes = obj.optInt("iconRes", R.drawable.ic_logo);

                if (id != -1 && !name.isEmpty() && favoriteIds.contains(id)) {
                    quickAccessList.add(new QuickAccessItem(id, name, iconRes, "DỰ ÁN"));
                }
            }
        } catch (JSONException ignored) {
        }

        if (quickAccessAdapter != null) {
            quickAccessAdapter.notifyDataSetChanged();
        }

        if (quickAccessSection != null) {
            quickAccessSection.setVisibility(View.VISIBLE);
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

    private void updateRecentState() {
        boolean hasItems = !recentList.isEmpty();
        recentLabel.setVisibility(View.VISIBLE);
        rvRecentActivity.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        tvRecentEmpty.setVisibility(hasItems ? View.GONE : View.VISIBLE);
    }

    private void loadRecentAndProgress() {
        if (!hasSession()) {
            updateProgressCard(new ArrayList<>());
            clearRecent();
            return;
        }

        final int loadGeneration = ++recentLoadGeneration;
        ApiService apiService = RetrofitClient.getApiService(token);

        List<RecentAccessResponse> recentAccesses = new ArrayList<>();
        List<Task> myTasks = new ArrayList<>();
        final int[] pendingSources = {2};

        apiService.getRecentAccesses(token).enqueue(new Callback<List<RecentAccessResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<RecentAccessResponse>> call,
                                   @NonNull Response<List<RecentAccessResponse>> response) {
                if (!isActiveRecentLoad(loadGeneration)) return;

                if (response.isSuccessful() && response.body() != null) {
                    recentAccesses.addAll(response.body());
                }

                onRecentSourceDone(apiService, recentAccesses, myTasks, pendingSources, loadGeneration);
            }

            @Override
            public void onFailure(@NonNull Call<List<RecentAccessResponse>> call, @NonNull Throwable t) {
                if (!isActiveRecentLoad(loadGeneration)) return;
                onRecentSourceDone(apiService, recentAccesses, myTasks, pendingSources, loadGeneration);
            }
        });

        apiService.getAllAssignedTasks(token).enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(@NonNull Call<List<Task>> call,
                                   @NonNull Response<List<Task>> response) {
                if (!isActiveRecentLoad(loadGeneration)) return;

                if (response.isSuccessful() && response.body() != null) {
                    myTasks.addAll(response.body());
                }

                updateProgressCard(myTasks);
                onRecentSourceDone(apiService, recentAccesses, myTasks, pendingSources, loadGeneration);
            }

            @Override
            public void onFailure(@NonNull Call<List<Task>> call, @NonNull Throwable t) {
                if (!isActiveRecentLoad(loadGeneration)) return;

                updateProgressCard(new ArrayList<>());
                onRecentSourceDone(apiService, recentAccesses, myTasks, pendingSources, loadGeneration);
            }
        });
    }

    private void onRecentSourceDone(ApiService apiService,
                                    List<RecentAccessResponse> recentAccesses,
                                    List<Task> assignedTasks,
                                    int[] pendingSources,
                                    int loadGeneration) {
        pendingSources[0]--;
        if (pendingSources[0] > 0) return;
        if (!isActiveRecentLoad(loadGeneration)) return;

        LinkedHashMap<Integer, RecentSeed> seedMap = new LinkedHashMap<>();

        for (RecentAccessResponse access : recentAccesses) {
            if (access == null || access.getTaskId() <= 0) continue;
            putRecentSeed(seedMap, access.getTaskId(), null, access.getAccessAt());
        }

        for (Task task : assignedTasks) {
            if (task == null || task.getId() <= 0) continue;
            putRecentSeed(seedMap, task.getId(), task, getLatestTaskActivityAt(task));
        }

        List<RecentSeed> seeds = new ArrayList<>(seedMap.values());
        seeds.sort((a, b) -> Long.compare(parseAccessTime(b.activityAt), parseAccessTime(a.activityAt)));

        if (seeds.isEmpty()) {
            publishRecentItems(new ArrayList<>(), loadGeneration);
            return;
        }

        List<RecentItem> loadedItems = new ArrayList<>();
        final int[] remaining = {seeds.size()};

        for (RecentSeed seed : seeds) {
            if (seed.task != null) {
                buildRecentItem(apiService, seed.task, seed.activityAt, loadedItems, remaining, loadGeneration);
            } else {
                apiService.getTaskById(token, seed.taskId).enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(@NonNull Call<Task> call, @NonNull Response<Task> response) {
                        if (!isActiveRecentLoad(loadGeneration)) return;

                        if (response.isSuccessful() && response.body() != null) {
                            Task task = response.body();
                            String activityAt = latestTime(seed.activityAt, getLatestTaskActivityAt(task));
                            buildRecentItem(apiService, task, activityAt, loadedItems, remaining, loadGeneration);
                        } else {
                            onRecentItemDone(loadedItems, remaining, loadGeneration);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Task> call, @NonNull Throwable t) {
                        if (!isActiveRecentLoad(loadGeneration)) return;
                        onRecentItemDone(loadedItems, remaining, loadGeneration);
                    }
                });
            }
        }
    }

    private void putRecentSeed(LinkedHashMap<Integer, RecentSeed> seedMap,
                               int taskId,
                               Task task,
                               String activityAt) {
        RecentSeed old = seedMap.get(taskId);
        if (old == null) {
            seedMap.put(taskId, new RecentSeed(taskId, task, activityAt));
            return;
        }

        if (task != null && old.task == null) {
            old.task = task;
        }

        old.activityAt = latestTime(old.activityAt, activityAt);
    }

    private void buildRecentItem(ApiService apiService,
                                 Task task,
                                 String activityAt,
                                 List<RecentItem> loadedItems,
                                 int[] remaining,
                                 int loadGeneration) {
        int projectId = task.getProjectId();

        if (projectId <= 0) {
            loadedItems.add(mapTaskToRecentItem(task, activityAt, projectId, "No Project"));
            onRecentItemDone(loadedItems, remaining, loadGeneration);
            return;
        }

        if (task.getProject() != null
                && task.getProject().getName() != null
                && !task.getProject().getName().trim().isEmpty()) {
            loadedItems.add(mapTaskToRecentItem(task, activityAt, projectId, task.getProject().getName()));
            onRecentItemDone(loadedItems, remaining, loadGeneration);
            return;
        }

        apiService.getProjectDetail(token, projectId).enqueue(new Callback<Project>() {
            @Override
            public void onResponse(@NonNull Call<Project> call,
                                   @NonNull Response<Project> response) {
                if (!isActiveRecentLoad(loadGeneration)) return;

                String projectName = "Project " + projectId;
                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getName() != null
                        && !response.body().getName().trim().isEmpty()) {
                    projectName = response.body().getName();
                }

                loadedItems.add(mapTaskToRecentItem(task, activityAt, projectId, projectName));
                onRecentItemDone(loadedItems, remaining, loadGeneration);
            }

            @Override
            public void onFailure(@NonNull Call<Project> call, @NonNull Throwable t) {
                if (!isActiveRecentLoad(loadGeneration)) return;

                loadedItems.add(mapTaskToRecentItem(task, activityAt, projectId, "Project " + projectId));
                onRecentItemDone(loadedItems, remaining, loadGeneration);
            }
        });
    }

    private void onRecentItemDone(List<RecentItem> loadedItems, int[] remaining, int loadGeneration) {
        if (!isActiveRecentLoad(loadGeneration)) return;

        remaining[0]--;
        if (remaining[0] > 0) return;

        loadedItems.sort((o1, o2) ->
                Long.compare(parseAccessTime(o2.getRawAccessAt()), parseAccessTime(o1.getRawAccessAt())));
        publishRecentItems(loadedItems, loadGeneration);
    }

    private void publishRecentItems(List<RecentItem> items, int loadGeneration) {
        if (!isActiveRecentLoad(loadGeneration)) return;

        recentList.clear();
        recentList.addAll(items);

        if (recentAdapter != null) {
            recentAdapter.notifyDataSetChanged();
        }

        updateRecentState();
    }

    private boolean isActiveRecentLoad(int loadGeneration) {
        return isAdded() && loadGeneration == recentLoadGeneration;
    }

    private RecentItem mapTaskToRecentItem(Task task, String activityAt, int projectId, String projectName) {
        RecentItem item = new RecentItem();
        item.setId(task.getId());
        item.setProjectId(projectId);
        item.setTitle(task.getTitle());
        item.setMessage(task.getDescription() != null ? task.getDescription() : "");
        item.setAvatarText(makeAvatarText(task.getTitle()));
        item.setTimeAgo(toRelativeTime(activityAt));
        item.setStatus(task.getStatus());
        item.setProjectName(projectName);
        item.setRawAccessAt(activityAt);
        return item;
    }

    private String toRelativeTime(String activityAt) {
        long time = parseAccessTime(activityAt);
        if (time <= 0L) return "";

        return DateUtils.getRelativeTimeSpanString(
                time,
                System.currentTimeMillis(),
                DateUtils.SECOND_IN_MILLIS
        ).toString();
    }

    private String getLatestTaskActivityAt(Task task) {
        if (task == null) return "";

        String latest = task.getUpdatedAt();
        latest = latestTime(latest, task.getStatusUpdatedAt());
        latest = latestTime(latest, task.getCreatedAt());
        return latest;
    }

    private String latestTime(String first, String second) {
        long firstTime = parseAccessTime(first);
        long secondTime = parseAccessTime(second);

        if (secondTime > firstTime) {
            return second != null ? second : "";
        }

        return first != null ? first : "";
    }

    private long parseAccessTime(String value) {
        if (value == null || value.trim().isEmpty()) return 0L;

        String normalized = normalizeDateValue(value);
        SimpleDateFormat[] formats = new SimpleDateFormat[]{
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US),
                new SimpleDateFormat("yyyy-MM-dd", Locale.US)
        };

        for (SimpleDateFormat sdf : formats) {
            try {
                sdf.setLenient(false);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                java.util.Date date = sdf.parse(normalized);
                if (date != null) return date.getTime();
            } catch (Exception ignored) {
            }
        }

        return 0L;
    }

    private String normalizeDateValue(String value) {
        String normalized = value.trim();

        int dotIndex = normalized.indexOf('.');
        if (dotIndex >= 0) {
            int fractionEnd = dotIndex + 1;
            while (fractionEnd < normalized.length()
                    && Character.isDigit(normalized.charAt(fractionEnd))) {
                fractionEnd++;
            }

            String fraction = normalized.substring(dotIndex + 1, fractionEnd);
            if (fraction.length() > 3) {
                fraction = fraction.substring(0, 3);
            }
            while (fraction.length() < 3) {
                fraction += "0";
            }

            normalized = normalized.substring(0, dotIndex + 1)
                    + fraction
                    + normalized.substring(fractionEnd);
        }

        if (normalized.endsWith("Z")) {
            normalized = normalized.substring(0, normalized.length() - 1) + "+0000";
        } else if (normalized.length() >= 6) {
            int zoneSignIndex = normalized.length() - 6;
            int zoneColonIndex = normalized.length() - 3;
            char zoneSign = normalized.charAt(zoneSignIndex);

            if ((zoneSign == '+' || zoneSign == '-')
                    && normalized.charAt(zoneColonIndex) == ':') {
                normalized = normalized.substring(0, zoneColonIndex)
                        + normalized.substring(zoneColonIndex + 1);
            }
        }

        return normalized;
    }

    private static class RecentSeed {
        int taskId;
        Task task;
        String activityAt;

        RecentSeed(int taskId, Task task, String activityAt) {
            this.taskId = taskId;
            this.task = task;
            this.activityAt = activityAt;
        }
    }
}
