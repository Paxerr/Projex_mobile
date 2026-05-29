package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RePasswordFragment extends Fragment {
    private static final String ARG_EMAIL = "email";
    private static final String ARG_CODE = "code";

    public RePasswordFragment() {
        super(R.layout.re_password_fragment);
    }

    public static RePasswordFragment newInstance(String email, String code) {
        RePasswordFragment fragment = new RePasswordFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        args.putString(ARG_CODE, code);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String email = getArguments() == null ? "" : getArguments().getString(ARG_EMAIL, "");
        String code = getArguments() == null ? "" : getArguments().getString(ARG_CODE, "");

        Button btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        EditText edtNewPassword = view.findViewById(R.id.edtNewPassword);
        EditText edtConfirmPassword = view.findViewById(R.id.edtConfirmPassword);

        btnLogin.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack(null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        btnUpdatePassword.setOnClickListener(v -> {
            String newPassword = getText(edtNewPassword);
            String confirmPassword = getText(edtConfirmPassword);

            if (email.isEmpty() || code.isEmpty()) {
                Toast.makeText(requireContext(), "Thiếu thông tin xác thực", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPassword.length() < 6) {
                edtNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                edtConfirmPassword.setError("Mật khẩu nhập lại không khớp");
                return;
            }

            btnUpdatePassword.setEnabled(false);
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("code", code);
            body.put("newPassword", newPassword);

            ApiService apiService = RetrofitClient.getApiService(null);
            apiService.resetPassword(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    btnUpdatePassword.setEnabled(true);

                    if (!response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Không đặt lại được mật khẩu: " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(requireContext(), "Đặt lại mật khẩu thành công", Toast.LENGTH_SHORT).show();
                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack(null,
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    btnUpdatePassword.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
