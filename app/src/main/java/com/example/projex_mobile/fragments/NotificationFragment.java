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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NotificationFragment extends Fragment {

    private NotificationAdapter adapter;
    private List<NotificationItem> allItems;
    private List<NotificationItem> unreadItems;

    private TextView btnTatCa, btnChuaDoc;

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

        if (allItems == null) {
            allItems = createMockData();
        }

        allItems = sortUnreadFirst(allItems);
        unreadItems = filterUnread(allItems);

        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(new ArrayList<>(allItems));
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
    }

    private List<NotificationItem> createMockData() {
        List<NotificationItem> list = new ArrayList<>();
        list.add(new NotificationItem("Vẽ Sequence diagram của Người mua", "Dat updated a story", "GGSHOP-3", "Dat", true));
        list.add(new NotificationItem("Sequence diagram Đăng xuất", "Tuan Anh updated a task", "GGSHOP-8", "TA", false));
        list.add(new NotificationItem("Thông báo mới 1", "System backup completed", "SYS-001", "SYS", true));
        list.add(new NotificationItem("Vẽ UseCase", "Ngoc Anh updated a task", "SYS-001", "NAnh", true));
        list.add(new NotificationItem("Chức năng Đăng nhập", "Tuan Cui updated a task", "SYS-002", "TCui", true));
        list.add(new NotificationItem("Chức năng Đăng xuất", "Tien Dat updated a task", "SYS-002", "TDat", false));
        list.add(new NotificationItem("Phân tích thiết kế", "Quat updated a task", "GGSHOP-8", "QA", false));
        return list;
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
        Collections.sort(result, new Comparator<NotificationItem>() {
            @Override
            public int compare(NotificationItem o1, NotificationItem o2) {
                if (o1.isUnread == o2.isUnread) return 0;
                return o1.isUnread ? -1 : 1;
            }
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