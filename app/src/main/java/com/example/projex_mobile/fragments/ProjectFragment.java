package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.adapter.TaskAdapter;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.Task;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
public class ProjectFragment extends Fragment {

    private int projectId;
    private RecyclerView rvUpdatesTask;
    private TaskAdapter adapter;
    private List<Task> originalList = new ArrayList<>();
    private List<Task> filteredList = new ArrayList<>();

    private TextView txtProjectName;
    private String projectName = "";
    private TextView txtMemberCount;
    private TextView txtRemainingTime;
    private TextView txtDoneTasks;
    private TextView txtInProgressTasks;
    private TextView txtAssignedTasks;
    private TextView tvProgressPercent;
    private PieChart pieChart;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.project_fragment, container, false);
        rvUpdatesTask = view.findViewById(R.id.rvUpdatesTask);
        txtProjectName = view.findViewById(R.id.txtProjectName);
        txtMemberCount = view.findViewById(R.id.txtCount);
        txtRemainingTime = view.findViewById(R.id.txtTime);
        txtDoneTasks = view.findViewById(R.id.txtDoneTasks);
        txtInProgressTasks = view.findViewById(R.id.txtInProgressTasks);
        txtAssignedTasks = view.findViewById(R.id.txtAssignedTasks);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        pieChart = view.findViewById(R.id.pieChart);

        rvUpdatesTask.setLayoutManager(new LinearLayoutManager(requireContext()));

        if(getArguments() != null){
            projectId = getArguments().getInt("project_id");
            projectName = getArguments().getString("project_name", "");
        }
        adapter = new TaskAdapter(filteredList, task -> {

            TaskDetailFragment fragment = new TaskDetailFragment();

            Bundle bundle = new Bundle();

            bundle.putInt("task_id", task.getId());
            bundle.putInt("project_id", projectId);
            bundle.putString("project_name", projectName);


            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .addToBackStack(null)
                    .commit();
        },projectName);

        rvUpdatesTask.setAdapter(adapter);

        setupPieChart(0, 0, 0);

        loadProject();
        loadLatestTasks();

        return view;
    }

    private void loadProject(){

        SharedPreferences prefs = requireActivity().getSharedPreferences(
                        "user_prefs",
                        Context.MODE_PRIVATE
                );

        String token = prefs.getString("token", "");

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getProjectById(token, projectId)
                .enqueue(new Callback<JsonObject>() {

                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {

                        if(response.isSuccessful() && response.body() != null){

                            JsonObject body = response.body();

                            String name = body.has("name") ? body.get("name").getAsString() : "";



                            int memberCount = 0;

                            if(body.has("members") && body.get("members").isJsonArray()){
                                memberCount = body.getAsJsonArray("members").size();
                            }

                            if(txtProjectName != null){
                                txtProjectName.setText(projectName);
                            }

                            txtMemberCount.setText(
                                    memberCount + " Members"
                            );

                            if (!body.has("endDate") || body.get("endDate").isJsonNull()) {

                                txtRemainingTime.setText("No time limit");

                            } else {

                                String endDate =
                                        body.get("endDate").getAsString();

                                try {

                                    SimpleDateFormat sdf = new SimpleDateFormat(
                                            "yyyy-MM-dd'T'HH:mm:ss",
                                            Locale.getDefault()
                                    );

                                    Date end = sdf.parse(endDate);

                                    Date now = new Date();

                                    long diff = end.getTime() - now.getTime();

                                    long days = TimeUnit.MILLISECONDS.toDays(diff);

                                    if (days > 0) {
                                        txtRemainingTime.setText(
                                                days + " days left"
                                        );
                                    } else {
                                        txtRemainingTime.setText(
                                                "Expired"
                                        );
                                    }

                                } catch (Exception e) {
                                    txtRemainingTime.setText(
                                            "Unknown"
                                    );
                                }
                            }


                        }else{
                            Log.e("PROJECT",
                                    "Load failed");
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t)
                    {

                        Log.e("PROJECT",
                                t.getMessage());
                    }
                });
    }


    private void openTeamFragment() {
        TeamFragment teamFragment = TeamFragment.newInstance(projectId);

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(
                        R.id.frame_container,
                        teamFragment
                )
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener(
                "task_changed",
                getViewLifecycleOwner(),
                (requestKey, result) -> loadLatestTasks()
        );

        LinearLayout btnProTask = view.findViewById(R.id.btnpro_task);

        btnProTask.setOnClickListener(v -> {

            ProTaskFragment fragment = new ProTaskFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("project_id", projectId);
            bundle.putString("project_name", txtProjectName.getText().toString());

            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(
                            R.id.frame_container,
                            fragment
                    )
                    .addToBackStack(null)
                    .commit();
        });

        LinearLayout btnTeam = view.findViewById(R.id.btnteam);
        btnTeam.setOnClickListener(v -> openTeamFragment());

        if (txtMemberCount != null) {
            txtMemberCount.setOnClickListener(v -> openTeamFragment());
        }

    }

    private void setupPieChart(int done, int inProgress, int assigned) {
        if (pieChart == null) {
            return;
        }

        ArrayList<PieEntry> entries = new ArrayList<>();
        int totalTasks = done + inProgress + assigned;

        if (totalTasks == 0) {
            entries.add(new PieEntry(1f, "No tasks"));
        } else {
            if (done > 0) {
                entries.add(new PieEntry(done, "Done"));
            }
            if (inProgress > 0) {
                entries.add(new PieEntry(inProgress, "In Progress"));
            }
            if (assigned > 0) {
                entries.add(new PieEntry(assigned, "Assigned"));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        if (totalTasks == 0) {
            dataSet.setColor(Color.parseColor("#3A3A3A"));
        } else {
            ArrayList<Integer> colors = new ArrayList<>();
            if (done > 0) {
                colors.add(Color.parseColor("#0FADFF"));
            }
            if (inProgress > 0) {
                colors.add(Color.parseColor("#EFEB3B"));
            }
            if (assigned > 0) {
                colors.add(Color.parseColor("#48FB98"));
            }
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

    private void updateChartFromTasks(List<Task> tasks) {
        int done = 0;
        int inProgress = 0;
        int assigned = 0;

        if (tasks != null) {
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
        }

        txtDoneTasks.setText(getString(R.string.tasks_label, done));
        txtInProgressTasks.setText(getString(R.string.tasks_label, inProgress));
        txtAssignedTasks.setText(getString(R.string.tasks_label, assigned));
        int totalTasks = done + inProgress + assigned;
        float progressPercent = totalTasks == 0 ? 0f : (done * 100f / totalTasks);
        tvProgressPercent.setText(getString(R.string.progress_percent, progressPercent));
        setupPieChart(done, inProgress, assigned);
    }

    private void showLatestUpdatedTasks() {

        filteredList.clear();

        if (originalList == null || originalList.isEmpty()) {
            adapter.notifyDataSetChanged();
            return;
        }

        List<Task> tempList = new ArrayList<>(originalList);

        tempList.sort((t1, t2) -> {

            String d1 = t1.getUpdatedAt();
            String d2 = t2.getUpdatedAt();

            if (d1 == null) return 1;
            if (d2 == null) return -1;

            return d2.compareTo(d1);
        });

        int limit = Math.min(3, tempList.size());

        for (int i = 0; i < limit; i++) {
            filteredList.add(tempList.get(i));
        }

        adapter.notifyDataSetChanged();
    }
    private void loadLatestTasks() {

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences(
                        "user_prefs",
                        Context.MODE_PRIVATE
                );

        String token = prefs.getString("token", "");

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getAllTasksByProject(token, projectId)
                .enqueue(new Callback<List<Task>>() {

                    @Override
                    public void onResponse(
                            Call<List<Task>> call,
                            Response<List<Task>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            originalList = response.body();

                            updateChartFromTasks(originalList);
                            showLatestUpdatedTasks();

                        } else {
                            updateChartFromTasks(null);
                            Log.e(
                                    "LATEST_TASK",
                                    "Load task failed: " + response.code()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<Task>> call,
                            Throwable t
                    ) {
                        updateChartFromTasks(null);
                        Log.e(
                                "LATEST_TASK",
                                t.getMessage()
                        );
                    }
                });
    }

}
