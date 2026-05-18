package com.example.projex_mobile;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class RoleActivity extends AppCompatActivity {

    private MaterialCardView cardAdmin, cardMember, cardOwner, cardDelete;
    private View radioAdmin, radioMember, radioOwner;
    private ImageView btnBack, btnSave;

    private String selectedRole = "Member";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.role_activity);

        initViews();
        handleEvents();

        selectRole("Member");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);

        cardAdmin = findViewById(R.id.cardAdmin);
        cardMember = findViewById(R.id.cardMember);
        cardOwner = findViewById(R.id.cardOwner);
        cardDelete = findViewById(R.id.cardDelete);

        radioAdmin = findViewById(R.id.radioAdmin);
        radioMember = findViewById(R.id.radioMember);
        radioOwner = findViewById(R.id.radioOwner);
    }

    private void handleEvents() {
        btnBack.setOnClickListener(v -> finish());

        cardAdmin.setOnClickListener(v -> selectRole("Admin"));
        cardMember.setOnClickListener(v -> selectRole("Member"));
        cardOwner.setOnClickListener(v -> selectRole("Owner"));

        btnSave.setOnClickListener(v -> {
            Toast.makeText(this, "Đã lưu quyền: " + selectedRole, Toast.LENGTH_SHORT).show();
            finish();
        });

        cardDelete.setOnClickListener(v -> showDeleteConfirmDialog());
    }

    private void selectRole(String role) {
        selectedRole = role;

        radioAdmin.setBackgroundResource(R.drawable.role_black_dot);
        radioMember.setBackgroundResource(R.drawable.role_black_dot);
        radioOwner.setBackgroundResource(R.drawable.role_black_dot);

        cardAdmin.setStrokeColor(getColorCompat(R.color.role_card_normal));
        cardAdmin.setStrokeWidth(dpToPx(1));

        cardMember.setStrokeColor(getColorCompat(R.color.role_card_normal));
        cardMember.setStrokeWidth(dpToPx(1));

        cardOwner.setStrokeColor(getColorCompat(R.color.role_card_normal));
        cardOwner.setStrokeWidth(dpToPx(1));

        if (role.equals("Admin")) {
            radioAdmin.setBackgroundResource(R.drawable.role_white_dot);
            cardAdmin.setStrokeColor(getColorCompat(R.color.role_card_selected));
            cardAdmin.setStrokeWidth(dpToPx(2));
        } else if (role.equals("Member")) {
            radioMember.setBackgroundResource(R.drawable.role_white_dot);
            cardMember.setStrokeColor(getColorCompat(R.color.role_card_selected));
            cardMember.setStrokeWidth(dpToPx(2));
        } else if (role.equals("Owner")) {
            radioOwner.setBackgroundResource(R.drawable.role_white_dot);
            cardOwner.setStrokeColor(getColorCompat(R.color.role_card_selected));
            cardOwner.setStrokeWidth(dpToPx(2));
        }
    }

    private void showDeleteConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa thành viên")
                .setMessage("Bạn có chắc muốn xóa thành viên này khỏi dự án không?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Toast.makeText(this, "Đã xóa thành viên", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .show();
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private int getColorCompat(int colorId) {
        return getResources().getColor(colorId, getTheme());
    }
}