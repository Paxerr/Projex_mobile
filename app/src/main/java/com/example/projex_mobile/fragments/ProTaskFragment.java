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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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
import com.example.projex_mobile.objects.ProjectMember;
import com.example.projex_mobile.objects.Task;
import com.example.projex_mobile.objects.TaskAssignment;
import com.example.projex_mobile.objects.TaskResponse;

import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProTaskFragment extends Fragment {

    private RecyclerView rvTask;

    private TaskAdapter adapter;

    private List<Task> originalList = new ArrayList<>();

    private List<Task> filteredList = new ArrayList<>();
    private List<ProjectMember> members = new ArrayList<>();

    private int projectId;
    private String projectName = "";

    private String selectedStatus = "All";
    private String selectedUser = "All";

    private EditText edtSearch;
    private TextView txtProject;
    private TextView btnPrevPage;
    private TextView btnNextPage;
    private TextView tvPageInfo;

    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalPages = 1;
    private boolean isLoading = false;

    public ProTaskFragment() {}

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState)
    {
        return inflater.inflate(
                R.layout.pro_task_fragment,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        if(getArguments() != null){
            projectId = getArguments().getInt("project_id");
            projectName = getArguments().getString("project_name", "");
        }

        getParentFragmentManager().setFragmentResultListener(
                "task_changed",
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    currentPage = 1;
                    loadTasks();
                }
        );

        rvTask = view.findViewById(R.id.rvTasks);

        rvTask.setLayoutManager(new LinearLayoutManager(requireContext()));

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

        ImageView btnBack = view.findViewById(R.id.btnBack);
        FrameLayout btnAdd = view.findViewById(R.id.btnAdd);
        LinearLayout ngth = view.findViewById(R.id.ngth);
        TextView ngthText = view.findViewById(R.id.ngth_text);
        TextView txtProject = view.findViewById(R.id.txtProject);
        txtProject.setText(projectName);

        btnBack.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });



        btnAdd.setOnClickListener(v -> {

            Add_TaskFragment fragment = new Add_TaskFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("project_id", projectId);
            bundle.putString("project_name", projectName);

            fragment.setArguments(bundle);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(
                            R.id.frame_container,
                            fragment
                    )
                    .addToBackStack(null)
                    .commit();
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

        ngth.setOnClickListener(v -> {

            if (members.isEmpty()) {
                Toast.makeText(
                        requireContext(),
                        "Project chưa có member",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            PopupMenu popup = new PopupMenu(requireContext(), ngth);

            popup.getMenu().add("All");

            for (ProjectMember member : members) {
                if (member.getUser() != null) {

                    int userId = member.getUserId();
                    String name = member.getUser().getFullName();

                    popup.getMenu().add(
                            0,
                            userId,
                            0,
                            name
                    );
                }
            }

            popup.setOnMenuItemClickListener(item -> {

                selectedUser = item.getTitle().toString();

                if (selectedUser.equals("All")) {
                    ngthText.setText("Người thực hiện");
                } else {
                    ngthText.setText(selectedUser);
                }

                currentPage = 1;
                loadTasks();

                return true;
            });

            popup.show();
        });

        edtSearch.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count)
                    {
                        filterTasks();
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

        updatePaginationUi();
        loadTasks();
        loadMembers();

    }

    private void loadTasks(){
        isLoading = true;
        updatePaginationUi();

        SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("token", "");

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getTasksByProject(token, projectId, currentPage, PAGE_SIZE).enqueue(new Callback<TaskResponse>() {

            @Override
            public void onResponse(Call<TaskResponse> call, Response<TaskResponse> response) {
                if (!isAdded()) {
                    return;
                }

                isLoading = false;

                if(response.isSuccessful() && response.body() != null && response.body().getItems() != null){
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

                }else{
                    Log.e(
                            "PROJECT_TASK",
                            "Code: " + response.code()
                    );
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

                Log.e(
                        "PROJECT_TASK",
                        String.valueOf(t.getMessage())
                );
            }
        });
    }

    private void filterTasks() {

        filteredList.clear();

        String keyword = edtSearch.getText().toString().trim().toLowerCase();

        for (Task task : originalList) {
            boolean matchSearch = task.getTitle() != null && task.getTitle().toLowerCase().contains(keyword);

            boolean matchStatus = selectedStatus.equals("All") || (task.getStatus() != null && task.getStatus()
                    .equalsIgnoreCase(selectedStatus));

            boolean matchUser = selectedUser.equals("All");

            if(!matchUser && task.getAssignees() != null){

                for(TaskAssignment assignee : task.getAssignees()){

                    if(assignee.getFullName() != null && assignee.getFullName().equalsIgnoreCase(selectedUser)){

                        matchUser = true;
                        break;
                    }
                }
            }

            if (matchSearch && matchStatus && matchUser) {
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

    private void loadMembers() {

        SharedPreferences prefs = requireActivity().getSharedPreferences(
                "user_prefs",
                Context.MODE_PRIVATE
        );

        String token = prefs.getString("token", "");

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getProjectDetail(token, projectId)
                .enqueue(new Callback<com.example.projex_mobile.objects.Project>() {
                    @Override
                    public void onResponse(
                            Call<com.example.projex_mobile.objects.Project> call,
                            Response<com.example.projex_mobile.objects.Project> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getMembers() != null) {

                            members.clear();
                            members.addAll(response.body().getMembers());
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<com.example.projex_mobile.objects.Project> call,
                            Throwable t
                    ) {
                        Toast.makeText(
                                requireContext(),
                                "Lỗi load member: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }



}
