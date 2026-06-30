package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.adapter.NotificationAdapter;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.NotificationItem;
import com.example.projex_mobile.objects.Task;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {

    private NotificationAdapter adapter;
    private final List<NotificationItem> allItems = new ArrayList<>();
    private List<NotificationItem> unreadItems = new ArrayList<>();

    private TextView btnTatCa, btnChuaDoc, btnMore;
    private String token;

    public NotificationFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.notification_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvNotifications = view.findViewById(R.id.rvNotifications);
        btnTatCa = view.findViewById(R.id.btnTatCa);
        btnChuaDoc = view.findViewById(R.id.btnChuaDoc);
        btnMore = view.findViewById(R.id.btnMore);

        SharedPreferences userPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        token = userPrefs.getString("token", "");

        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(new ArrayList<>());
        rvNotifications.setAdapter(adapter);

        adapter.setOnNotificationClickListener((item, position) -> handleNotificationClick(item, position));

        setActive(btnTatCa, btnChuaDoc);

        btnTatCa.setOnClickListener(v -> {
            setActive(btnTatCa, btnChuaDoc);
            adapter.setData(new ArrayList<>(allItems));
        });

        btnChuaDoc.setOnClickListener(v -> {
            setActive(btnChuaDoc, btnTatCa);
            adapter.setData(new ArrayList<>(unreadItems));
        });

        btnMore.setOnClickListener(this::showPopupMenu);

        loadNotifications();
    }

    private void handleNotificationClick(NotificationItem item, int position) {
        if (item.isUnread) {
            markNotificationAsRead(item, position);
        } else {
            navigateTo(item);
        }
    }

    private void markNotificationAsRead(NotificationItem item, int position) {
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
        ApiService apiService = RetrofitClient.getApiService(token);

        apiService.markNotificationAsRead(authHeader, item.id)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;
                        if (response.isSuccessful()) {
                            item.isUnread = false;
                            adapter.markItemAsRead(position);
                            unreadItems = filterUnread(allItems);
                        }
                        navigateTo(item);
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        navigateTo(item);
                    }
                });
    }

    private void navigateTo(NotificationItem item) {
        if (item.taskId != null) {
            Bundle args = new Bundle();
            args.putInt("task_id", item.taskId);
            if (item.projectId != null) args.putInt("project_id", item.projectId);
            if (item.projectName != null) args.putString("project_name", item.projectName);

            TaskDetailFragment fragment = new TaskDetailFragment();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else if (item.projectId != null) {
            Bundle args = new Bundle();
            args.putInt("project_id", item.projectId);
            args.putString("project_name", item.projectName != null ? item.projectName : "");

            ProjectFragment fragment = new ProjectFragment();
            fragment.setArguments(args);
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, fragment)
                    .addToBackStack(null)
                    .commit();
        } else {
            Toast.makeText(requireContext(), "Không có nội dung liên kết", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadNotifications() {
        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Thiếu token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;
        ApiService apiService = RetrofitClient.getApiService(token);

        apiService.getNotifications(authHeader, 1, 100, null)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call,
                                           @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(requireContext(), "Không tải được thông báo", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        parseNotifications(response.body(), apiService);
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void parseNotifications(JsonObject body, ApiService apiService) {
        allItems.clear();

        JsonArray items = null;
        if (body.has("items") && body.get("items").isJsonArray()) {
            items = body.getAsJsonArray("items");
        } else if (body.has("data") && body.get("data").isJsonArray()) {
            items = body.getAsJsonArray("data");
        }

        if (items == null) {
            adapter.setData(new ArrayList<>());
            return;
        }

        List<NotificationItem> parsedItems = new ArrayList<>();
        final int[] remaining = {0};

        for (JsonElement element : items) {
            if (!element.isJsonObject()) continue;
            remaining[0]++;
        }

        if (remaining[0] == 0) {
            adapter.setData(new ArrayList<>());
            return;
        }

        for (JsonElement element : items) {
            if (!element.isJsonObject()) continue;

            JsonObject obj = element.getAsJsonObject();

            int id = obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsInt() : 0;
            String title = obj.has("title") && !obj.get("title").isJsonNull() ? obj.get("title").getAsString() : "";
            String message = obj.has("message") && !obj.get("message").isJsonNull() ? obj.get("message").getAsString() : "";
            String type = obj.has("type") && !obj.get("type").isJsonNull() ? obj.get("type").getAsString() : "";
            boolean isRead = obj.has("isRead") && !obj.get("isRead").isJsonNull() && obj.get("isRead").getAsBoolean();
            Integer projectId = obj.has("projectId") && !obj.get("projectId").isJsonNull() ? obj.get("projectId").getAsInt() : null;
            Integer taskId = obj.has("taskId") && !obj.get("taskId").isJsonNull() ? obj.get("taskId").getAsInt() : null;
            String createdAt = obj.has("createdAt") && !obj.get("createdAt").isJsonNull() ? obj.get("createdAt").getAsString() : "";
            String senderName = obj.has("senderName") && !obj.get("senderName").isJsonNull() ? obj.get("senderName").getAsString() : "";
            String senderAvatarUrl = obj.has("senderAvatarUrl") && !obj.get("senderAvatarUrl").isJsonNull() ? obj.get("senderAvatarUrl").getAsString() : "";

            if (taskId != null) {
                apiService.getTaskById(token, taskId).enqueue(new Callback<Task>() {
                    @Override
                    public void onResponse(@NonNull Call<Task> call, @NonNull Response<Task> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            Task task = response.body();
                            int resolvedProjectId = task.getProjectId();

                            apiService.getProjectDetailRaw(token, resolvedProjectId).enqueue(new Callback<JsonObject>() {
                                @Override
                                public void onResponse(@NonNull Call<JsonObject> call,
                                                       @NonNull Response<JsonObject> response) {
                                    if (!isAdded()) return;

                                    String resolvedProjectName = "Project " + resolvedProjectId;

                                    if (response.isSuccessful() && response.body() != null) {
                                        JsonObject body = response.body();

                                        if (body.has("name") && !body.get("name").isJsonNull()) {
                                            resolvedProjectName = body.get("name").getAsString();
                                        } else if (body.has("data") && body.get("data").isJsonObject()) {
                                            JsonObject data = body.getAsJsonObject("data");
                                            if (data.has("name") && !data.get("name").isJsonNull()) {
                                                resolvedProjectName = data.get("name").getAsString();
                                            }
                                        } else if (body.has("project") && body.get("project").isJsonObject()) {
                                            JsonObject project = body.getAsJsonObject("project");
                                            if (project.has("name") && !project.get("name").isJsonNull()) {
                                                resolvedProjectName = project.get("name").getAsString();
                                            }
                                        }
                                    }

                                    allItems.add(new NotificationItem(
                                            id, title, message,
                                            senderName, senderAvatarUrl,
                                            !isRead, resolvedProjectId, resolvedProjectName,
                                            taskId, createdAt
                                    ));
                                    onNotificationBuilt();
                                }

                                @Override
                                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                                    if (!isAdded()) return;

                                    allItems.add(new NotificationItem(
                                            id, title, message,
                                            senderName, senderAvatarUrl,
                                            !isRead, resolvedProjectId, "Project " + resolvedProjectId,
                                            taskId, createdAt
                                    ));
                                    onNotificationBuilt();
                                }
                            });

                        } else {
                            allItems.add(new NotificationItem(
                                    id, title, message,
                                    senderName, senderAvatarUrl,
                                    !isRead, projectId, null,
                                    taskId, createdAt
                            ));
                            onNotificationBuilt();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Task> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        allItems.add(new NotificationItem(
                                id, title, message,
                                senderName, senderAvatarUrl,
                                !isRead, projectId, null,
                                taskId, createdAt
                        ));
                        onNotificationBuilt();
                    }
                });
            } else {
                allItems.add(new NotificationItem(
                        id, title, message,
                        senderName, senderAvatarUrl,
                        !isRead, projectId, null,
                        null, createdAt
                ));
                onNotificationBuilt();
            }
        }
    }

    private void onNotificationBuilt() {
        if (!isAdded()) return;

        if (allItems.size() == 0) return;

        if (allItems.size() > 1) {
            allItems.sort((a, b) -> {
                if (a.createdAt == null) return 1;
                if (b.createdAt == null) return -1;
                return b.createdAt.compareTo(a.createdAt);
            });
        }

        unreadItems = filterUnread(allItems);
        adapter.setData(new ArrayList<>(allItems));
        setActive(btnTatCa, btnChuaDoc);
    }

    private void showPopupMenu(View anchor) {
        ContextThemeWrapper wrapper = new ContextThemeWrapper(requireContext(), R.style.CustomPopupMenuStyle);
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(wrapper, anchor);
        popupMenu.inflate(R.menu.menu_notification);
        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_mark_all_read) {
                markAllAsRead();
                return true;
            }
            if (id == R.id.action_delete_read) {
                deleteReadNotifications();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void markAllAsRead() {
        if (token == null || token.trim().isEmpty()) return;
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.markAllNotificationsAsRead(authHeader).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call,
                                   @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    for (NotificationItem item : allItems) item.isUnread = false;
                    unreadItems = filterUnread(allItems);
                    adapter.setData(new ArrayList<>(allItems));
                    setActive(btnTatCa, btnChuaDoc);
                    Toast.makeText(requireContext(), "Đã đánh dấu tất cả là đã đọc", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Không thể đánh dấu đã đọc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReadNotifications() {
        List<NotificationItem> remaining = new ArrayList<>();
        for (NotificationItem item : allItems) {
            if (item.isUnread) remaining.add(item);
        }
        allItems.clear();
        allItems.addAll(remaining);
        unreadItems = filterUnread(allItems);
        adapter.setData(new ArrayList<>(allItems));
        setActive(btnTatCa, btnChuaDoc);
        Toast.makeText(requireContext(), "Đã xóa tất cả thông báo đã đọc", Toast.LENGTH_SHORT).show();
    }

    private List<NotificationItem> filterUnread(List<NotificationItem> source) {
        List<NotificationItem> result = new ArrayList<>();
        for (NotificationItem item : source) {
            if (item.isUnread) result.add(item);
        }
        return result;
    }

    private void setActive(TextView active, TextView inactive) {
        active.setTextColor(requireContext().getColor(R.color.white));
        active.setBackgroundResource(R.drawable.notice_bg_btn_active);
        inactive.setTextColor(requireContext().getColor(R.color.text_secondary));
        inactive.setBackgroundResource(R.drawable.notice_bg_btn_inactive);
    }

}