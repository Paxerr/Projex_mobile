package com.example.projex_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ThemThanhVienActivity extends AppCompatActivity {

    private View modalOverlay;
    private View modalCard;
    private EditText edtEmailOrName;
    private TextView tvRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_thanh_vien);

        modalOverlay = findViewById(R.id.modalOverlay);
        modalCard = findViewById(R.id.modalCard);
        edtEmailOrName = findViewById(R.id.edtEmailOrName);
        tvRole = findViewById(R.id.tvRole);

        ImageView btnClose = findViewById(R.id.btnClose);
        TextView btnSend = findViewById(R.id.btnSend);
        TextView btnCancel = findViewById(R.id.btnCancel);
        TextView btnAdd = findViewById(R.id.btnAdd);

        View roleBox = findViewById(R.id.roleBox);
        TextView tvTitleTop = findViewById(R.id.tvTitleTop);

        StringBuilder missing = new StringBuilder();
        if (modalOverlay == null) missing.append(" modalOverlay");
        if (modalCard == null) missing.append(" modalCard");
        if (edtEmailOrName == null) missing.append(" edtEmailOrName");
        if (tvRole == null) missing.append(" tvRole");
        if (btnClose == null) missing.append(" btnClose");
        if (btnSend == null) missing.append(" btnSend");
        if (btnCancel == null) missing.append(" btnCancel");
        if (btnAdd == null) missing.append(" btnAdd");
        if (roleBox == null) missing.append(" roleBox");
        if (tvTitleTop == null) missing.append(" tvTitleTop");

        if (missing.length() > 0) {
            Toast.makeText(this, "Thiếu view:" + missing, Toast.LENGTH_LONG).show();
        }

        // Luôn hiện modal khi mở màn này
        if (modalOverlay != null) {
            showModal(true);
        }

        // Nếu overlay đang ẩn, bấm "Thêm t.viên" sẽ mở modal.
        if (tvTitleTop != null) {
            tvTitleTop.setOnClickListener(v -> showModal(true));
        }

        // Không đóng khi bấm nền để tránh cảm giác "vừa vào đã thoát".
        if (modalOverlay != null) {
            modalOverlay.setOnClickListener(v -> { });
        }
        // Chặn click xuyên qua card.
        if (modalCard != null) {
            modalCard.setOnClickListener(v -> {});
        }

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> showModal(false));
        }

        if (roleBox != null) {
            roleBox.setOnClickListener(v -> showRoleDropdown(v));
        }

        View.OnClickListener submit = v -> inviteMember();
        if (btnAdd != null) {
            btnAdd.setOnClickListener(submit);
        }
        if (btnSend != null) {
            btnSend.setOnClickListener(submit);
        }

        // Hủy: quay về trang team_activity.xml
        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> {
                Intent intent = new Intent(ThemThanhVienActivity.this, TeamActivity.class);
                // Tránh tạo nhiều bản TeamActivity nếu user quay lại nhiều lần
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    private void showModal(boolean show) {
        modalOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showRoleDropdown(View anchor) {
        PopupMenu pm = new PopupMenu(this, anchor);
        pm.getMenuInflater().inflate(R.menu.menu_role, pm.getMenu());
        pm.setOnMenuItemClickListener(item -> {
            tvRole.setText(item.getTitle());
            return true;
        });
        pm.show();
    }

    private void inviteMember() {
        if (edtEmailOrName == null || tvRole == null) {
            Toast.makeText(this, "Thiếu thành phần nhập liệu", Toast.LENGTH_SHORT).show();
            return;
        }
        String email = edtEmailOrName.getText() == null ? "" : edtEmailOrName.getText().toString().trim();
        String role = tvRole.getText() == null ? "Member" : tvRole.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Mời " + email + " với role " + role + " thành công", Toast.LENGTH_SHORT).show();
        showModal(false);
    }
}

