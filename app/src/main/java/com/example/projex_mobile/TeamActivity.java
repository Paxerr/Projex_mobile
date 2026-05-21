package com.example.projex_mobile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TeamActivity extends AppCompatActivity {

    private ImageView btnAddMember;
    private ImageView btnEditRoleMember1;
    private ImageView btnEditRoleMember2;

    private LinearLayout navHome;
    private LinearLayout navSpaces;
    private LinearLayout navTasks;
    private LinearLayout navNotifications;
    private LinearLayout navUser;
    private ImageView ivSpaces;
    private TextView tvSpaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_activity);

        initViews();
        setupBottomNav();
        handleEvents();
    }

    private void initViews() {
        btnAddMember = findViewById(R.id.btn_add_member);
        btnEditRoleMember1 = findViewById(R.id.btn_edit_role_member1);
        btnEditRoleMember2 = findViewById(R.id.btn_edit_role_member2);

        navHome = findViewById(R.id.nav_home);
        navSpaces = findViewById(R.id.nav_spaces);
        navTasks = findViewById(R.id.nav_tasks);
        navNotifications = findViewById(R.id.nav_notifications);
        navUser = findViewById(R.id.nav_user);
        ivSpaces = findViewById(R.id.iv_spaces);
        tvSpaces = findViewById(R.id.tv_spaces);
    }

    private void handleEvents() {
        if (btnAddMember != null) {
            btnAddMember.setOnClickListener(v -> openAddMemberScreen());
        }

        // Bấm nút 3 chấm của thành viên để sang màn chỉnh sửa quyền
        if (btnEditRoleMember1 != null) {
            btnEditRoleMember1.setOnClickListener(v -> openRoleScreen());
        }
        if (btnEditRoleMember2 != null) {
            btnEditRoleMember2.setOnClickListener(v -> openRoleScreen());
        }

        // Các id navbar lấy từ file layout_bottom_nav.xml sau khi include vào team_activity.xml
        if (navSpaces != null) {
            navSpaces.setOnClickListener(v ->
                    Toast.makeText(this, "Bạn đang ở trang Mọi người", Toast.LENGTH_SHORT).show());
        }
        if (navHome != null) {
            navHome.setOnClickListener(v ->
                    Toast.makeText(this, "HOME", Toast.LENGTH_SHORT).show());
        }
        if (navTasks != null) {
            navTasks.setOnClickListener(v ->
                    Toast.makeText(this, "TASKS", Toast.LENGTH_SHORT).show());
        }
        if (navNotifications != null) {
            navNotifications.setOnClickListener(v ->
                    Toast.makeText(this, "NOTIFICATION", Toast.LENGTH_SHORT).show());
        }
        if (navUser != null) {
            navUser.setOnClickListener(v ->
                    Toast.makeText(this, "USER", Toast.LENGTH_SHORT).show());
        }
    }

    private void setupBottomNav() {
        int activeColor = Color.parseColor("#5A86FF");

        if (ivSpaces != null) {
            ivSpaces.setColorFilter(activeColor);
        }
        if (tvSpaces != null) {
            tvSpaces.setTextColor(activeColor);
        }
        if (navSpaces != null) {
            navSpaces.setSelected(true);
        }
    }

    public void onAddMemberClick(View view) {
        openAddMemberScreen();
    }

    public void onEditRoleClick(View view) {
        openRoleScreen();
    }

    private void openAddMemberScreen() {
        try {
            ThemThanhVienDialogFragment dialog = new ThemThanhVienDialogFragment();
            dialog.show(getSupportFragmentManager(), "ThemThanhVienDialog");
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được popup thêm thành viên", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRoleScreen() {
        try {
            Intent intent = new Intent(TeamActivity.this, RoleActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được màn chỉnh sửa quyền", Toast.LENGTH_SHORT).show();
        }
    }
}
