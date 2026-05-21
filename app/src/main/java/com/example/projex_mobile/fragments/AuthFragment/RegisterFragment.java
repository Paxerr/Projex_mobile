package com.example.projex_mobile.fragments.AuthFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.HomeActivity;
import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {

    public RegisterFragment() {
        super(R.layout.register_fragment);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EditText edtFullName = view.findViewById(R.id.edtFullName);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String fullName = edtFullName.getText() == null ? "" : edtFullName.getText().toString().trim();
            String password = edtPassword.getText() == null ? "" : edtPassword.getText().toString().trim();
            String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();

            //validate
            if (email.isEmpty()) {
                edtEmail.setError("Vui lòng nhập email hoặc tài khoản");
                edtEmail.requestFocus();
                return;
            }

            if (email.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmail.setError("Email không hợp lệ");
                edtEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                edtPassword.setError("Vui lòng nhập mật khẩu");
                edtPassword.requestFocus();
                return;
            }

            if (fullName.isEmpty()) {
                edtFullName.setError("Vui lòng nhập đầy đủ họ tên");
                edtFullName.requestFocus();
                return;
            }

            btnRegister.setEnabled(false);

            //tạo requestbody api

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("fullName", fullName);
            requestBody.put("password", password);


            ApiService apiService = RetrofitClient.getApiService(null);
            apiService.register(requestBody).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    btnRegister.setEnabled(true);

                    if (!isAdded()) return;

                    if (!response.isSuccessful()) {
                        Toast.makeText(requireContext(),
                                "Lỗi ! Đăng ký tài khoản không thành công.",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(requireContext(),"Đăng ký tài khoản thành công.",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    btnRegister.setEnabled(true);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

    }
}
