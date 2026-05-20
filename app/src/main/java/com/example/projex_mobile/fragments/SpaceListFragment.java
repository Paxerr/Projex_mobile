package com.example.projex_mobile.fragments;

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
import com.example.projex_mobile.objects.ProjectItem;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

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

    private ActivityResultLauncher<Intent> createSpaceLauncher;

    public SpaceListFragment() {
        super(R.layout.space_list_fragment);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createSpaceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        String name = result.getData().getStringExtra("space_name");
                        if (name != null && !name.trim().isEmpty()) {
                            addNewProjectFromCreateResult(result.getData());
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

        prefs = requireContext().getSharedPreferences("space_prefs", 0);

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
        loadMockData();
        applyFilter();

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

    private void addNewProjectFromCreateResult(Intent data) {
        String name = data.getStringExtra("space_name");
        String managerName = data.getStringExtra("manager_name");
        String desc = data.getStringExtra("space_desc");

        int newId = allProjects.isEmpty() ? 1 : allProjects.get(allProjects.size() - 1).getId() + 1;

        ProjectItem newItem = new ProjectItem(
                newId,
                name != null ? name.trim() : "",
                "Active",
                1,
                false,
                R.drawable.ic_logo
        );

        allProjects.add(0, newItem);
        recentProjectId = newId;
        saveRecent(newId);
        currentTab = "ALL";
        updateTabUI();
        applyFilter();

        Toast.makeText(requireContext(), "Đã tạo không gian: " + newItem.getName(), Toast.LENGTH_SHORT).show();
    }

    private void loadMockData() {
        allProjects.add(new ProjectItem(1, "GGshop", "Active", 5, isFavorite(1), R.drawable.login_logo_github));
        allProjects.add(new ProjectItem(2, "gg_projex", "Active", 8, isFavorite(2), R.drawable.login_logo_pj_rm_bg));
        allProjects.add(new ProjectItem(3, "GGweb", "Done", 3, isFavorite(3), R.drawable.logo_library_web));
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
        Toast.makeText(requireContext(), item.getName(), Toast.LENGTH_SHORT).show();
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