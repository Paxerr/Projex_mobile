package com.example.projex_mobile.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.ThemThanhVienDialogFragment;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TeamFragment extends Fragment {

    private static final String ARG_PROJECT_ID = "project_id";

    private int projectId = -1;
    private int adminCount = 0;
    private String currentUserRole = "Member";

    private ImageView btnBackHome;
    private ImageView btnAddMember;
    private TextView tvCount;
    private LinearLayout layoutMembersContainer;

    public TeamFragment() {
        super(R.layout.fragment_team);
    }

    public static TeamFragment newInstance(int projectId) {
        TeamFragment fragment = new TeamFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PROJECT_ID, projectId);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            projectId = getArguments().getInt(ARG_PROJECT_ID, -1);
        }

        initViews(view);
        handleEvents();

        getParentFragmentManager().setFragmentResultListener(
                "member_added",
                getViewLifecycleOwner(),
                (requestKey, result) -> loadProjectMembers()
        );

        loadProjectMembers();
    }

    private void initViews(View view) {
        btnBackHome = view.findViewById(R.id.btn_menu);
        btnAddMember = view.findViewById(R.id.btn_add_member);
        tvCount = view.findViewById(R.id.tv_count);
        layoutMembersContainer = view.findViewById(R.id.layout_members_container);
    }

    private void handleEvents() {
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(v -> goBackToProject());
        }

        if (btnAddMember != null) {
            btnAddMember.setOnClickListener(v -> openAddMemberScreen());
        }
    }

    private void goBackToProject() {
        try {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không quay lại được trang Project", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadProjectMembers() {
        if (projectId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy projectId", Toast.LENGTH_SHORT).show();
            return;
        }

        if (layoutMembersContainer == null) {
            Toast.makeText(requireContext(), "Thiếu layout_members_container trong fragment_team.xml", Toast.LENGTH_LONG).show();
            return;
        }

        String token = requireActivity()
                .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("token", "");

        ApiService apiService = RetrofitClient.getApiService(null);

        apiService.getProjectById(token, projectId)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject body = response.body();

                            if (body.has("members") && body.get("members").isJsonArray()) {
                                JsonArray members = body.getAsJsonArray("members");
                                renderMembers(members);
                            } else {
                                layoutMembersContainer.removeAllViews();
                                updateMemberCount(0);
                                Toast.makeText(requireContext(), "Project này chưa có thành viên", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Không load được thành viên", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void renderMembers(JsonArray members) {
        layoutMembersContainer.removeAllViews();
        updateMemberCount(members.size());

        if (members.size() == 0) {
            TextView emptyView = new TextView(requireContext());
            emptyView.setText("Chưa có thành viên trong dự án");
            emptyView.setTextColor(Color.parseColor("#9CA3AF"));
            emptyView.setTextSize(14);
            emptyView.setPadding(0, dp(8), 0, dp(8));
            layoutMembersContainer.addView(emptyView);
            return;
        }

        // Count admins and find current user's role
        int tempAdminCount = 0;
        String tempCurrentUserRole = "Member";
        String loggedInEmail = "";
        try {
            loggedInEmail = requireContext()
                    .getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    .getString("user_email", "");
        } catch (Exception ignored) {}

        for (JsonElement element : members) {
            if (!element.isJsonObject()) continue;
            JsonObject memberObj = element.getAsJsonObject();
            String role = getString(memberObj, "role", "Member");
            if ("Admin".equalsIgnoreCase(role)) {
                tempAdminCount++;
            }
            JsonObject userObj = memberObj.has("user") && memberObj.get("user").isJsonObject()
                    ? memberObj.getAsJsonObject("user") : null;
            if (userObj != null) {
                String email = getString(userObj, "email", "");
                if (email.equalsIgnoreCase(loggedInEmail)) {
                    tempCurrentUserRole = role;
                }
            }
        }
        this.adminCount = tempAdminCount;
        this.currentUserRole = tempCurrentUserRole;

        if (btnAddMember != null) {
            boolean canAdd = "Owner".equalsIgnoreCase(currentUserRole) || "Admin".equalsIgnoreCase(currentUserRole);
            btnAddMember.setVisibility(canAdd ? View.VISIBLE : View.GONE);
        }

        for (JsonElement element : members) {
            if (!element.isJsonObject()) continue;

            JsonObject memberObj = element.getAsJsonObject();

            int userId = memberObj.has("userId") && !memberObj.get("userId").isJsonNull()
                    ? memberObj.get("userId").getAsInt() : -1;
            String role = getString(memberObj, "role", "Member");
            String joinedAt = formatJoinedAt(getString(memberObj, "joinedAt", ""));

            JsonObject userObj = new JsonObject();
            if (memberObj.has("user")
                    && memberObj.get("user").isJsonObject()) {
                userObj = memberObj.getAsJsonObject("user");
            }

            String name = getString(userObj, "fullName", "Unknown");
            String email = getString(userObj, "email", "");
            String status = "● Đang hoạt động";

            View card = createMemberCard(userId, name, email, role, joinedAt, status);
            layoutMembersContainer.addView(card);
        }
    }

    private View createMemberCard(
            int memberUserId,
            String name,
            String email,
            String role,
            String joinedAt,
            String status
    ) {
        LinearLayout card = new LinearLayout(requireContext());
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackgroundResource(R.drawable.team_bg_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView avatar = new TextView(requireContext());
        avatar.setText(getAvatarText(name));
        avatar.setTextColor(Color.WHITE);
        avatar.setTextSize(14);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackgroundResource(R.drawable.home_bg_card);

        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(
                dp(44),
                dp(44)
        );
        avatarParams.setMargins(0, 0, dp(12), 0);
        card.addView(avatar, avatarParams);

        LinearLayout infoBox = new LinearLayout(requireContext());
        infoBox.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1
        );

        TextView tvName = new TextView(requireContext());
        tvName.setText(name);
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(15);
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvEmail = new TextView(requireContext());
        tvEmail.setText(email);
        tvEmail.setTextColor(Color.parseColor("#9CA3AF"));
        tvEmail.setTextSize(12);

        TextView tvRole = new TextView(requireContext());
        tvRole.setText(role);
        tvRole.setTextColor(Color.parseColor("#85ADFF"));
        tvRole.setTextSize(12);

        TextView tvJoinedAt = new TextView(requireContext());
        tvJoinedAt.setText(joinedAt.isEmpty() ? status : joinedAt);
        tvJoinedAt.setTextColor(Color.parseColor("#9CA3AF"));
        tvJoinedAt.setTextSize(11);

        infoBox.addView(tvName);
        infoBox.addView(tvEmail);
        infoBox.addView(tvRole);
        infoBox.addView(tvJoinedAt);

        card.addView(infoBox, infoParams);

        // Chỉ hiện nút 3 chấm chỉnh sửa quyền nếu người dùng hiện tại là Owner hoặc Admin
        boolean canManage = "Owner".equalsIgnoreCase(currentUserRole) || "Admin".equalsIgnoreCase(currentUserRole);
        if (canManage) {
            TextView btnMore = new TextView(requireContext());
            btnMore.setText("⋮");
            btnMore.setTextColor(Color.WHITE);
            btnMore.setTextSize(24);
            btnMore.setGravity(Gravity.CENTER);
            btnMore.setPadding(dp(8), dp(4), dp(8), dp(4));

            btnMore.setOnClickListener(v ->
                    openRoleFragment(
                            memberUserId,
                            name,
                            email,
                            role,
                            joinedAt,
                            status
                    )
            );

            card.addView(btnMore);
        }

        return card;
    }

    private void openAddMemberScreen() {
        if (projectId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy projectId", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            ThemThanhVienDialogFragment dialog = ThemThanhVienDialogFragment.newInstance(projectId, currentUserRole);
            dialog.show(getParentFragmentManager(), "ThemThanhVienDialog");
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không mở được popup thêm thành viên", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRoleFragment(int memberUserId,
                                  String memberName,
                                  String memberEmail,
                                  String memberRole,
                                  String joinDate,
                                  String status) {
        try {
            View parentView = (View) requireView().getParent();

            if (parentView == null || parentView.getId() == View.NO_ID) {
                Toast.makeText(requireContext(), "Không tìm thấy khung chứa Fragment", Toast.LENGTH_SHORT).show();
                return;
            }

            RoleFragment roleFragment = RoleFragment.newInstance(
                    projectId,
                    memberUserId,
                    memberName,
                    memberEmail,
                    memberRole,
                    joinDate,
                    status,
                    adminCount,
                    currentUserRole
            );

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(parentView.getId(), roleFragment)
                    .addToBackStack("RoleFragment")
                    .commit();

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không mở được trang chỉnh sửa quyền", Toast.LENGTH_SHORT).show();
        }
    }

    private String getString(JsonObject object, String key, String defaultValue) {
        if (object == null
                || !object.has(key)
                || object.get(key).isJsonNull()) {
            return defaultValue;
        }

        return object.get(key).getAsString();
    }

    private String formatJoinedAt(String joinedAt) {
        if (joinedAt == null || joinedAt.trim().isEmpty()) {
            return "";
        }

        int index = joinedAt.indexOf("T");
        if (index > 0) {
            return "Tham gia: " + joinedAt.substring(0, index);
        }

        return "Tham gia: " + joinedAt;
    }

    private void updateMemberCount(int count) {
        if (tvCount != null) {
            tvCount.setText(count + " người");
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String getAvatarText(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0].substring(0, 1).toUpperCase();
        }

        return (
                parts[0].substring(0, 1)
                        + parts[parts.length - 1].substring(0, 1)
        ).toUpperCase();
    }
}
