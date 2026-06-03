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
    private List<NotificationItem> allItems = new ArrayList<>();
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
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        if (!isAdded()) return;

                        if (!response.isSuccessful() || response.body() == null) {
                            Toast.makeText(requireContext(), "Không tải được thông báo", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        parseNotifications(response.body());
                        adapter.setData(new ArrayList<>(allItems));
                        setActive(btnTatCa, btnChuaDoc);
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void parseNotifications(JsonObject body) {
        allItems.clear();

        JsonArray items = null;
        if (body.has("items") && body.get("items").isJsonArray()) {
            items = body.getAsJsonArray("items");
        } else if (body.has("data") && body.get("data").isJsonArray()) {
            items = body.getAsJsonArray("data");
        }

        if (items == null) return;

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

            allItems.add(new NotificationItem(
                    id,
                    title,
                    message,
                    type,
                    !isRead,
                    projectId,
                    taskId,
                    createdAt
            ));
        }

        allItems = sortUnreadFirst(allItems);
        unreadItems = filterUnread(allItems);
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
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
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
        allItems = sortUnreadFirst(remaining);
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

    private List<NotificationItem> sortUnreadFirst(List<NotificationItem> source) {
        List<NotificationItem> result = new ArrayList<>(source);
        result.sort((o1, o2) -> {
            if (o1.isUnread == o2.isUnread) return 0;
            return o1.isUnread ? -1 : 1;
        });
        return result;
    }

    private void setActive(TextView active, TextView inactive) {
        active.setTextColor(requireContext().getColor(R.color.white));
        active.setBackgroundResource(R.drawable.notice_bg_btn_active);
        inactive.setTextColor(requireContext().getColor(R.color.text_secondary));
        inactive.setBackgroundResource(R.drawable.notice_bg_btn_inactive);
    }
}