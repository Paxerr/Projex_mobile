package com.example.projex_mobile;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class AccountActivity extends AppCompatActivity {

    private TextView tvFullName, tvEmail, tvAvatarText;

    private String currentName = "Tiến Đạt Đinh";
    private String currentEmail = "dinhtiendat2105@gmail.com";
    private String currentPhone = "+84 901 234 567";

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();

                            currentName = data.getStringExtra("name");
                            currentEmail = data.getStringExtra("email");
                            currentPhone = data.getStringExtra("phone");

                            bindUserData();

                            Toast.makeText(this, "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_activity);

        initViews();
        bindUserData();
        handleEvents();
    }

    private void initViews() {
        tvFullName = findViewById(R.id.tvFullName);
        tvEmail = findViewById(R.id.tvEmail);
        tvAvatarText = findViewById(R.id.tvAvatarText);
    }

    private void bindUserData() {
        tvFullName.setText(currentName);
        tvEmail.setText(currentEmail);
        tvAvatarText.setText(makeAvatarText(currentName));
    }

    private void handleEvents() {
        MaterialCardView btnBack = findViewById(R.id.btnBack);
        MaterialCardView btnEditProfile = findViewById(R.id.btnEditProfile);
        MaterialCardView cardChangePassword = findViewById(R.id.cardChangePassword);
        MaterialCardView cardLogout = findViewById(R.id.cardLogout);

        SwitchMaterial switchNotifications = findViewById(R.id.switchNotifications);

        View bottomHome = findViewById(R.id.nav_home);
        View bottomSpaces = findViewById(R.id.nav_spaces);
        View bottomTasks = findViewById(R.id.nav_tasks);
        View bottomNotification = findViewById(R.id.nav_notifications);
        View bottomUser = findViewById(R.id.nav_user);
        btnBack.setOnClickListener(v -> finish());

        // Nút bút chì góc phải: chuyển sang trang edit_profile_activity
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(AccountActivity.this, EditProfileActivity.class);

            intent.putExtra("name", currentName);
            intent.putExtra("email", currentEmail);
            intent.putExtra("phone", currentPhone);

            editProfileLauncher.launch(intent);
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Đã bật thông báo", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
            }
        });

        cardChangePassword.setOnClickListener(v -> {
            Toast.makeText(this, "Mở chức năng đổi mật khẩu", Toast.LENGTH_SHORT).show();

            // Nếu sau này có màn đổi mật khẩu thì dùng:
            // Intent intent = new Intent(AccountActivity.this, ChangePasswordActivity.class);
            // startActivity(intent);
        });

        cardLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            // Nếu bạn đã có LoginActivity thì dùng đoạn này:
            // Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // startActivity(intent);

            finish();
        });


        bottomHome.setOnClickListener(v ->
                Toast.makeText(this, "Chuyển sang Home", Toast.LENGTH_SHORT).show()
        );

        bottomSpaces.setOnClickListener(v ->
                Toast.makeText(this, "Chuyển sang Spaces", Toast.LENGTH_SHORT).show()
        );

        bottomTasks.setOnClickListener(v ->
                Toast.makeText(this, "Chuyển sang Tasks", Toast.LENGTH_SHORT).show()
        );

        bottomNotification.setOnClickListener(v ->
                Toast.makeText(this, "Chuyển sang Notification", Toast.LENGTH_SHORT).show()
        );

        bottomUser.setOnClickListener(v ->
                Toast.makeText(this, "Bạn đang ở trang tài khoản", Toast.LENGTH_SHORT).show()
        );
    }

    private String makeAvatarText(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }

        String[] words = name.trim().split("\\s+");

        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length()))
                    .toUpperCase(new Locale("vi", "VN"));
        }

        String firstChar = words[0].substring(0, 1);
        String lastChar = words[words.length - 1].substring(0, 1);

        return (firstChar + lastChar).toUpperCase(new Locale("vi", "VN"));
    }
}