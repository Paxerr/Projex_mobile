package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterFragment extends Fragment {
    private static final int EMAIL_MAX_LENGTH = 255;
    private static final int FULL_NAME_MAX_LENGTH = 255;
    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 100;

    public RegisterFragment() {
        super(R.layout.register_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtFullName = view.findViewById(R.id.edtFullName);
        EditText edtPassword = view.findViewById(R.id.edtPassword);
        EditText edtEmail = view.findViewById(R.id.edtEmail);
        CheckBox cbTerms = view.findViewById(R.id.cbTerms);
        View tvTerms = view.findViewById(R.id.tvTerms);
        View tvTermsLink = view.findViewById(R.id.tvTermsLink);
        Button btnRegister = view.findViewById(R.id.btnRegister);

        View.OnClickListener toggleTerms = v -> cbTerms.setChecked(!cbTerms.isChecked());
        if (tvTerms != null) tvTerms.setOnClickListener(toggleTerms);
        if (tvTermsLink != null) tvTermsLink.setOnClickListener(toggleTerms);

        btnRegister.setOnClickListener(v -> {
            String fullName = edtFullName.getText() == null ? "" : edtFullName.getText().toString().trim();
            String password = edtPassword.getText() == null ? "" : edtPassword.getText().toString();
            String email = edtEmail.getText() == null ? "" : edtEmail.getText().toString().trim();

            if (!validateInput(email, password, fullName, cbTerms, edtEmail, edtPassword, edtFullName)) {
                return;
            }

            btnRegister.setEnabled(false);

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
                        String errorMessage = getRegisterErrorMessage(response);
                        applyFieldError(errorMessage, edtEmail, edtPassword, edtFullName);
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(requireContext(), "Đăng ký tài khoản thành công.", Toast.LENGTH_SHORT).show();
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

    private boolean validateInput(
            String email,
            String password,
            String fullName,
            CheckBox cbTerms,
            EditText edtEmail,
            EditText edtPassword,
            EditText edtFullName
    ) {
        if (email.isEmpty()) {
            edtEmail.setError("Vui lòng nhập email");
            edtEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không đúng định dạng");
            edtEmail.requestFocus();
            return false;
        }

        if (email.length() > EMAIL_MAX_LENGTH) {
            edtEmail.setError("Email không được vượt quá 255 ký tự");
            edtEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edtPassword.setError("Vui lòng nhập mật khẩu");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() < PASSWORD_MIN_LENGTH) {
            edtPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            edtPassword.requestFocus();
            return false;
        }

        if (password.length() > PASSWORD_MAX_LENGTH) {
            edtPassword.setError("Mật khẩu không được vượt quá 100 ký tự");
            edtPassword.requestFocus();
            return false;
        }

        if (fullName.isEmpty()) {
            edtFullName.setError("Vui lòng nhập đầy đủ họ tên");
            edtFullName.requestFocus();
            return false;
        }

        if (fullName.length() > FULL_NAME_MAX_LENGTH) {
            edtFullName.setError("Họ tên không được vượt quá 255 ký tự");
            edtFullName.requestFocus();
            return false;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(requireContext(), "Bạn cần đồng ý với điều khoản dịch vụ để đăng ký", Toast.LENGTH_SHORT).show();
            cbTerms.requestFocus();
            return false;
        }

        return true;
    }

    private void applyFieldError(String errorMessage, EditText edtEmail, EditText edtPassword, EditText edtFullName) {
        String normalized = errorMessage == null ? "" : errorMessage.toLowerCase();

        if (normalized.contains("email")) {
            edtEmail.setError(errorMessage);
            edtEmail.requestFocus();
        } else if (normalized.contains("mật khẩu") || normalized.contains("password")) {
            edtPassword.setError(errorMessage);
            edtPassword.requestFocus();
        } else if (normalized.contains("họ tên") || normalized.contains("fullname") || normalized.contains("full name")) {
            edtFullName.setError(errorMessage);
            edtFullName.requestFocus();
        }
    }

    private String getRegisterErrorMessage(Response<JsonObject> response) {
        try {
            if (response.errorBody() == null) {
                return "Đăng ký tài khoản không thành công";
            }

            String errorJson = response.errorBody().string();
            JsonObject error = JsonParser.parseString(errorJson).getAsJsonObject();

            if (error.has("message")) {
                return mapBackendMessage(error.get("message").getAsString());
            }

            if (error.has("errors") && error.get("errors").isJsonObject()) {
                JsonObject errors = error.getAsJsonObject("errors");

                String fieldError = getValidationError(errors, "Email");
                if (fieldError != null) return fieldError;

                fieldError = getValidationError(errors, "Password");
                if (fieldError != null) return fieldError;

                fieldError = getValidationError(errors, "FullName");
                if (fieldError != null) return fieldError;
            }
        } catch (IOException | IllegalStateException ignored) {
        }

        return "Đăng ký tài khoản không thành công";
    }

    private String getValidationError(JsonObject errors, String fieldName) {
        if (!errors.has(fieldName)) return null;

        JsonElement value = errors.get(fieldName);
        String backendMessage = null;

        if (value.isJsonArray()) {
            JsonArray messages = value.getAsJsonArray();
            if (!messages.isEmpty()) {
                backendMessage = messages.get(0).getAsString();
            }
        } else if (value.isJsonPrimitive()) {
            backendMessage = value.getAsString();
        }

        if (backendMessage == null) return null;
        return mapValidationMessage(fieldName, backendMessage);
    }

    private String mapBackendMessage(String message) {
        if ("Email already exists.".equalsIgnoreCase(message)) {
            return "Email này đã được đăng ký";
        }

        return message == null || message.trim().isEmpty()
                ? "Đăng ký tài khoản không thành công"
                : message;
    }

    private String mapValidationMessage(String fieldName, String backendMessage) {
        String normalized = backendMessage.toLowerCase();

        if ("Email".equals(fieldName)) {
            if (normalized.contains("required")) return "Vui lòng nhập email";
            if (normalized.contains("valid e-mail")) return "Email không đúng định dạng";
            if (normalized.contains("maximum length")) return "Email không được vượt quá 255 ký tự";
            return "Email không hợp lệ";
        }

        if ("Password".equals(fieldName)) {
            if (normalized.contains("required")) return "Vui lòng nhập mật khẩu";
            if (normalized.contains("minimum length")) return "Mật khẩu phải có ít nhất 6 ký tự";
            if (normalized.contains("maximum length")) return "Mật khẩu không được vượt quá 100 ký tự";
            return "Mật khẩu không hợp lệ";
        }

        if ("FullName".equals(fieldName)) {
            if (normalized.contains("required")) return "Vui lòng nhập đầy đủ họ tên";
            if (normalized.contains("maximum length")) return "Họ tên không được vượt quá 255 ký tự";
            return "Họ tên không hợp lệ";
        }

        return backendMessage;
    }
}
