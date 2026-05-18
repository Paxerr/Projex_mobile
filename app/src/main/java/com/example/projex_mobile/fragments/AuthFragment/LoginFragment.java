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

public class LoginFragment extends Fragment {

    public LoginFragment() {
        super(R.layout.login_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtEmail = view.findViewById(R.id.edtEmail);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);
        TextView tvForgot = view.findViewById(R.id.tvForgot);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();
            String password = edtPassword.getText() == null ? "" : edtPassword.getText().toString().trim();

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

            btnLogin.setEnabled(false);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("email", email);
            requestBody.put("password", password);

            ApiService apiService = RetrofitClient.getApiService(null);
            apiService.login(requestBody).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    btnLogin.setEnabled(true);

                    if (!isAdded()) {
                        return;
                    }

                    if (!response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Đăng nhập thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JsonObject body = response.body();
                    if (body == null) {
                        Toast.makeText(requireContext(), "Server không trả dữ liệu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String token = extractString(body, "token");
                    if (token == null) {
                        token = extractString(body, "accessToken");
                    }

                    JsonObject data = getObject(body, "data");
                    if (token == null && data != null) {
                        token = extractString(data, "token");
                        if (token == null) {
                            token = extractString(data, "accessToken");
                        }
                    }

                    JsonObject userObject = getObject(body, "user");
                    if (userObject == null && data != null) {
                        userObject = getObject(data, "user");
                    }

                    String fullName = userObject != null ? extractString(userObject, "fullName") : null;
                    String responseEmail = userObject != null ? extractString(userObject, "email") : null;

                    if (fullName == null || fullName.isEmpty()) {
                        fullName = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
                    }
                    if (responseEmail == null || responseEmail.isEmpty()) {
                        responseEmail = email;
                    }

                    if (token == null || token.isEmpty()) {
                        String message = extractString(body, "message");
                        if ((message == null || message.isEmpty()) && data != null) {
                            message = extractString(data, "message");
                        }
                        Toast.makeText(
                                requireContext(),
                                message != null ? message : "Đăng nhập thất bại: thiếu token",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    if (!token.startsWith("Bearer ")) {
                        token = "Bearer " + token;
                    }

                    SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putString("token", token)
                            .putString("user_name", fullName)
                            .putString("user_email", responseEmail)
                            .apply();

                    startActivity(new Intent(requireActivity(), HomeActivity.class));
                    requireActivity().finish();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    btnLogin.setEnabled(true);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        tvForgot.setOnClickListener(v -> {
            View authContent = requireActivity().findViewById(R.id.authContent);
            authContent.setVisibility(View.GONE);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authOverlayContainer, new ForgotPasswordFragment())
                    .addToBackStack("forgot_password")
                    .commit();
        });
    }

    private static String extractString(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            return object.get(key).getAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JsonObject getObject(JsonObject object, String key) {
        if (object == null || !object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        try {
            return object.getAsJsonObject(key);
        } catch (Exception ignored) {
            return null;
        }
    }
}
