package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;

import java.util.Locale;

public class RoleFragment extends Fragment {

    public static final String ARG_MEMBER_NAME = "member_name";
    public static final String ARG_MEMBER_EMAIL = "member_email";
    public static final String ARG_MEMBER_ROLE = "member_role";
    public static final String ARG_JOIN_DATE = "join_date";
    public static final String ARG_STATUS = "status";

    private TextView btnBack, btnSave;
    private TextView tvAvatar, tvMemberName, tvMemberEmail, tvStatusValue, tvJoinDateValue;

    private LinearLayout cardAdmin, cardMember, cardOwner, cardDeleteMember;
    private RadioButton rbAdmin, rbMember, rbOwner;

    private String currentRole = "member";

    public RoleFragment() {
        super(R.layout.fragment_role);
    }

    public static RoleFragment newInstance(String memberName,
                                           String memberEmail,
                                           String memberRole,
                                           String joinDate,
                                           String status) {
        RoleFragment fragment = new RoleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MEMBER_NAME, memberName);
        args.putString(ARG_MEMBER_EMAIL, memberEmail);
        args.putString(ARG_MEMBER_ROLE, memberRole);
        args.putString(ARG_JOIN_DATE, joinDate);
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        bindData();
        handleEvents();
    }

    private void initViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnSave = view.findViewById(R.id.btnSave);

        tvAvatar = view.findViewById(R.id.tvAvatar);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        tvMemberEmail = view.findViewById(R.id.tvMemberEmail);
        tvStatusValue = view.findViewById(R.id.tvStatusValue);
        tvJoinDateValue = view.findViewById(R.id.tvJoinDateValue);

        cardAdmin = view.findViewById(R.id.cardAdmin);
        cardMember = view.findViewById(R.id.cardMember);
        cardOwner = view.findViewById(R.id.cardOwner);
        cardDeleteMember = view.findViewById(R.id.cardDeleteMember);

        rbAdmin = view.findViewById(R.id.rbAdmin);
        rbMember = view.findViewById(R.id.rbMember);
        rbOwner = view.findViewById(R.id.rbOwner);
    }

    private void bindData() {
        Bundle args = getArguments();

        String memberName = "Đăng Nguyễn";
        String memberEmail = "dang.nguyen@company.com";
        String joinDate = "12/05/2023";
        String status = "● Đang hoạt động";

        if (args != null) {
            memberName = getSafeValue(args.getString(ARG_MEMBER_NAME), memberName);
            memberEmail = getSafeValue(args.getString(ARG_MEMBER_EMAIL), memberEmail);
            currentRole = getSafeValue(args.getString(ARG_MEMBER_ROLE), "member");
            joinDate = getSafeValue(args.getString(ARG_JOIN_DATE), joinDate);
            status = getSafeValue(args.getString(ARG_STATUS), status);
        }

        tvMemberName.setText(memberName);
        tvMemberEmail.setText(memberEmail);
        tvJoinDateValue.setText("🗓 " + joinDate);
        tvStatusValue.setText(status);
        tvAvatar.setText(makeAvatarText(memberName));

        updateSelectedRole(currentRole);
    }

    private void handleEvents() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v ->
                    requireActivity()
                            .getSupportFragmentManager()
                            .popBackStack()
            );
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> {
                Toast.makeText(requireContext(), "Đã lưu quyền: " + currentRole, Toast.LENGTH_SHORT).show();
                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();
            });
        }

        if (cardAdmin != null) {
            cardAdmin.setOnClickListener(v -> updateSelectedRole("admin"));
        }

        if (cardMember != null) {
            cardMember.setOnClickListener(v -> updateSelectedRole("member"));
        }

        if (cardOwner != null) {
            cardOwner.setOnClickListener(v -> updateSelectedRole("owner"));
        }

        if (rbAdmin != null) {
            rbAdmin.setOnClickListener(v -> updateSelectedRole("admin"));
        }

        if (rbMember != null) {
            rbMember.setOnClickListener(v -> updateSelectedRole("member"));
        }

        if (rbOwner != null) {
            rbOwner.setOnClickListener(v -> updateSelectedRole("owner"));
        }

        if (cardDeleteMember != null) {
            cardDeleteMember.setOnClickListener(v ->
                    Toast.makeText(requireContext(), "Đã chọn xóa thành viên", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void updateSelectedRole(String role) {
        currentRole = role;

        rbAdmin.setChecked("admin".equals(role));
        rbMember.setChecked("member".equals(role));
        rbOwner.setChecked("owner".equals(role));
    }

    private String getSafeValue(String value, String defaultValue) {
        return TextUtils.isEmpty(value) ? defaultValue : value;
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