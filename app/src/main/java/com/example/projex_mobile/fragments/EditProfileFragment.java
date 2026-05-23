package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class EditProfileFragment extends Fragment {

    public static final String REQUEST_KEY_EDIT_PROFILE = "edit_profile_result";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";

    private TextView tvAvatar;
    private TextInputEditText etName, etEmail, etPhone;

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    public static EditProfileFragment newInstance(String name, String email, String phone) {
        EditProfileFragment fragment = new EditProfileFragment();

        Bundle args = new Bundle();
        args.putString(KEY_NAME, name);
        args.putString(KEY_EMAIL, email);
        args.putString(KEY_PHONE, phone);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        receiveDataFromAccount();
        handleEvents(view);
        handleTextBoxEvents();
    }

    private void initViews(View view) {
        tvAvatar = view.findViewById(R.id.tvAvatar);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
    }

    private void receiveDataFromAccount() {
        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        String name = args.getString(KEY_NAME);
        String email = args.getString(KEY_EMAIL);
        String phone = args.getString(KEY_PHONE);

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
    }

    private void handleEvents(View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        View btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        View btnSave = view.findViewById(R.id.btnSave);
        View btnCancel = view.findViewById(R.id.btnCancel);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> closeFragment());
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> closeFragment());
        }

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Chức năng đổi ảnh đại diện", Toast.LENGTH_SHORT).show()
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

        Bundle result = new Bundle();
        result.putString(KEY_NAME, name);
        result.putString(KEY_EMAIL, email);
        result.putString(KEY_PHONE, phone);

        getParentFragmentManager().setFragmentResult(REQUEST_KEY_EDIT_PROFILE, result);

        Toast.makeText(requireContext(), "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();

        closeFragment();
    }

    private void closeFragment() {
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
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