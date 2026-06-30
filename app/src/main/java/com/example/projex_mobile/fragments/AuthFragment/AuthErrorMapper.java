package com.example.projex_mobile.fragments.AuthFragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import retrofit2.Response;

final class AuthErrorMapper {
    static final int EMAIL_MAX_LENGTH = 255;
    static final int PASSWORD_MIN_LENGTH = 6;
    static final int PASSWORD_MAX_LENGTH = 100;

    private AuthErrorMapper() {
    }

    static String fromResponse(Response<?> response, String fallback) {
        try {
            if (response.errorBody() == null) {
                return fallback;
            }

            String errorJson = response.errorBody().string();
            JsonObject error = JsonParser.parseString(errorJson).getAsJsonObject();

            if (error.has("message")) {
                return mapMessage(error.get("message").getAsString(), fallback);
            }

            if (error.has("errors") && error.get("errors").isJsonObject()) {
                JsonObject errors = error.getAsJsonObject("errors");

                String fieldError = getValidationError(errors, "Email");
                if (fieldError != null) return fieldError;

                fieldError = getValidationError(errors, "Password");
                if (fieldError != null) return fieldError;

                fieldError = getValidationError(errors, "NewPassword");
                if (fieldError != null) return fieldError;

                fieldError = getValidationError(errors, "Code");
                if (fieldError != null) return fieldError;

                fieldError = getValidationError(errors, "FullName");
                if (fieldError != null) return fieldError;
            }
        } catch (IOException | IllegalStateException ignored) {
        }

        return fallback;
    }

    static String mapMessage(String message, String fallback) {
        if (message == null || message.trim().isEmpty()) {
            return fallback;
        }

        if ("Email does not exist.".equalsIgnoreCase(message)) {
            return "Email này chưa được đăng ký tài khoản";
        }

        if ("Invalid or expired reset code.".equalsIgnoreCase(message)) {
            return "Mã xác thực không đúng hoặc đã hết hạn";
        }

        if ("Invalid email or password.".equalsIgnoreCase(message)) {
            return "Email hoặc mật khẩu không đúng";
        }

        if ("Account is inactive.".equalsIgnoreCase(message)) {
            return "Tài khoản đang bị khóa hoặc chưa được kích hoạt";
        }

        if ("Email already exists.".equalsIgnoreCase(message)) {
            return "Email này đã được đăng ký";
        }

        if ("Could not send reset code email.".equalsIgnoreCase(message)) {
            return "Không gửi được mã xác thực tới email";
        }

        if ("User not found.".equalsIgnoreCase(message)) {
            return "Không tìm thấy tài khoản";
        }

        return message;
    }

    private static String getValidationError(JsonObject errors, String fieldName) {
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

    private static String mapValidationMessage(String fieldName, String backendMessage) {
        String normalized = backendMessage.toLowerCase();

        if ("Email".equals(fieldName)) {
            if (normalized.contains("required")) return "Vui lòng nhập email";
            if (normalized.contains("valid e-mail")) return "Email không đúng định dạng";
            if (normalized.contains("maximum length")) return "Email không được vượt quá 255 ký tự";
            return "Email không hợp lệ";
        }

        if ("Password".equals(fieldName) || "NewPassword".equals(fieldName)) {
            if (normalized.contains("required")) return "Vui lòng nhập mật khẩu";
            if (normalized.contains("minimum length")) return "Mật khẩu phải có ít nhất 6 ký tự";
            if (normalized.contains("maximum length")) return "Mật khẩu không được vượt quá 100 ký tự";
            return "Mật khẩu không hợp lệ";
        }

        if ("Code".equals(fieldName)) {
            if (normalized.contains("required")) return "Vui lòng nhập mã xác thực";
            if (normalized.contains("regular expression")) return "Mã xác thực phải gồm đúng 4 chữ số";
            return "Mã xác thực không hợp lệ";
        }

        if ("FullName".equals(fieldName)) {
            if (normalized.contains("required")) return "Vui lòng nhập đầy đủ họ tên";
            if (normalized.contains("maximum length")) return "Họ tên không được vượt quá 255 ký tự";
            return "Họ tên không hợp lệ";
        }

        return backendMessage;
    }
}
