package com.example.projex_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class TeamActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.team_activity);

        ImageView btnAddMember = findViewById(R.id.btn_add_member);
        btnAddMember.setOnClickListener(v -> openAddMemberScreen());
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
}

