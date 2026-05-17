package com.example.projex_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TeamActivity extends AppCompatActivity {

    private ImageView btnAddMember;
    private ImageView btnEditRoleMember1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_activity);

        initViews();
        handleEvents();
    }

    private void initViews() {
        btnAddMember = findViewById(R.id.btn_add_member);
        btnEditRoleMember1 = findViewById(R.id.btn_edit_role_member1);
    }

    private void handleEvents() {
        btnAddMember.setOnClickListener(v -> openAddMemberScreen());

        // Bấm nút 3 chấm của thành viên để sang màn chỉnh sửa quyền
        btnEditRoleMember1.setOnClickListener(v -> openRoleScreen());
    }

    public void onAddMemberClick(View view) {
        openAddMemberScreen();
    }

    private void openAddMemberScreen() {
        try {
            Intent intent = new Intent(TeamActivity.this, ThemThanhVienActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không mở được màn thêm thành viên", Toast.LENGTH_SHORT).show();
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