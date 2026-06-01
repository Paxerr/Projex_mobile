package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.AuthActivity;
import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.example.projex_mobile.objects.DashboardOverview;
import com.example.projex_mobile.objects.User;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountFragment extends Fragment {

    private TextView tvFullName, tvEmail, tvAvatarText;
    private TextView tvTasksValue, tvProjectsValue, tvOnTimeValue;

    private String currentName = "Tiến Đạt Đinh";
    private String currentEmail = "dinhtiendat2105@gmail.com";
    private String currentPhone = "+84 901 234 567";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listenEditProfileResult();
        initViews(view);
        loadProfileData();
        loadDashboardOverview();
        handleEvents(view);
    }


    private void listenEditProfileResult() {
        getParentFragmentManager().setFragmentResultListener(
                EditProfileFragment.REQUEST_KEY_EDIT_PROFILE,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    String newName = result.getString(EditProfileFragment.KEY_NAME, currentName);
                    String newEmail = result.getString(EditProfileFragment.KEY_EMAIL, currentEmail);
                    String newPhone = result.getString(EditProfileFragment.KEY_PHONE, currentPhone);

                    updateProfileApi(newName, newEmail, newPhone);
                }
        );
    }

    private void initViews(View view) {
        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAvatarText = view.findViewById(R.id.tvAvatarText);

        tvTasksValue = view.findViewById(R.id.tvTasksValue);
        tvProjectsValue = view.findViewById(R.id.tvProjectsValue);
        tvOnTimeValue = view.findViewById(R.id.tvOnTimeValue);
    }

    private void bindUserData() {
        if (tvFullName != null) {
            tvFullName.setText(currentName);
        }

        if (tvEmail != null) {
            tvEmail.setText(currentEmail);
        }

        if (tvAvatarText != null) {
            tvAvatarText.setText(makeAvatarText(currentName));
        }
    }

    private String getAuthToken() {
        if (getContext() == null) return null;
        return getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("token", null);
    }

    private void loadProfileData() {
        String token = getAuthToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    currentName = user.getFullName();
                    currentEmail = user.getEmail();
                    currentPhone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";
                    bindUserData();

                    if (getContext() != null) {
                        getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putString("user_name", currentName)
                                .putString("user_email", currentEmail)
                                .apply();
                    }
                } else {
                    Toast.makeText(requireContext(), "Không thể tải thông tin cá nhân", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi kết nối tải thông tin: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDashboardOverview() {
        String token = getAuthToken();
        if (token == null) return;

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.getDashboardOverview(token).enqueue(new Callback<DashboardOverview>() {
            @Override
            public void onResponse(@NonNull Call<DashboardOverview> call, @NonNull Response<DashboardOverview> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    DashboardOverview overview = response.body();
                    if (tvTasksValue != null) {
                        tvTasksValue.setText(String.valueOf(overview.getMyTasks()));
                    }
                    if (tvProjectsValue != null) {
                        tvProjectsValue.setText(String.valueOf(overview.getMyProjects()));
                    }
                    if (tvOnTimeValue != null) {
                        tvOnTimeValue.setText((int) overview.getOnTimeRate() + "%");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<DashboardOverview> call, @NonNull Throwable t) {
                // Fail silently
            }
        });
    }

    private void updateProfileApi(String newName, String newEmail, String newPhone) {
        String token = getAuthToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("email", newEmail);
        body.put("fullName", newName);
        body.put("phoneNumber", newPhone);

        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.updateProfile(token, body).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (!isAdded()) return;

                if (response.isSuccessful()) {
                    currentName = newName;
                    currentEmail = newEmail;
                    currentPhone = newPhone;
                    bindUserData();

                    if (getContext() != null) {
                        getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                .edit()
                                .putString("user_name", currentName)
                                .putString("user_email", currentEmail)
                                .apply();
                    }

                    Toast.makeText(requireContext(), "Cập nhật hồ sơ thành công", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Không thể cập nhật hồ sơ";
                    try {
                        if (response.errorBody() != null) {
                            String errorJson = response.errorBody().string();
                            JsonObject errorObj = JsonParser.parseString(errorJson).getAsJsonObject();
                            if (errorObj.has("message")) {
                                errorMsg = errorObj.get("message").getAsString();
                            }
                        }
                    } catch (Exception ignored) {}
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Lỗi kết nối lưu hồ sơ: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleEvents(View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        View btnEditProfile = view.findViewById(R.id.btnEditProfile);
        ImageView icEditProfile = view.findViewById(R.id.icEditProfile);

        View cardChangePassword = view.findViewById(R.id.cardChangePassword);
        View cardLogout = view.findViewById(R.id.cardLogout);

        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack());
        }

        View.OnClickListener editProfileClickListener = v -> openEditProfileFragment();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(editProfileClickListener);
        }

        if (icEditProfile != null) {
            icEditProfile.setOnClickListener(editProfileClickListener);
        }

        if (switchNotifications != null) {
            switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    Toast.makeText(requireContext(), "Đã bật thông báo", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Đã tắt thông báo", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (cardChangePassword != null) {
            cardChangePassword.setOnClickListener(v -> openRePasswordFragment());
        }

        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> logoutAndOpenLogin());
        }
    }

    public void onEditProfileClick(View view) {
        openEditProfileFragment();
    }

    private void openEditProfileFragment() {
        try {
            View parentView = (View) requireView().getParent();

            if (parentView == null || parentView.getId() == View.NO_ID) {
                Toast.makeText(requireContext(), "Không tìm thấy khung chứa Fragment", Toast.LENGTH_SHORT).show();
                return;
            }

            EditProfileFragment editProfileFragment = EditProfileFragment.newInstance(
                    currentName,
                    currentEmail,
                    currentPhone
            );

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(parentView.getId(), editProfileFragment)
                    .addToBackStack("EditProfileFragment")
                    .commit();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không mở được màn chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRePasswordFragment() {
        try {
            View parentView = (View) requireView().getParent();

            if (parentView == null || parentView.getId() == View.NO_ID) {
                Toast.makeText(requireContext(), "Không tìm thấy khung chứa Fragment", Toast.LENGTH_SHORT).show();
                return;
            }

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(parentView.getId(), new ResetPasswordFragment())
                    .addToBackStack("ResetPasswordFragment")
                    .commit();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không mở được trang đổi mật khẩu", Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutAndOpenLogin() {
        try {
            requireActivity()
                    .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();

            Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);
            requireActivity().finishAffinity();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không thể đăng xuất", Toast.LENGTH_SHORT).show();
        }
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