package com.example.projex_mobile.fragments;

import android.content.Context;
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
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoleFragment extends Fragment {

    public static final String ARG_MEMBER_NAME = "member_name";
    public static final String ARG_MEMBER_EMAIL = "member_email";
    public static final String ARG_MEMBER_ROLE = "member_role";
    public static final String ARG_JOIN_DATE = "join_date";
    public static final String ARG_STATUS = "status";
    public static final String ARG_PROJECT_ID = "project_id";
    public static final String ARG_USER_ID = "user_id";
    public static final String ARG_ADMIN_COUNT = "admin_count";
    public static final String ARG_CURRENT_USER_ROLE = "current_user_role";

    private TextView btnBack, btnSave, tvTitle;
    private TextView tvAvatar, tvMemberName, tvMemberEmail, tvStatusValue, tvJoinDateValue, tvRoleSection;

    private LinearLayout cardAdmin, cardMember, cardOwner, cardDeleteMember;
    private RadioButton rbAdmin, rbMember, rbOwner;

    private String currentRole = "member";
    private int projectId = -1;
    private int userId = -1;
    private int adminCount = 0;
    private String currentUserRole = "Member";

    public RoleFragment() {
        super(R.layout.fragment_role);
    }

    public static RoleFragment newInstance(int projectId,
                                           int userId,
                                           String memberName,
                                           String memberEmail,
                                           String memberRole,
                                           String joinDate,
                                           String status,
                                           int adminCount,
                                           String currentUserRole) {
        RoleFragment fragment = new RoleFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PROJECT_ID, projectId);
        args.putInt(ARG_USER_ID, userId);
        args.putString(ARG_MEMBER_NAME, memberName);
        args.putString(ARG_MEMBER_EMAIL, memberEmail);
        args.putString(ARG_MEMBER_ROLE, memberRole);
        args.putString(ARG_JOIN_DATE, joinDate);
        args.putString(ARG_STATUS, status);
        args.putInt(ARG_ADMIN_COUNT, adminCount);
        args.putString(ARG_CURRENT_USER_ROLE, currentUserRole);
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
        tvTitle = view.findViewById(R.id.tvTitle);

        tvAvatar = view.findViewById(R.id.tvAvatar);
        tvMemberName = view.findViewById(R.id.tvMemberName);
        tvMemberEmail = view.findViewById(R.id.tvMemberEmail);
        tvStatusValue = view.findViewById(R.id.tvStatusValue);
        tvJoinDateValue = view.findViewById(R.id.tvJoinDateValue);
        tvRoleSection = view.findViewById(R.id.tvRoleSection);

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
            projectId = args.getInt(ARG_PROJECT_ID, -1);
            userId = args.getInt(ARG_USER_ID, -1);
            memberName = getSafeValue(args.getString(ARG_MEMBER_NAME), memberName);
            memberEmail = getSafeValue(args.getString(ARG_MEMBER_EMAIL), memberEmail);
            currentRole = getSafeValue(args.getString(ARG_MEMBER_ROLE), "member");
            joinDate = getSafeValue(args.getString(ARG_JOIN_DATE), joinDate);
            status = getSafeValue(args.getString(ARG_STATUS), status);
            adminCount = args.getInt(ARG_ADMIN_COUNT, 0);
            currentUserRole = getSafeValue(args.getString(ARG_CURRENT_USER_ROLE), "Member");
        }

        tvMemberName.setText(memberName);
        tvMemberEmail.setText(memberEmail);
        tvJoinDateValue.setText("🗓 " + joinDate);
        tvStatusValue.setText(status);
        tvAvatar.setText(makeAvatarText(memberName));

        updateSelectedRole(currentRole);
        applyRolePermissions();
    }

    private void applyRolePermissions() {
        boolean isOwner = "Owner".equalsIgnoreCase(currentUserRole);
        boolean isAdmin = "Admin".equalsIgnoreCase(currentUserRole);
        boolean isTargetOwner = "Owner".equalsIgnoreCase(currentRole);
        boolean isTargetAdmin = "Admin".equalsIgnoreCase(currentRole);
        boolean isTargetMember = "Member".equalsIgnoreCase(currentRole) || (!isTargetOwner && !isTargetAdmin);

        // Owner can edit roles of others, but cannot edit themselves
        boolean canEditRole = isOwner && !isTargetOwner;

        if (tvTitle != null) {
            tvTitle.setText(canEditRole ? "Chỉnh sửa quyền" : "Thông tin vai trò");
        }

        if (btnSave != null) {
            btnSave.setVisibility(canEditRole ? View.VISIBLE : View.GONE);
        }

        boolean showRoleOptions = !isAdmin;
        if (tvRoleSection != null) tvRoleSection.setVisibility(showRoleOptions ? View.VISIBLE : View.GONE);
        if (cardAdmin != null) cardAdmin.setVisibility(showRoleOptions ? View.VISIBLE : View.GONE);
        if (cardMember != null) cardMember.setVisibility(showRoleOptions ? View.VISIBLE : View.GONE);
        if (cardOwner != null) cardOwner.setVisibility(showRoleOptions ? View.VISIBLE : View.GONE);

        if (cardAdmin != null) cardAdmin.setEnabled(canEditRole);
        if (cardMember != null) cardMember.setEnabled(canEditRole);
        if (cardOwner != null) cardOwner.setEnabled(false); // ALWAYS disabled to prevent multiple owners
        
        if (rbAdmin != null) rbAdmin.setEnabled(canEditRole);
        if (rbMember != null) rbMember.setEnabled(canEditRole);
        if (rbOwner != null) rbOwner.setEnabled(false); // ALWAYS disabled to prevent multiple owners

        // Owner can delete admins and members. Admin can only delete members.
        boolean canDelete = false;
        if (isOwner) {
            canDelete = !isTargetOwner;
        } else if (isAdmin) {
            canDelete = isTargetMember;
        }

        if (cardDeleteMember != null) {
            cardDeleteMember.setVisibility(canDelete ? View.VISIBLE : View.GONE);
        }
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
            btnSave.setOnClickListener(v -> saveMemberRole());
        }

        if (cardAdmin != null) {
            cardAdmin.setOnClickListener(v -> {
                if (cardAdmin.isEnabled()) updateSelectedRole("admin");
            });
        }

        if (cardMember != null) {
            cardMember.setOnClickListener(v -> {
                if (cardMember.isEnabled()) updateSelectedRole("member");
            });
        }

        if (cardOwner != null) {
            cardOwner.setOnClickListener(v -> {
                if (cardOwner.isEnabled()) updateSelectedRole("owner");
            });
        }

        if (rbAdmin != null) {
            rbAdmin.setOnClickListener(v -> {
                if (rbAdmin.isEnabled()) updateSelectedRole("admin");
            });
        }

        if (rbMember != null) {
            rbMember.setOnClickListener(v -> {
                if (rbMember.isEnabled()) updateSelectedRole("member");
            });
        }

        if (rbOwner != null) {
            rbOwner.setOnClickListener(v -> {
                if (rbOwner.isEnabled()) updateSelectedRole("owner");
            });
        }

        if (cardDeleteMember != null) {
            cardDeleteMember.setOnClickListener(v -> deleteMember());
        }
    }

    private void saveMemberRole() {
        if (projectId == -1 || userId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin thành viên để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("token", "");

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        String roleToSend = "Member";
        if ("admin".equals(currentRole)) {
            roleToSend = "Admin";
        } else if ("owner".equals(currentRole)) {
            roleToSend = "Owner";
        }

        Map<String, String> body = new HashMap<>();
        body.put("role", roleToSend);

        if (btnSave != null) {
            btnSave.setEnabled(false);
        }

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.updateProjectMemberRole(authToken, projectId, userId, body)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;
                        if (btnSave != null) {
                            btnSave.setEnabled(true);
                        }

                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Cập nhật quyền thành công", Toast.LENGTH_SHORT).show();
                            
                            Bundle result = new Bundle();
                            result.putBoolean("success", true);
                            getParentFragmentManager().setFragmentResult("member_added", result);

                            requireActivity()
                                    .getSupportFragmentManager()
                                    .popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Cập nhật thất bại: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;
                        if (btnSave != null) {
                            btnSave.setEnabled(true);
                        }
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteMember() {
        if (projectId == -1 || userId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin thành viên để xóa", Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa thành viên này khỏi dự án?")
                .setPositiveButton("Xóa", (dialog, which) -> executeDeleteMember())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void executeDeleteMember() {
        String token = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("token", "");

        if (token == null || token.trim().isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;

        if (cardDeleteMember != null) {
            cardDeleteMember.setEnabled(false);
        }

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.removeProjectMember(authToken, projectId, userId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;
                        if (cardDeleteMember != null) {
                            cardDeleteMember.setEnabled(true);
                        }

                        if (response.isSuccessful()) {
                            Toast.makeText(requireContext(), "Xóa thành viên thành công", Toast.LENGTH_SHORT).show();

                            Bundle result = new Bundle();
                            result.putBoolean("success", true);
                            getParentFragmentManager().setFragmentResult("member_added", result);

                            requireActivity()
                                    .getSupportFragmentManager()
                                    .popBackStack();
                        } else {
                            Toast.makeText(requireContext(), "Xóa thành viên thất bại: " + response.code(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;
                        if (cardDeleteMember != null) {
                            cardDeleteMember.setEnabled(true);
                        }
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updateSelectedRole(String role) {
        if (role == null) role = "member";
        role = role.trim().toLowerCase();

        // Safeguard: Cannot select owner role if not already owner
        if ("owner".equals(role)) {
            String originalRole = "member";
            if (getArguments() != null) {
                originalRole = getSafeValue(getArguments().getString(ARG_MEMBER_ROLE), "member").toLowerCase();
            }
            if (!"owner".equals(originalRole)) {
                Toast.makeText(requireContext(), "Dự án chỉ có duy nhất 1 Owner.", Toast.LENGTH_SHORT).show();
                rbOwner.setChecked(false);
                rbMember.setChecked(true);
                currentRole = "member";
                return;
            }
        }

        if ("admin".equals(role)) {
            String originalRole = "member";
            if (getArguments() != null) {
                originalRole = getSafeValue(getArguments().getString(ARG_MEMBER_ROLE), "member").toLowerCase();
            }
            if (!"admin".equals(originalRole)) {
                if (adminCount >= 3) {
                    Toast.makeText(requireContext(), "Dự án đã đạt giới hạn tối đa 3 quản trị viên (Admin).", Toast.LENGTH_LONG).show();
                    rbAdmin.setChecked(false);
                    rbMember.setChecked(true);
                    rbOwner.setChecked(false);
                    currentRole = "member";
                    return;
                }
            }
        }

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
