package com.example.projex_mobile.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.EditProfileActivity;
import com.example.projex_mobile.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class AccountFragment extends Fragment {

    private TextView tvFullName, tvEmail, tvAvatarText;

    private String currentName = "Tiến Đạt Đinh";
    private String currentEmail = "dinhtiendat2105@gmail.com";
    private String currentPhone = "+84 901 234 567";

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();

                            currentName = data.getStringExtra("name");
                            currentEmail = data.getStringExtra("email");
                            currentPhone = data.getStringExtra("phone");

                            bindUserData();

                            if (isAdded()) {
                                Toast.makeText(requireContext(), "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
            );

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

        initViews(view);
        bindUserData();
        handleEvents(view);
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
        MaterialCardView btnBack = view.findViewById(R.id.btnBack);
        MaterialCardView btnEditProfile = view.findViewById(R.id.btnEditProfile);
        MaterialCardView cardChangePassword = view.findViewById(R.id.cardChangePassword);
        MaterialCardView cardLogout = view.findViewById(R.id.cardLogout);
        SwitchMaterial switchNotifications = view.findViewById(R.id.switchNotifications);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack());
        }

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> openEditProfileScreen());
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
            cardChangePassword.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Mở chức năng đổi mật khẩu", Toast.LENGTH_SHORT).show()
            );
        }

        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            });
        }
    }

    public void onEditProfileClick(View view) {
        openEditProfileScreen();
    }

    private void openEditProfileScreen() {
        try {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            intent.putExtra("name", currentName);
            intent.putExtra("email", currentEmail);
            intent.putExtra("phone", currentPhone);
            editProfileLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không mở được màn chỉnh sửa hồ sơ", Toast.LENGTH_SHORT).show();
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
