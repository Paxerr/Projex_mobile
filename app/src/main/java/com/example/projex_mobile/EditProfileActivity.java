package com.example.projex_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity {

    private TextView tvAvatar;
    private TextInputEditText etName, etEmail, etPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile_activity);

        initViews();
        receiveDataFromAccount();
        handleEvents();
        handleTextBoxEvents();
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
    }

    private void receiveDataFromAccount() {
        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        String email = intent.getStringExtra("email");
        String phone = intent.getStringExtra("phone");

        if (name != null) {
            etName.setText(name);
            tvAvatar.setText(makeAvatarText(name));
        }

        if (email != null) {
            etEmail.setText(email);
        }

        if (phone != null) {
            etPhone.setText(phone);
        }

        // Nếu bạn muốn khóa email không cho sửa thì bỏ comment dòng dưới:
        // etEmail.setEnabled(false);
    }

    private void handleEvents() {
        View btnBack = findViewById(R.id.btnBack);
        View btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        View btnSave = findViewById(R.id.btnSave);
        View btnCancel = findViewById(R.id.btnCancel);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> finish());
        }

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v ->
                    Toast.makeText(this, "Chức năng đổi ảnh đại diện", Toast.LENGTH_SHORT).show()
            );
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }
    }

    private void handleTextBoxEvents() {
        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String name = getText(etName);

                if (name.isEmpty()) {
                    etName.setError("Vui lòng nhập họ và tên");
                }
            }
        });

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = getText(etEmail);

                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Email không hợp lệ");
                }
            }
        });

        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String phone = getText(etPhone);

                if (phone.isEmpty()) {
                    etPhone.setError("Vui lòng nhập số điện thoại");
                }
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAvatar.setText(makeAvatarText(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void saveProfile() {
        String name = getText(etName);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập họ và tên");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        Intent resultIntent = new Intent();
        resultIntent.putExtra("name", name);
        resultIntent.putExtra("email", email);
        resultIntent.putExtra("phone", phone);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
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