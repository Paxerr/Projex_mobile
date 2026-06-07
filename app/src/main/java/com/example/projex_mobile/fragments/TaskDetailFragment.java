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
import com.example.projex_mobile.objects.Project;
import com.example.projex_mobile.objects.ProjectMember;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskDetailFragment extends Fragment {

    private int taskId;

    private TextView btnBack, btnSave, btnDeleteTask;
    private TextView txtProject, txtStatus;
    private LinearLayout btnStatus;

    private EditText edtTaskName;
    private EditText edtDescription;
    private EditText edtDueDate;
    private EditText edtStartDate;
    private EditText edtPriority;
    private EditText edtAssigned;
    private List<ProjectMember> members = new ArrayList<>();
    private List<Integer> oldAssignedUserIds = new ArrayList<>();
    private List<Integer> selectedUserIds = new ArrayList<>();
    private int projectId;
    String projectName = "";
    private String dueDateApi = "";
    private String originalTitle = "";
    private String originalDescription = "";
    private String originalStatus = "Assigned";
    private String originalDueDateApi = "";
    private int originalPriority = 1;
    private int currentUserId = 0;
    private boolean canManageTask = false;
    private boolean permissionsLoaded = false;

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
            projectId = getArguments().getInt("project_id", 0);
            projectName = getArguments().getString("project_name", "");
        }

        SharedPreferences prefs = requireActivity().getSharedPreferences(
                        "user_prefs",
                        Context.MODE_PRIVATE
                );

        token = prefs.getString("token", "");
        currentUserId = prefs.getInt("user_id", 0);

        btnBack = view.findViewById(R.id.btnHuy);
        btnSave = view.findViewById(R.id.btnSave);
        btnDeleteTask = view.findViewById(R.id.btnDeleteTask);
        btnDeleteTask.setVisibility(View.GONE);

        txtProject = view.findViewById(R.id.txtProject);
        txtStatus = view.findViewById(R.id.txtStatus);
        btnStatus = view.findViewById(R.id.btnStatus);

        edtTaskName = view.findViewById(R.id.taskname);
        edtDescription = view.findViewById(R.id.description);
        edtDueDate = view.findViewById(R.id.Duedate);
        edtStartDate = view.findViewById(R.id.Startdate);
        edtPriority = view.findViewById(R.id.Priority);
        edtAssigned = view.findViewById(R.id.Assigned);
        edtAssigned.setFocusable(false);
        edtAssigned.setClickable(true);

        edtAssigned.setOnClickListener(v -> showMemberPopup());

        edtDueDate.setOnClickListener(v -> showDatePicker());

        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        btnStatus.setOnClickListener(v -> showStatusPopup());
        btnSave.setOnClickListener(v -> updateTask());
        btnDeleteTask.setOnClickListener(v -> confirmDeleteTask());

        loadMembers();
        loadTaskDetail();
    }

    private void showStatusPopup() {

        if (!canCurrentUserChangeStatus()) {
            Toast.makeText(
                    requireContext(),
                    "Ban khong co quyen cap nhat status task nay",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

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

                            recordTaskAccess();
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

        if (projectId == 0 && task.getProjectId() > 0) {
            projectId = task.getProjectId();
            loadMembers();
        }

        originalTitle = task.getTitle() != null ? task.getTitle() : "";
        originalDescription = task.getDescription() != null ? task.getDescription() : "";
        originalStatus = normalizeStatus(task.getStatus());
        originalPriority = task.getPriority();

        edtTaskName.setText(originalTitle);

        edtDescription.setText(originalDescription);

        txtStatus.setText(originalStatus);
        oldAssignedUserIds.clear();
        selectedUserIds.clear();

        if (task.getAssignees() != null && !task.getAssignees().isEmpty()) {

            StringBuilder names = new StringBuilder();

            for (int i = 0; i < task.getAssignees().size(); i++) {

                int userId = task.getAssignees().get(i).getUserId();
                String fullName = task.getAssignees().get(i).getFullName();
                if (fullName == null || fullName.trim().isEmpty()) {
                    fullName = task.getAssignees().get(i).getEmail();
                }
                if (fullName == null || fullName.trim().isEmpty()) {
                    fullName = "User " + userId;
                }

                oldAssignedUserIds.add(userId);
                selectedUserIds.add(userId);

                if (i > 0) {
                    names.append(", ");
                }

                names.append(fullName);
            }

            edtAssigned.setText(names.toString());

        } else {
            edtAssigned.setText("Chưa assigned");
        }

        dueDateApi = task.getDueDate() != null ? task.getDueDate() : "";
        originalDueDateApi = dueDateApi;

        String dueDate = dueDateApi != null ? dueDateApi : "";
        if (dueDate.isEmpty()){
            edtDueDate.setText("No time limit");
        }
        else if (dueDate.contains("T")) {
            dueDate = dueDate.substring(0, dueDate.indexOf("T"));
            edtDueDate.setText(dueDate != null ? dueDate : "");
        } else {
            edtDueDate.setText(dueDate);
        }


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

        applyFieldPermissions();
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

        String dueDate = dueDateApi != null ? dueDateApi : "";


        String status = normalizeStatus(txtStatus.getText().toString());

        String priorityText = edtPriority.getText().toString().trim();

        if (title.isEmpty()) {edtTaskName.setError("Vui lòng nhập tên task");
            return;
        }

        int priority = 1;

        try {
            priority = Integer.parseInt(priorityText);
        }
        catch (Exception ignored) {}

        boolean statusChanged = !safeEquals(status, normalizeStatus(originalStatus));
        boolean detailChanged =
                !safeEquals(title, originalTitle)
                        || !safeEquals(description, originalDescription)
                        || priority != originalPriority
                        || !safeEquals(dateKey(dueDate), dateKey(originalDueDateApi));
        boolean assignmentChanged = !sameUserIds(oldAssignedUserIds, selectedUserIds);

        if (!statusChanged && !detailChanged && !assignmentChanged) {
            Toast.makeText(
                    requireContext(),
                    "Không có thay đổi",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (!detailChanged && statusChanged) {
            updateTaskStatusOnly(assignmentChanged, false);
            return;
        }

        if (!detailChanged && assignmentChanged) {
            syncTaskAssignments();
            return;
        }

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

                            originalStatus =
                                    normalizeStatus(
                                            txtStatus.getText().toString());

                            recordTaskAccess();

                            finishSave();
                        } else {
                            if (response.code() == 403 && statusChanged) {
                                updateTaskStatusOnly(false, true);
                                return;
                            }

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

    private void updateTaskStatusOnly(
            boolean syncAssignmentsAfter,
            boolean showLimitedPermissionMessage
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", normalizeStatus(txtStatus.getText().toString()));

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.updateTaskStatus(token, taskId, body)
                .enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(Call<Task> call, Response<Task> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful()) {
                            originalStatus = normalizeStatus(txtStatus.getText().toString());

                            if (syncAssignmentsAfter) {
                                syncTaskAssignments();
                                return;
                            }

                            if (showLimitedPermissionMessage) {
                                finishSaveWithMessage("Đã cập nhập status. Thong tin/assigned can quyen Admin hoac Owner.");
                            } else {
                                finishSave();
                            }
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Lưu status thất bại: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Task> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(
                                requireContext(),
                                "Loi: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void confirmDeleteTask() {
        if (!canManageTask) {
            Toast.makeText(
                    requireContext(),
                    "Chỉ Admin/Owner mới được task",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa task")
                .setMessage("Bạn có chắc chắn muốn xóa task này?")
                .setPositiveButton("Xóa", (dialog, which) -> executeDeleteTask())
                .setNegativeButton("Hủy", null)
                .show();
    }
    private void recordTaskAccess() {
        if (taskId == 0 || token == null || token.isEmpty()) return;

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.recordTaskAccess(token, taskId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;
                        if (!response.isSuccessful()) {
                            Toast.makeText(requireContext(),
                                    "Không lưu được recent: " + response.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(),
                                "Lỗi recent: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void executeDeleteTask() {
        if (taskId == 0) {
            Toast.makeText(
                    requireContext(),
                    "Không tìm thấy task để xóa",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnDeleteTask.setEnabled(false);
        btnDeleteTask.setAlpha(0.55f);

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.deleteTask(token, taskId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;

                        btnDeleteTask.setEnabled(true);
                        btnDeleteTask.setAlpha(1f);

                        if (response.isSuccessful()) {
                            notifyTaskChanged(true);
                            Toast.makeText(
                                    requireContext(),
                                    "Xóa task thành công",
                                    Toast.LENGTH_SHORT
                            ).show();

                            requireActivity()
                                    .getSupportFragmentManager()
                                    .popBackStack();
                        } else if (response.code() == 403) {
                            Toast.makeText(
                                    requireContext(),
                                    "Bạn không có quyền xóa task này",
                                    Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            Toast.makeText(
                                    requireContext(),
                                    "Xóa task thất bại: " + response.code(),
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;

                        btnDeleteTask.setEnabled(true);
                        btnDeleteTask.setAlpha(1f);

                        Toast.makeText(
                                requireContext(),
                                "Loi: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loadMembers() {

        if (projectId == 0) {
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getProjectDetail(token, projectId)
                .enqueue(new Callback<Project>() {
                    @Override
                    public void onResponse(Call<Project> call,
                                           Response<Project> response) {

                        if (!isAdded()) return;

                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().getMembers() != null) {

                            members.clear();
                            members.addAll(response.body().getMembers());
                            permissionsLoaded = true;
                            canManageTask = false;

                            for (ProjectMember member : members) {
                                if (member.getUserId() == currentUserId) {
                                    String role = member.getRole();
                                    canManageTask = "Owner".equalsIgnoreCase(role)
                                            || "Admin".equalsIgnoreCase(role);
                                    break;
                                }
                            }

                            applyFieldPermissions();
                        }
                    }

                    @Override
                    public void onFailure(Call<Project> call, Throwable t) {
                        if (!isAdded()) return;

                        Toast.makeText(
                                requireContext(),
                                "Lỗi load member: " + t.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }
    private void showMemberPopup() {

        if (permissionsLoaded && !canManageTask) {
            Toast.makeText(
                    requireContext(),
                    "Chi Admin/Owner moi duoc sua assigned",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (members.isEmpty()) {
            Toast.makeText(requireContext(), "Project chưa có member", Toast.LENGTH_SHORT).show();
            return;
        }

        PopupMenu popup = new PopupMenu(requireContext(), edtAssigned);

        for (ProjectMember member : members) {

            if (member.getUser() != null) {

                int userId = member.getUserId();
                String name = member.getUser().getFullName();

                boolean checked = selectedUserIds.contains(userId);

                popup.getMenu().add(
                        0,
                        userId,
                        0,
                        checked ? "✓ " + name : name
                );
            }
        }

        popup.setOnMenuItemClickListener(item -> {

            int userId = item.getItemId();

            if (selectedUserIds.contains(userId)) {
                selectedUserIds.remove(Integer.valueOf(userId));
            } else {
                selectedUserIds.add(userId);
            }

            updateAssignedText();

            return true;
        });

        popup.show();
    }

    private void updateAssignedText() {

        StringBuilder names = new StringBuilder();

        for (ProjectMember member : members) {

            if (selectedUserIds.contains(member.getUserId())
                    && member.getUser() != null) {

                if (names.length() > 0) {
                    names.append(", ");
                }

                names.append(member.getUser().getFullName());
            }
        }

        if (names.length() == 0) {
            edtAssigned.setText("Chưa assigned");
        } else {
            edtAssigned.setText(names.toString());
        }
    }

    private void applyFieldPermissions() {
        boolean canEditTaskInfo = canManageTask || !permissionsLoaded;
        boolean canEditStatus = canCurrentUserChangeStatus();

        setEditable(edtTaskName, canEditTaskInfo);
        setEditable(edtDescription, canEditTaskInfo);
        setEditable(edtDueDate, canEditTaskInfo);
        setEditable(edtPriority, canEditTaskInfo);
        setEditable(edtAssigned, canEditTaskInfo);

        edtDueDate.setClickable(canEditTaskInfo);
        edtAssigned.setClickable(canEditTaskInfo);

        btnStatus.setEnabled(canEditStatus);
        btnStatus.setAlpha(canEditStatus ? 1f : 0.55f);

        btnDeleteTask.setVisibility(canManageTask ? View.VISIBLE : View.GONE);
        btnDeleteTask.setEnabled(canManageTask);
        btnDeleteTask.setAlpha(canManageTask ? 1f : 0.55f);
    }

    private void setEditable(EditText editText, boolean enabled) {
        editText.setEnabled(enabled);
        editText.setAlpha(enabled ? 1f : 0.65f);
    }

    private boolean canCurrentUserChangeStatus() {
        return canManageTask
                || oldAssignedUserIds.isEmpty()
                || (currentUserId > 0 && oldAssignedUserIds.contains(currentUserId));
    }

    private String normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "Assigned";
        }

        String value = status.trim().toLowerCase();
        if ("todo".equals(value) || "to do".equals(value) || "assigned".equals(value)) {
            return "Assigned";
        }
        if ("inprogress".equals(value) || "in progress".equals(value)) {
            return "InProgress";
        }
        if ("done".equals(value) || "completed".equals(value)) {
            return "Done";
        }

        return status.trim();
    }

    private boolean safeEquals(String first, String second) {
        String left = first != null ? first : "";
        String right = second != null ? second : "";
        return left.equals(right);
    }

    private String dateKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }

        String date = value.trim();
        int timeIndex = date.indexOf("T");
        if (timeIndex >= 0) {
            return date.substring(0, timeIndex);
        }

        return date;
    }

    private boolean sameUserIds(List<Integer> first, List<Integer> second) {
        if (first.size() != second.size()) {
            return false;
        }

        for (int userId : first) {
            if (!second.contains(userId)) {
                return false;
            }
        }

        return true;
    }

    private void syncTaskAssignments() {

        List<Integer> addIds = new ArrayList<>();
        List<Integer> removeIds = new ArrayList<>();

        for (int userId : selectedUserIds) {
            if (!oldAssignedUserIds.contains(userId)) {
                addIds.add(userId);
            }
        }

        for (int userId : oldAssignedUserIds) {
            if (!selectedUserIds.contains(userId)) {
                removeIds.add(userId);
            }
        }

        int requestCount = 0;

        if (!addIds.isEmpty()) {
            requestCount++;
        }

        requestCount += removeIds.size();

        if (requestCount == 0) {
            finishSave();
            return;
        }

        ApiService apiService =
                RetrofitClient.getApiService(null);

        final int[] pending = {requestCount};
        final boolean[] hasError = {false};

        // ADD ASSIGNMENTS
        if (!addIds.isEmpty()) {

            Map<String, Object> body =
                    new HashMap<>();

            body.put("userIds", addIds);

            apiService.addTaskAssignments(
                            token,
                            taskId,
                            body
                    )
                    .enqueue(new Callback<JsonObject>() {

                        @Override
                        public void onResponse(
                                Call<JsonObject> call,
                                Response<JsonObject> response
                        ) {

                            if (!response.isSuccessful()) {
                                hasError[0] = true;
                            }

                            pending[0]--;

                            if (pending[0] == 0) {
                                handleSyncResult(
                                        hasError[0]
                                );
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<JsonObject> call,
                                Throwable t
                        ) {

                            hasError[0] = true;

                            pending[0]--;

                            if (pending[0] == 0) {
                                handleSyncResult(
                                        hasError[0]
                                );
                            }
                        }
                    });
        }

        // REMOVE ASSIGNMENTS
        for (int userId : removeIds) {

            apiService.removeTaskAssignment(
                            token,
                            taskId,
                            userId
                    )
                    .enqueue(new Callback<JsonObject>() {

                        @Override
                        public void onResponse(
                                Call<JsonObject> call,
                                Response<JsonObject> response
                        ) {

                            if (!response.isSuccessful()) {
                                hasError[0] = true;
                            }

                            pending[0]--;

                            if (pending[0] == 0) {
                                handleSyncResult(
                                        hasError[0]
                                );
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<JsonObject> call,
                                Throwable t
                        ) {

                            hasError[0] = true;

                            pending[0]--;

                            if (pending[0] == 0) {
                                handleSyncResult(
                                        hasError[0]
                                );
                            }
                        }
                    });
        }
    }

    private void handleSyncResult(boolean hasError) {
        if (!isAdded()) return;

        if (hasError) {
            Toast.makeText(
                    requireContext(),
                    "Task đã lưu nhưng assigned có lỗi",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            finishSave();
        }
    }

    private void notifyTaskChanged(boolean deleted) {
        Bundle result = new Bundle();
        result.putInt("task_id", taskId);
        result.putBoolean("deleted", deleted);
        getParentFragmentManager().setFragmentResult("task_changed", result);
    }

    private void finishSaveWithMessage(String message) {
        if (!isAdded()) return;

        notifyTaskChanged(false);

        Toast.makeText(
                requireContext(),
                message,
                Toast.LENGTH_SHORT
        ).show();

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    private void finishSave() {
        if (!isAdded()) return;

        notifyTaskChanged(false);

        Toast.makeText(
                requireContext(),
                "Lưu task thành công",
                Toast.LENGTH_SHORT
        ).show();

        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

}
