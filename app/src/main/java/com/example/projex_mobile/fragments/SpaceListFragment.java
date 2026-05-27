package com.example.projex_mobile.fragments;

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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;

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

    private SharedPreferences prefs;
    private List<Integer> favoriteIds = new ArrayList<>();
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
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        String name = result.getData().getStringExtra("space_name");
                        String code = result.getData().getStringExtra("space_code");
                        String desc = result.getData().getStringExtra("space_desc");

                        if (name != null && !name.trim().isEmpty()) {
                            createProject(name, code, desc);
                        }
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

        loadPrefs();
        setupRecyclerView();
        setupListeners();
        loadProjectsFromApi();

        return view;
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
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { applyFilter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnAddSpace.setOnClickListener(v -> openCreateSpace());

        tvAll.setOnClickListener(v -> {
            currentTab = "ALL";
            updateTabUI();
            applyFilter();
        });

        tvRecent.setOnClickListener(v -> {
            currentTab = "RECENT";
            updateTabUI();
            applyFilter();
        });

        tvFavorite.setOnClickListener(v -> {
            currentTab = "FAVORITE";
            updateTabUI();
            applyFilter();
        });

        updateTabUI();
    }

    private void openCreateSpace() {
        Intent intent = new Intent(requireContext(), SpaceCreateActivity.class);
        createSpaceLauncher.launch(intent);
    }

    private void loadProjectsFromApi() {
        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Thiếu token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getProjects(
                token,
                1,
                100,
                null,
                null,
                null,
                null
        ).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(requireContext(), "Không tải được danh sách project", Toast.LENGTH_SHORT).show();
                    return;
                }

                parseProjectsResponse(response.body());
                applyFilter();
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void parseProjectsResponse(JsonObject body) {
        allProjects.clear();

        JsonArray items = null;
        if (body.has("items") && body.get("items").isJsonArray()) {
            items = body.getAsJsonArray("items");
        } else if (body.has("data") && body.get("data").isJsonArray()) {
            items = body.getAsJsonArray("data");
        }

        if (items == null) {
            return;
        }

        for (JsonElement element : items) {
            if (!element.isJsonObject()) continue;

            JsonObject obj = element.getAsJsonObject();

            int id = obj.has("id") ? obj.get("id").getAsInt() : 0;
            String name = obj.has("name") && !obj.get("name").isJsonNull() ? obj.get("name").getAsString() : "";
            String status = obj.has("status") && !obj.get("status").isJsonNull() ? obj.get("status").getAsString() : "Active";
            int memberCount = obj.has("memberCount") ? obj.get("memberCount").getAsInt() : 0;

            int imageRes = R.drawable.ic_logo;
            if (name.toLowerCase().contains("ggshop")) {
                imageRes = R.drawable.login_logo_github;
            } else if (name.toLowerCase().contains("projex")) {
                imageRes = R.drawable.login_logo_pj_rm_bg;
            } else if (name.toLowerCase().contains("web")) {
                imageRes = R.drawable.logo_library_web;
            }

            ProjectItem item = new ProjectItem(
                    id,
                    name,
                    status,
                    memberCount,
                    favoriteIds.contains(id),
                    imageRes
            );
            allProjects.add(item);
        }
    }

    private void createProject(String name, String code, String desc) {
        if (token == null || token.isEmpty()) return;

        Map<String, Object> body = new HashMap<>();
        body.put("name", name.trim());
        body.put("code", code == null ? "" : code.trim());
        body.put("description", desc == null ? "" : desc.trim());
        body.put("status", "Active");

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.createProject(token, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Tạo project thành công", Toast.LENGTH_SHORT).show();
                    loadProjectsFromApi();
                } else {
                    Toast.makeText(requireContext(), "Tạo project thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isFavorite(int id) {
        return favoriteIds.contains(id);
    }

    private void loadPrefs() {
        favoriteIds.clear();
        String favJson = prefs.getString("favorite_ids", "[]");
        try {
            JSONArray arr = new JSONArray(favJson);
            for (int i = 0; i < arr.length(); i++) {
                favoriteIds.add(arr.getInt(i));
            }
        } catch (JSONException ignored) {}
        recentProjectId = prefs.getInt("recent_project_id", -1);
    }

    private void saveFavorites() {
        JSONArray arr = new JSONArray();
        for (Integer id : favoriteIds) arr.put(id);
        prefs.edit().putString("favorite_ids", arr.toString()).apply();
    }

    private void saveRecent(int projectId) {
        prefs.edit().putInt("recent_project_id", projectId).apply();
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
            ProjectItem recent = null;
            for (ProjectItem item : allProjects) {
                if (item.getId() == recentProjectId) {
                    recent = item;
                    break;
                }
            }

            if (recent != null) {
                if (keyword.isEmpty()
                        || (recent.getName() != null && recent.getName().toLowerCase().contains(keyword))
                        || (recent.getStatus() != null && recent.getStatus().toLowerCase().contains(keyword))) {
                    recent.setFavorite(favoriteIds.contains(recent.getId()));
                    filteredProjects.add(recent);
                }
            }

            adapter.setData(filteredProjects);
            tvEmpty.setVisibility(filteredProjects.isEmpty() ? View.VISIBLE : View.GONE);
            tvEmpty.setText("Chưa có hoạt động nào");
            return;
        }

        for (ProjectItem item : allProjects) {
            boolean matchesKeyword =
                    keyword.isEmpty()
                            || (item.getName() != null && item.getName().toLowerCase().contains(keyword))
                            || (item.getStatus() != null && item.getStatus().toLowerCase().contains(keyword));

            boolean matchesTab = true;
            if ("FAVORITE".equals(currentTab)) {
                matchesTab = favoriteIds.contains(item.getId());
            }

            if (matchesKeyword && matchesTab) {
                item.setFavorite(favoriteIds.contains(item.getId()));
                filteredProjects.add(item);
            }
        }

        adapter.setData(filteredProjects);
        tvEmpty.setVisibility(filteredProjects.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setText("Chưa có hoạt động nào");
    }

    @Override
    public void onProjectClick(ProjectItem item) {
        saveRecent(item.getId());
        recentProjectId = item.getId();

        ProjectFragment fragment = new ProjectFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("project_id", item.getId());
        bundle.putString("project_name", item.getName());
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
        if (favoriteIds.contains(item.getId())) {
            favoriteIds.remove((Integer) item.getId());
        } else {
            favoriteIds.add(item.getId());
        }
        saveFavorites();
        item.setFavorite(favoriteIds.contains(item.getId()));
        applyFilter();
    }
}