package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.adapter.TaskAdapter;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.Task;
import com.example.projex_mobile.objects.TaskResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskFragment extends Fragment {

    private RecyclerView rvTask;

    private TaskAdapter adapter;

    private List<Task> originalList =
            new ArrayList<>();

    private List<Task> filteredList =
            new ArrayList<>();

    private String selectedStatus = "All";

    private EditText edtSearch;


    public TaskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.task_fragment,
                container,
                false
        );

        rvTask = view.findViewById(R.id.rvTask);

        rvTask.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter =
                new TaskAdapter(filteredList);

        rvTask.setAdapter(adapter);

        edtSearch = view.findViewById(R.id.edtSearch);
        LinearLayout btnAllTask = view.findViewById(R.id.btnAllTask);

        LinearLayout btnStatus = view.findViewById(R.id.btnStatus);

        TextView textStatus = view.findViewById(R.id.textStatus);

        btnStatus.setOnClickListener(v -> {

            PopupMenu popup =
                    new PopupMenu(requireContext(),
                            btnStatus);


            popup.getMenu().add("Assigned");
            popup.getMenu().add("InProgress");

            popup.getMenu().add("Done");

            popup.setOnMenuItemClickListener(item -> {

                selectedStatus =
                        item.getTitle().toString();

                textStatus.setText(selectedStatus);

                filterTasks();

                return true;
            });

            popup.show();
        });
        btnAllTask.setOnClickListener(v -> {

            selectedStatus = "All";

            textStatus.setText("Trạng thái");

            edtSearch.setText("");

            filterTasks();
        });


        edtSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        filterTasks();
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                });

        loadTasks();

        return view;
    }

    private void loadTasks() {

        SharedPreferences prefs =
                requireActivity().getSharedPreferences(
                        "user_prefs",
                        Context.MODE_PRIVATE
                );

        String token =
                prefs.getString("token", "");

        ApiService apiService =
                RetrofitClient.getApiService(null);

        apiService.getAssignedTasks(token)
                .enqueue(new Callback<TaskResponse>() {

                    @Override
                    public void onResponse(
                            Call<TaskResponse> call,
                            Response<TaskResponse> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getItems() != null) {

                            originalList =
                                    response.body().getItems();

                            filterTasks();

                        } else {

                            Log.e("TASK_API",
                                    "Response Error");
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<TaskResponse> call,
                            Throwable t
                    ) {

                        Log.e("TASK_API",
                                t.getMessage());
                    }
                });
    }

    private void filterTasks() {

        filteredList.clear();

        String keyword =
                edtSearch.getText()
                        .toString()
                        .trim()
                        .toLowerCase();

        for (Task task : originalList) {

            boolean matchSearch =
                    task.getTitle() != null
                            && task.getTitle()
                            .toLowerCase()
                            .contains(keyword);

            boolean matchStatus =
                    selectedStatus.equals("All")
                            || (
                            task.getStatus() != null
                                    && task.getStatus()
                                    .equalsIgnoreCase(selectedStatus)
                    );

            if (matchSearch && matchStatus) {

                filteredList.add(task);
            }
        }

        adapter.notifyDataSetChanged();
    }
}