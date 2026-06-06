package com.example.projex_mobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        changePassword(currentPassword, newPassword);
    }

    private void changePassword(String currentPassword, String newPassword) {
        String token = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("token", "");

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Thiếu token đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        Map<String, String> body = new HashMap<>();
        body.put("currentPassword", currentPassword);
        body.put("newPassword", newPassword);

        ApiService apiService = RetrofitClient.getApiService(authHeader);
        apiService.changePassword(authHeader, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    closeFragment();
                    return;
                }

                if (response.code() == 400) {
                    etCurrentPassword.setError("Mật khẩu hiện tại không đúng");
                    etCurrentPassword.requestFocus();
                    return;
                }

                if (response.code() == 401 || response.code() == 403) {
                    Toast.makeText(requireContext(), "Phiên đăng nhập đã hết hạn", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(requireContext(), "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                if (!isAdded()) {
                    return;
                }

                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
