package com.example.projex_mobile.fragments;

import android.content.Context;
import android.content.Intent;
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
import com.example.projex_mobile.fragments.AuthFragment.RePasswordFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class AccountFragment extends Fragment {

    private TextView tvFullName, tvEmail, tvAvatarText;

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
        bindUserData();
        handleEvents(view);
    }

    private void listenEditProfileResult() {
        getParentFragmentManager().setFragmentResultListener(
                EditProfileFragment.REQUEST_KEY_EDIT_PROFILE,
                getViewLifecycleOwner(),
                (requestKey, result) -> {
                    currentName = result.getString(EditProfileFragment.KEY_NAME, currentName);
                    currentEmail = result.getString(EditProfileFragment.KEY_EMAIL, currentEmail);
                    currentPhone = result.getString(EditProfileFragment.KEY_PHONE, currentPhone);

                    bindUserData();
                }
        );
    }

    private void initViews(View view) {
        tvFullName = view.findViewById(R.id.tvFullName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAvatarText = view.findViewById(R.id.tvAvatarText);
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
                    .replace(parentView.getId(), new RePasswordFragment())
                    .addToBackStack("RePasswordFragment")
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