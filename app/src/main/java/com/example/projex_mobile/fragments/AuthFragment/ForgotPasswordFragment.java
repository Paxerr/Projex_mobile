package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
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

public class ForgotPasswordFragment extends Fragment {

    public ForgotPasswordFragment() {
        super(R.layout.forgot_password_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnBack = view.findViewById(R.id.btnBack);
        Button btnNext = view.findViewById(R.id.btnNext);
        EditText edtEmail = view.findViewById(R.id.edtEmailorName);

        btnBack.setOnClickListener(v -> requireActivity()
                .getSupportFragmentManager()
                .popBackStack());

        btnNext.setOnClickListener(v -> {
            String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();

            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email");
                edtEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không đúng định dạng");
                edtEmail.requestFocus();
                return;
            }

            if (email.length() > AuthErrorMapper.EMAIL_MAX_LENGTH) {
                edtEmail.setError("Email không được vượt quá 255 ký tự");
                edtEmail.requestFocus();
                return;
            }

            btnNext.setEnabled(false);
            Map<String, String> body = new HashMap<>();
            body.put("email", email);

            ApiService apiService = RetrofitClient.getApiService(null);
            apiService.forgotPassword(body).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    btnNext.setEnabled(true);
                    if (!isAdded()) return;

                    if (!response.isSuccessful()) {
                        String errorMessage = AuthErrorMapper.fromResponse(response, "Không gửi được mã xác thực");
                        if (errorMessage.toLowerCase().contains("email")) {
                            edtEmail.setError(errorMessage);
                            edtEmail.requestFocus();
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(requireContext(), "Mã xác thực đã được gửi tới email", Toast.LENGTH_SHORT).show();
                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.authOverlayContainer, VerifyFragment.newInstance(email))
                            .addToBackStack("verify_fragment")
                            .commit();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    btnNext.setEnabled(true);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });
    }
}
