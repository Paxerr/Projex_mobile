package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.adapter.NotificationAdapter;
import com.example.projex_mobile.objects.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;

    private List<NotificationItem> allItems;
    private List<NotificationItem> unreadItems;

    private TextView btnTatCa, btnChuaDoc;

    public NotificationFragment() {
    }

    public static NotificationFragment newInstance() {
        return new NotificationFragment();
    }

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

        rvNotifications = view.findViewById(R.id.rvNotifications);
        btnTatCa = view.findViewById(R.id.btnTatCa);
        btnChuaDoc = view.findViewById(R.id.btnChuaDoc);

        allItems = new ArrayList<>();
        allItems.add(new NotificationItem("Vẽ Sequence diagram của Người mua", "Dat updated a story", "GGSHOP-3", "Dat", true));
        allItems.add(new NotificationItem("Sequence diagram Đăng xuất", "Tuan Anh updated a task", "GGSHOP-8", "TA", false));
        allItems.add(new NotificationItem("Thông báo mới 1", "System backup completed", "SYS-001", "SYS", true));

        unreadItems = new ArrayList<>();
        for (NotificationItem item : allItems) {
            if (item.isUnread) unreadItems.add(item);
        }

        adapter = new NotificationAdapter(allItems);
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);

        btnTatCa.setOnClickListener(v -> {
            setActive(btnTatCa, btnChuaDoc);
            adapter = new NotificationAdapter(allItems);
            rvNotifications.setAdapter(adapter);
        });

        btnChuaDoc.setOnClickListener(v -> {
            setActive(btnChuaDoc, btnTatCa);
            adapter = new NotificationAdapter(unreadItems);
            rvNotifications.setAdapter(adapter);
        });
    }

    private void setActive(TextView active, TextView inactive) {
        active.setTextColor(requireContext().getColor(R.color.white));
        active.setBackgroundResource(R.drawable.notice_bg_btn_active);
        inactive.setTextColor(requireContext().getColor(R.color.text_secondary));
        inactive.setBackgroundResource(R.drawable.notice_bg_btn_inactive);
    }
}