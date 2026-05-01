package com.example.projex_mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.adapter.NotificationAdapter;
import com.example.projex_mobile.objects.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;

    private List<NotificationItem> allItems;
    private List<NotificationItem> unreadItems;

    private TextView btnTatCa, btnChuaDoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_activity);

        rvNotifications = findViewById(R.id.rvNotifications);
        btnTatCa = findViewById(R.id.btnTatCa);
        btnChuaDoc = findViewById(R.id.btnChuaDoc);

        // Dữ liệu mẫu
        allItems = new ArrayList<>();
        allItems.add(new NotificationItem(
                "Vẽ Sequence diagram của Người mua",
                "Dat updated a story",
                "GGSHOP-3",
                "Dat",
                true));
        allItems.add(new NotificationItem(
                "Sequence diagram Đăng xuất",
                "Tuan Anh updated a task",
                "GGSHOP-8",
                "TA",
                false));
        allItems.add(new NotificationItem(
                "Thông báo mới 1",
                "System backup completed",
                "SYS-001",
                "SYS",
                true));

        unreadItems = new ArrayList<>();
        for (NotificationItem item : allItems) {
            if (item.isUnread) unreadItems.add(item);
        }

        adapter = new NotificationAdapter(allItems);
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);

        btnTatCa.setOnClickListener(v -> {
            btnTatCa.setTextColor(getColor(R.color.white));
            btnTatCa.setBackground(getDrawable(R.drawable.notice_bg_btn_active));
            btnChuaDoc.setTextColor(getColor(R.color.text_secondary));
            btnChuaDoc.setBackground(getDrawable(R.drawable.notice_bg_btn_inactive));

            adapter = new NotificationAdapter(allItems);
            rvNotifications.setAdapter(adapter);
        });

        btnChuaDoc.setOnClickListener(v -> {
            btnTatCa.setTextColor(getColor(R.color.text_secondary));
            btnTatCa.setBackground(getDrawable(R.drawable.notice_bg_btn_inactive));
            btnChuaDoc.setTextColor(getColor(R.color.white));
            btnChuaDoc.setBackground(getDrawable(R.drawable.notice_bg_btn_active));

            adapter = new NotificationAdapter(unreadItems);
            rvNotifications.setAdapter(adapter);
        });
    }
}