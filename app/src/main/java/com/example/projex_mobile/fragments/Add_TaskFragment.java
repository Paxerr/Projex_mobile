package com.example.projex_mobile.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Add_TaskFragment extends Fragment {

    private int projectId;
    private String projectName = "";
    private String dueDateApi = "";

    private EditText taskname, description, Duedate, Priority;
    private TextView btnTao, btnHuy, txtProject;

    public Add_TaskFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.add_task_fragment,
                container,
                false
        );

        if (getArguments() != null) {
            projectId = getArguments().getInt("project_id", 0);
            projectName = getArguments().getString("project_name", "");
        }

        taskname = view.findViewById(R.id.taskname);
        description = view.findViewById(R.id.description);
        Duedate = view.findViewById(R.id.Duedate);
        Priority = view.findViewById(R.id.Priority);
        txtProject = view.findViewById(R.id.txtProject);

        btnTao = view.findViewById(R.id.btnTao);
        btnHuy = view.findViewById(R.id.btnHuy);

        txtProject.setText(projectName);

        Duedate.setOnClickListener(v -> showDatePicker());

        btnHuy.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        btnTao.setOnClickListener(v -> createTask());

        return view;
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


                    Duedate.setText(displayDate);
                },

                day,month,year
        );

        dialog.show();
    }

    private void createTask() {

        String title = taskname.getText().toString().trim();

        String desc = description.getText().toString().trim();

        String dueDate = dueDateApi;

        String priorityText = Priority.getText().toString().trim();

        if (projectId == 0) {Toast.makeText(
                    requireContext(),
                    "Thiếu projectId",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (title.isEmpty()) {taskname.setError("Nhập tên task");
            return;
        }

        if (dueDate.isEmpty()) {Duedate.setError("Chọn ngày hết hạn");
            return;
        }

        if (priorityText.isEmpty()) {Priority.setError("Nhập priority");
            return;
        }

        int priority;

        try {
            priority = Integer.parseInt(priorityText);
        } catch (Exception e) {
            Priority.setError("Priority phải là số");
            return;
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences(
                        "user_prefs",
                        Context.MODE_PRIVATE
                );

        String token = prefs.getString("token", "");

        int userId = prefs.getInt("user_id", 0);

        if (token.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Thiếu token đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (userId == 0) {
            Toast.makeText(
                    requireContext(),
                    "Thiếu user_id đăng nhập",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        Map<String, Object> body = new HashMap<>();

        body.put("title", title);
        body.put("description", desc);
        body.put("status", "Assigned");
        body.put("priority", priority);
        body.put("dueDate", dueDate);

        List<Integer> assignedUserIds = new ArrayList<>();

        assignedUserIds.add(userId);

        body.put("assignedUserIds", assignedUserIds);

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.createTask(token, projectId, body)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    requireContext(),
                                    "Tạo task thành công",
                                    Toast.LENGTH_SHORT
                            ).show();

                            requireActivity()
                                    .getSupportFragmentManager()
                                    .popBackStack();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Lỗi tạo task: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                requireContext(),
                                t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
}