package com.example.projex_mobile;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.adapter.QuickAccessAdapter;
import com.example.projex_mobile.adapter.RecentActivityAdapter;
import com.example.projex_mobile.objects.QuickAccess;
import com.example.projex_mobile.objects.Recent;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        ImageView btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> {
            //chua co gi
        });

        RecyclerView rvQuickAccess = findViewById(R.id.rvQuickAccess);

        List<QuickAccess> quickAccessItems = new ArrayList<>();
        quickAccessItems.add(new QuickAccess("GGshop", "DỰ ÁN", R.drawable.login_logo_github));
        quickAccessItems.add(new QuickAccess("gg_projex", "DỰ ÁN", R.drawable.ic_logo));

        QuickAccessAdapter accessAdapter = new QuickAccessAdapter(quickAccessItems);
        rvQuickAccess.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvQuickAccess.setAdapter(accessAdapter);

        RecyclerView rvRecentActivity = findViewById(R.id.rvRecentActivity);

        List<Recent> recentItems = new ArrayList<>();
        recentItems.add(new Recent("GGSHOP", "Đã cập nhật 2h trước"));
        recentItems.add(new Recent("Vừa xử lý xong", "14 tasks hoàn thành"));

        RecentActivityAdapter recentAdapter = new RecentActivityAdapter(recentItems);
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        rvRecentActivity.setAdapter(recentAdapter);
    }
}