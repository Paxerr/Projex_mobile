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

    private List<Task> originalList = new ArrayList<>();

    private List<Task> filteredList = new ArrayList<>();

    private String selectedStatus = "All";

    private EditText edtSearch;
    private TextView btnPrevPage;
    private TextView btnNextPage;
    private TextView tvPageInfo;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;


    public TaskFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.task_fragment, container, false);

        rvTask = view.findViewById(R.id.rvTask);

        rvTask.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TaskAdapter(filteredList, task -> {

                    TaskDetailFragment fragment = new TaskDetailFragment();

                    Bundle bundle = new Bundle();

                    bundle.putInt("task_id", task.getId());

                    if(task.getProject() != null){
                        bundle.putInt("project_id", task.getProject().getId());
                        bundle.putString("project_name", task.getProject().getName());
                    }

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
                }
        );

        rvTask.setAdapter(adapter);

        edtSearch = view.findViewById(R.id.edtSearch);
        LinearLayout btnAllTask = view.findViewById(R.id.btnAllTask);

        LinearLayout btnStatus = view.findViewById(R.id.btnStatus);

        TextView textStatus = view.findViewById(R.id.textStatus);
        btnPrevPage = view.findViewById(R.id.btnPrevPage);
        btnNextPage = view.findViewById(R.id.btnNextPage);
        tvPageInfo = view.findViewById(R.id.tvPageInfo);

        btnPrevPage.setOnClickListener(v -> {
            if (!isLoading && currentPage > 1) {
                currentPage--;
                loadTasks();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (!isLoading && currentPage < totalPages) {
                currentPage++;
                loadTasks();
            }
        });

        btnStatus.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), btnStatus);

            popup.getMenu().add("Assigned");
            popup.getMenu().add("InProgress");
            popup.getMenu().add("Done");

            popup.setOnMenuItemClickListener(item -> {

                selectedStatus = item.getTitle().toString();

                textStatus.setText(selectedStatus);

                currentPage = 1;
                loadTasks();

                return true;
            });

            popup.show();
        });
        btnAllTask.setOnClickListener(v -> {

            selectedStatus = "All";
            textStatus.setText("Trạng thái");
            edtSearch.setText("");
            currentPage = 1;
            loadTasks();
        });


        edtSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        filterTasks();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

        updatePaginationUi();
        loadTasks();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener(
                "task_changed",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    currentPage = 1;
                    loadTasks();
                }
        );
    }

    private void loadTasks() {
        isLoading = true;
        updatePaginationUi();

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("token", "");

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getAssignedTasks(token, currentPage, PAGE_SIZE)
                .enqueue(new Callback<TaskResponse>() {

                    @Override
                    public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                        if (!isAdded()) {
                            return;
                        }

                        isLoading = false;

                        if (response.isSuccessful() && response.body() != null
                                && response.body().getItems() != null) {

                            TaskResponse body = response.body();
                            if (body.getPage() != null && body.getPage() > 0) {
                                currentPage = body.getPage();
                            }

                            originalList = new ArrayList<>(body.getItems());
                            totalPages = Math.max(1, body.resolveTotalPages(currentPage, PAGE_SIZE));

                            boolean hasKnownTotal = body.getTotalPages() != null
                                    || body.getTotalItems() != null;
                            if (originalList.isEmpty()
                                    && currentPage > 1
                                    && (!hasKnownTotal || currentPage > totalPages)) {
                                currentPage = hasKnownTotal ? totalPages : currentPage - 1;
                                loadTasks();
                                return;
                            }

                            filterTasks();

                        } else {
                            Log.e("TASK_API",
                                    "Response Error");
                        }

                        updatePaginationUi();
                    }

                    @Override
                    public void onFailure(Call<TaskResponse> call, Throwable t) {
                        if (!isAdded()) {
                            return;
                        }

                        isLoading = false;
                        updatePaginationUi();
                        Log.e("TASK_API",
                                String.valueOf(t.getMessage()));
                    }
                });
    }

    private void filterTasks() {

        filteredList.clear();

        String keyword = edtSearch.getText().toString().trim().toLowerCase();

        for (Task task : originalList) {
            boolean matchSearch = task.getTitle() != null && task.getTitle().toLowerCase().contains(keyword);

            boolean matchStatus = selectedStatus.equals("All") || (task.getStatus() != null && task.getStatus().equalsIgnoreCase(selectedStatus));

            if (matchSearch && matchStatus) {
                filteredList.add(task);
            }
        }

        adapter.notifyDataSetChanged();
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
