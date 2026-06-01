package com.example.projex_mobile.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.Task;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailFragment extends Fragment {

    private int taskId;

    private TextView btnBack, btnSave;
    private TextView txtProject, txtStatus;
    private LinearLayout btnStatus;

    private EditText edtTaskName;
    private EditText edtDescription;
    private EditText edtDueDate;
    private EditText edtStartDate;
    private EditText edtPriority;
    private int projectId;
    String projectName = "";
    private String dueDateApi = "";

    private String token = "";

    public TaskDetailFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.task_detail_fragment, container, false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);


        if (getArguments() != null) {
            taskId = getArguments().getInt("task_id");
            projectName = getArguments().getString("project_name", "");
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences(
                        "user_prefs",
                        Context.MODE_PRIVATE
                );

        token = prefs.getString("token", "");

        btnBack = view.findViewById(R.id.btnHuy);
        btnSave = view.findViewById(R.id.btnSave);

        txtProject = view.findViewById(R.id.txtProject);
        txtStatus = view.findViewById(R.id.txtStatus);
        btnStatus = view.findViewById(R.id.btnStatus);

        edtTaskName = view.findViewById(R.id.taskname);
        edtDescription = view.findViewById(R.id.description);
        edtDueDate = view.findViewById(R.id.Duedate);
        edtStartDate = view.findViewById(R.id.Startdate);
        edtPriority = view.findViewById(R.id.Priority);

        edtDueDate.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        btnStatus.setOnClickListener(v -> showStatusPopup());
        btnSave.setOnClickListener(v -> updateTask());

        loadTaskDetail();
    }

    private void showStatusPopup() {

        PopupMenu popup = new PopupMenu(requireContext(), btnStatus);

        popup.getMenu().add("Assigned");
        popup.getMenu().add("InProgress");
        popup.getMenu().add("Done");

        popup.setOnMenuItemClickListener(item -> {

            txtStatus.setText(item.getTitle().toString());

            return true;
        });

        popup.show();
    }

    private void loadTaskDetail() {

        if (taskId == 0) {
            Toast.makeText(
                    requireContext(),
                    "Không có task id",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getTaskById(token, taskId)
                .enqueue(new Callback<Task>() {

                    @Override
                    public void onResponse(Call<Task> call,
                                           Response<Task> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null)
                        {

                            Task task = response.body();
                            bindTask(task);

                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Không lấy được task: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Task> call, Throwable t)
                    {

                        if (!isAdded()) return;
                        Toast.makeText(
                                requireContext(),
                                "Lỗi: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void bindTask(Task task) {

        edtTaskName.setText(task.getTitle() != null ? task.getTitle() : "");

        edtDescription.setText(task.getDescription() != null ? task.getDescription() : "");

        txtStatus.setText(task.getStatus() != null ? task.getStatus() : "Assigned");

        String dueDate = task.getDueDate();
        if (dueDate != null && dueDate.contains("T")) {
            dueDate = dueDate.substring(0, dueDate.indexOf("T"));
        }
        edtDueDate.setText(dueDate != null ? dueDate : "");

        String createdAt = task.getCreatedAt();
        if (createdAt != null && createdAt.contains("T")) {
            createdAt = createdAt.substring(0, createdAt.indexOf("T"));
        }
        edtStartDate.setText(createdAt != null ? createdAt : "");
        edtPriority.setText(String.valueOf(task.getPriority()));

        if (projectName != null && !projectName.isEmpty()) {

            txtProject.setText(projectName);

        } else if (task.getProject() != null && task.getProject().getName() != null)
        {
            txtProject.setText(task.getProject().getName());

        } else {

            txtProject.setText("Project " + task.getProjectId());
        }
    }
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance(
                TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        );


        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {

                    String displayDate = String.format(
                                    "%04d-%02d-%02d",
                                    selectedYear,
                                    selectedMonth + 1,
                                    selectedDay
                            );

                    dueDateApi = selectedYear
                            + "-"
                            + String.format("%02d", selectedMonth + 1)
                            + "-"
                            + String.format("%02d", selectedDay)
                            + "T23:59:00";

                    edtDueDate.setText(displayDate);
                },
                year, month, day
        );

        dialog.show();
    }

    private void updateTask() {

        String title = edtTaskName.getText().toString().trim();

        String description = edtDescription.getText().toString().trim();

        String dueDate = dueDateApi;


        String status = txtStatus.getText().toString().trim();

        String priorityText = edtPriority.getText().toString().trim();

        if (title.isEmpty()) {edtTaskName.setError("Vui lòng nhập tên task");
            return;
        }

        int priority = 1;

        try {
            priority = Integer.parseInt(priorityText);
        }
        catch (Exception ignored) {}

        Map<String, Object> body = new HashMap<>();

        body.put("title", title);
        body.put("description", description);
        body.put("status", status);
        body.put("priority", priority);

        if (!dueDate.isEmpty()) {
            body.put("dueDate", dueDate);
        }

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.updateTask(token, taskId, body)
                .enqueue(new Callback<Task>() {

                    @Override
                    public void onResponse(Call<Task> call, Response<Task> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    requireContext(),
                                    "Lưu task thành công",
                                    Toast.LENGTH_SHORT
                            ).show();

                            requireActivity()
                                    .getSupportFragmentManager()
                                    .popBackStack();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Lưu thất bại: "
                                            + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Task> call, Throwable t) {

                        if (!isAdded()) return;
                        Toast.makeText(
                                requireContext(),
                                "Lỗi: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}