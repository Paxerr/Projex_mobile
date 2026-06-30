package com.example.projex_mobile.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.SpaceCreateActivity;
import com.example.projex_mobile.adapter.ProjectAdapter;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.ProjectItem;
import com.example.projex_mobile.utils.PrefKeyHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SpaceListFragment extends Fragment implements ProjectAdapter.OnProjectClickListener {

    private RecyclerView rvProjects;
    private EditText edtSearch;
    private FrameLayout btnAddSpace;
    private TextView tvAll, tvRecent, tvFavorite, tvEmpty;

    private ProjectAdapter adapter;
    private final List<ProjectItem> allProjects = new ArrayList<>();
    private final List<ProjectItem> filteredProjects = new ArrayList<>();

    private TextView btnPrevPage, btnNextPage, tvPageInfo;
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;

    private SharedPreferences prefs;
    private final List<Integer> favoriteIds = new ArrayList<>();
    private int recentProjectId = -1;
    private String currentTab = "ALL";
    private String token;

    private ActivityResultLauncher<Intent> createSpaceLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createSpaceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;

                    String name = result.getData().getStringExtra("space_name");
                    String desc = result.getData().getStringExtra("space_desc");
                    String startDate = result.getData().getStringExtra("start_date");
                    String endDate = result.getData().getStringExtra("end_date");

                    if (name != null && !name.trim().isEmpty()) {
                        createProject(name.trim(), desc, startDate, endDate);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.space_list_fragment, container, false);

        prefs = requireContext().getSharedPreferences("space_prefs", Context.MODE_PRIVATE);
        SharedPreferences userPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = userPrefs.getString("token", "");

        rvProjects = view.findViewById(R.id.rvProjects);
        edtSearch = view.findViewById(R.id.edtSearch);
        btnAddSpace = view.findViewById(R.id.btnAddSpace);
        tvAll = view.findViewById(R.id.tvAll);
        tvRecent = view.findViewById(R.id.tvRecent);
        tvFavorite = view.findViewById(R.id.tvFavorite);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);

        setupRecyclerView();
        setupListeners();
        loadPrefs();
        loadProjectsFromApi();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPrefs();

        for (ProjectItem project : allProjects) {
            project.setFavorite(favoriteIds.contains(project.getId()));
        }

        applyFilter();
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(this);
        rvProjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProjects.setAdapter(adapter);
        rvProjects.setHasFixedSize(true);
    }

    private void setupListeners() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentPage = 1;
                loadProjectsFromApi();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddSpace.setOnClickListener(v -> openCreateSpace());

        tvAll.setOnClickListener(v -> {
            currentTab = "ALL";
            updateTabUI();
            currentPage = 1;
            loadProjectsFromApi();
        });

        tvRecent.setOnClickListener(v -> {
            currentTab = "RECENT";
            updateTabUI();
            currentPage = 1;
            loadProjectsFromApi();
        });

        tvFavorite.setOnClickListener(v -> {
            currentTab = "FAVORITE";
            updateTabUI();
            currentPage = 1;
            loadProjectsFromApi();
        });

        if (btnPrevPage != null) {
            btnPrevPage.setOnClickListener(v -> {
                if (!isLoading && currentPage > 1) {
                    currentPage--;
                    loadProjectsFromApi();
                }
            });
        }

        if (btnNextPage != null) {
            btnNextPage.setOnClickListener(v -> {
                if (!isLoading && currentPage < totalPages) {
                    currentPage++;
                    loadProjectsFromApi();
                }
            });
        }

        updateTabUI();
        updatePaginationUi();
    }

    private void openCreateSpace() {
        Intent intent = new Intent(requireContext(), SpaceCreateActivity.class);
        createSpaceLauncher.launch(intent);
    }

    private void loadProjectsFromApi() {
        if (token == null || token.isEmpty()) {
            showToast("Thiếu token đăng nhập");
            return;
        }

        isLoading = true;
        updatePaginationUi();

        String keyword = edtSearch.getText() != null ? edtSearch.getText().toString().trim() : null;
        if (keyword != null && keyword.isEmpty()) keyword = null;

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getProjects(token, currentPage, PAGE_SIZE, keyword, null, null, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;
                        isLoading = false;

                        if (!response.isSuccessful() || response.body() == null) {
                            showToast("Không tải được danh sách project");
                            updatePaginationUi();
                            return;
                        }

                        parseProjectsResponse(response.body());
                        applyFilter();
                        updatePaginationUi();
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            isLoading = false;
                            updatePaginationUi();
                            showToast("Lỗi kết nối: " + t.getMessage());
                        }
                    }
                });
    }

    private void parseProjectsResponse(JsonObject body) {
        allProjects.clear();

        if (body.has("page") && !body.get("page").isJsonNull()) {
            currentPage = body.get("page").getAsInt();
        }

        if (body.has("totalPages") && !body.get("totalPages").isJsonNull()) {
            totalPages = body.get("totalPages").getAsInt();
        } else if (body.has("totalItems") && !body.get("totalItems").isJsonNull()) {
            int totalItems = body.get("totalItems").getAsInt();
            totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);
        } else {
            totalPages = 1;
        }
        if (totalPages < 1) totalPages = 1;

        JsonArray items = null;
        if (body.has("items") && body.get("items").isJsonArray()) {
            items = body.getAsJsonArray("items");
        } else if (body.has("data") && body.get("data").isJsonArray()) {
            items = body.getAsJsonArray("data");
        }

        if (items == null) {
            saveProjectsForQuickAccess();
            return;
        }

        for (JsonElement element : items) {
            if (!element.isJsonObject()) continue;

            JsonObject obj = element.getAsJsonObject();

            int id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsInt() : 0;
            String name = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "";
            String status = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "Active";
            int memberCount = obj.has("memberCount") && !obj.get("memberCount").isJsonNull() ? obj.get("memberCount").getAsInt() : 0;

            ProjectItem item = new ProjectItem(
                    id,
                    name,
                    status,
                    memberCount,
                    favoriteIds.contains(id)
            );

            allProjects.add(item);
        }

        saveProjectsForQuickAccess();
    }

    private void createProject(String name, String description, String startDate, String endDate) {
        if (token == null || token.isEmpty()) {
            showToast("Thiếu token đăng nhập");
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("description", description);

        if (startDate != null && !startDate.isEmpty()) body.put("startDate", startDate);
        if (endDate != null && !endDate.isEmpty()) body.put("endDate", endDate);

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.createProject(token, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    showToast("Tạo project thành công");
                    loadProjectsFromApi();
                } else {
                    String errorMessage = "Tạo project thất bại (" + response.code() + ")";
                    showToast(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showToast("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void loadPrefs() {
        favoriteIds.clear();

        String favJson = prefs.getString(
                PrefKeyHelper.userScopedKey(requireContext(), "favorite_ids"),
                "[]"
        );

        try {
            JSONArray arr = new JSONArray(favJson);
            for (int i = 0; i < arr.length(); i++) {
                favoriteIds.add(arr.getInt(i));
            }
        } catch (JSONException ignored) {
        }

        recentProjectId = prefs.getInt(
                PrefKeyHelper.userScopedKey(requireContext(), "recent_project_id"),
                -1
        );
    }

    private void saveFavorites() {
        JSONArray arr = new JSONArray();

        for (Integer id : favoriteIds) {
            arr.put(id);
        }

        prefs.edit()
                .putString(PrefKeyHelper.userScopedKey(requireContext(), "favorite_ids"), arr.toString())
                .apply();
    }

    private void saveRecent(int projectId) {
        prefs.edit()
                .putInt(PrefKeyHelper.userScopedKey(requireContext(), "recent_project_id"), projectId)
                .apply();
    }

    private void saveProjectsForQuickAccess() {
        JSONArray arr = new JSONArray();

        for (ProjectItem item : allProjects) {
            JSONObject obj = new JSONObject();

            try {
                obj.put("id", item.getId());
                obj.put("name", item.getName());
                arr.put(obj);
            } catch (JSONException ignored) {
            }
        }

        prefs.edit()
                .putString(PrefKeyHelper.userScopedKey(requireContext(), "projects_json"), arr.toString())
                .apply();
    }

    private void updateTabUI() {
        tvAll.setTextColor(currentTab.equals("ALL") ? Color.parseColor("#85ADFF") : Color.parseColor("#9CA3AF"));
        tvRecent.setTextColor(currentTab.equals("RECENT") ? Color.parseColor("#85ADFF") : Color.parseColor("#9CA3AF"));
        tvFavorite.setTextColor(currentTab.equals("FAVORITE") ? Color.parseColor("#85ADFF") : Color.parseColor("#9CA3AF"));
    }

    private void applyFilter() {
        String keyword = edtSearch.getText() != null ? edtSearch.getText().toString().trim().toLowerCase() : "";
        filteredProjects.clear();

        if ("RECENT".equals(currentTab)) {
            ProjectItem recent = findRecentProject();
            if (recent != null && matchesSearch(recent, keyword)) {
                recent.setFavorite(favoriteIds.contains(recent.getId()));
                filteredProjects.add(recent);
            }
            renderProjects();
            tvEmpty.setText("Chưa có hoạt động nào");
            return;
        }

        for (ProjectItem item : allProjects) {
            boolean matchesTab = !"FAVORITE".equals(currentTab) || favoriteIds.contains(item.getId());
            if (matchesTab && matchesSearch(item, keyword)) {
                item.setFavorite(favoriteIds.contains(item.getId()));
                filteredProjects.add(item);
            }
        }

        renderProjects();
        tvEmpty.setText("Chưa có hoạt động nào");
    }

    private ProjectItem findRecentProject() {
        for (ProjectItem item : allProjects) {
            if (item.getId() == recentProjectId) {
                return item;
            }
        }
        return null;
    }

    private boolean matchesSearch(ProjectItem item, String keyword) {
        if (keyword.isEmpty()) return true;

        String name = item.getName() != null ? item.getName().toLowerCase() : "";
        String status = item.getStatus() != null ? item.getStatus().toLowerCase() : "";
        return name.contains(keyword) || status.contains(keyword);
    }

    private void renderProjects() {
        adapter.setData(filteredProjects);
        tvEmpty.setVisibility(filteredProjects.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private String getProjectInitial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";

        String[] parts = name.trim().split("[\\s_\\-]+");
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (sb.length() == 2) break;
            }
        }

        if (sb.length() == 0) {
            char first = Character.toUpperCase(name.trim().charAt(0));
            sb.append(first);
        }

        return sb.toString();
    }

    private void showToast(String message) {
        if (isAdded()) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onProjectClick(ProjectItem item) {
        saveRecent(item.getId());
        recentProjectId = item.getId();

        ProjectFragment fragment = new ProjectFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("project_id", item.getId());
        bundle.putString("project_name", item.getName());
        bundle.putString("project_initial", getProjectInitial(item.getName()));
        fragment.setArguments(bundle);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onFavoriteClick(ProjectItem item) {
        int projectId = item.getId();

        boolean willBeFavorite;

        if (favoriteIds.contains(projectId)) {
            favoriteIds.remove((Integer) projectId);
            willBeFavorite = false;
        } else {
            favoriteIds.add(projectId);
            willBeFavorite = true;
        }

        item.setFavorite(willBeFavorite);

        for (ProjectItem project : allProjects) {
            if (project.getId() == projectId) {
                project.setFavorite(willBeFavorite);
                break;
            }
        }

        saveFavorites();

        if ("FAVORITE".equals(currentTab) && !willBeFavorite) {
            filteredProjects.remove(item);
            adapter.setData(filteredProjects);
            tvEmpty.setVisibility(filteredProjects.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            applyFilter();
        }
    }

    private void updatePaginationUi() {
        if (tvPageInfo != null) {
            tvPageInfo.setText("Trang " + currentPage + "/" + totalPages);
        }

        setPaginationButtonState(btnPrevPage, !isLoading && currentPage > 1);
        setPaginationButtonState(btnNextPage, !isLoading && currentPage < totalPages);
    }

    private void setPaginationButtonState(TextView button, boolean enabled) {
        if (button == null) {
            return;
        }
        button.setEnabled(enabled);
        button.setClickable(enabled);
        button.setAlpha(enabled ? 1f : 0.45f);
    }
}