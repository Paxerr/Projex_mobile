package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;

public class ResetPasswordFragment extends Fragment {

    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;

    public ResetPasswordFragment() {
        super(R.layout.fragment_reset_password);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        handleEvents(view);
    }

    private void initViews(View view) {
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
    }

    private void handleEvents(View view) {
        View btnConfirm = view.findViewById(R.id.btnConfirm);
        View btnCancel = view.findViewById(R.id.btnCancel);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> closeFragment());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> validateAndChangePassword());
        }
    }

    private void validateAndChangePassword() {
        String currentPassword = getText(etCurrentPassword);
        String newPassword = getText(etNewPassword);
        String confirmPassword = getText(etConfirmPassword);

        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Vui lòng nhập mật khẩu hiện tại");
            etCurrentPassword.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Vui lòng nhập mật khẩu mới");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Mật khẩu mới phải có ít nhất 6 ký tự");
            etNewPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Vui lòng nhập lại mật khẩu mới");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Mật khẩu nhập lại không khớp");
            etConfirmPassword.requestFocus();
            return;
        }

        // Logic placeholder for changing password success
        Toast.makeText(requireContext(), "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        closeFragment();
    }

    private void closeFragment() {
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    private String getText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return editText.getText().toString().trim();
    }
}
