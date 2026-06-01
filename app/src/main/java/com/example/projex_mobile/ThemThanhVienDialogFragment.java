package com.example.projex_mobile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ThemThanhVienDialogFragment extends DialogFragment {

    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_CURRENT_USER_ROLE = "current_user_role";

    private int projectId = -1;
    private String currentUserRole = "Member";
    private View modalCard;
    private EditText edtEmailOrName;
    private TextView tvRole;

    public static ThemThanhVienDialogFragment newInstance(int projectId, String currentUserRole) {
        ThemThanhVienDialogFragment fragment = new ThemThanhVienDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PROJECT_ID, projectId);
        bundle.putString(ARG_CURRENT_USER_ROLE, currentUserRole);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.add_member_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setCancelable(false);

        if (getArguments() != null) {
            projectId = getArguments().getInt(ARG_PROJECT_ID, -1);
            currentUserRole = getArguments().getString(ARG_CURRENT_USER_ROLE, "Member");
        }

        modalCard = view.findViewById(R.id.modalCard);
        edtEmailOrName = view.findViewById(R.id.edtEmailOrName);
        tvRole = view.findViewById(R.id.tvRole);


        TextView btnSend = view.findViewById(R.id.btnSend);
        TextView btnCancel = view.findViewById(R.id.btnCancel);
        TextView btnAdd = view.findViewById(R.id.btnAdd);
        View roleBox = view.findViewById(R.id.roleBox);

        StringBuilder missing = new StringBuilder();
        if (modalCard == null) missing.append(" modalCard");
        if (edtEmailOrName == null) missing.append(" edtEmailOrName");
        if (tvRole == null) missing.append(" tvRole");

        if (btnCancel == null) missing.append(" btnCancel");
        if (btnAdd == null) missing.append(" btnAdd");
        if (roleBox == null) missing.append(" roleBox");

        if (missing.length() > 0) {
            Toast.makeText(requireContext(), "Thiếu view:" + missing, Toast.LENGTH_LONG).show();
            return;
        }

        // Chặn click xuyên qua card.
        modalCard.setOnClickListener(v -> { });


        btnCancel.setOnClickListener(v -> dismiss());

        // Only Owner is allowed to choose the role. Admins are forced to invite as Member.
        boolean isOwner = "Owner".equalsIgnoreCase(currentUserRole);
        if (!isOwner) {
            if (tvRole != null) {
                tvRole.setText("Member");
            }
            if (roleBox != null) {
                roleBox.setEnabled(false);
                roleBox.setClickable(false);
            }
        } else {
            if (roleBox != null) {
                roleBox.setEnabled(true);
                roleBox.setClickable(true);
                roleBox.setOnClickListener(v -> showRoleDropdown(v));
            }
        }

        View.OnClickListener submit = v -> inviteMember();
        btnAdd.setOnClickListener(submit);
        if (btnSend != null) {
            btnSend.setOnClickListener(submit);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog == null || dialog.getWindow() == null) {
            return;
        }

        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
        );
    }

    private void showRoleDropdown(View anchor) {
        PopupMenu pm = new PopupMenu(requireContext(), anchor);
        pm.getMenuInflater().inflate(R.menu.menu_role, pm.getMenu());
        pm.setOnMenuItemClickListener(item -> {
            tvRole.setText(item.getTitle());
            return true;
        });
        pm.show();
    }

    private void inviteMember() {
        if (projectId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy projectId", Toast.LENGTH_SHORT).show();
            return;
        }

        if (edtEmailOrName == null || tvRole == null) {
            Toast.makeText(requireContext(), "Thiếu thành phần nhập liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = edtEmailOrName.getText() == null
                ? ""
                : edtEmailOrName.getText().toString().trim();
        String role = tvRole.getText() == null
                ? "Member"
                : normalizeRole(tvRole.getText().toString().trim());

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Email không hợp lệ", Toast.LENGTH_SHORT).show();
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

        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("role", role);

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.addProjectMemberByEmail(authToken, projectId, body)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    requireContext(),
                                    "Thêm người thành công",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Bundle result = new Bundle();
                            result.putBoolean("success", true);
                            getParentFragmentManager().setFragmentResult("member_added", result);

                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), getErrorMessage(response), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String normalizeRole(String role) {
        if (role == null) {
            return "Member";
        }

        String value = role.trim().toLowerCase();

        if (value.contains("owner") || value.contains("chủ")) {
            return "Owner";
        }

        if (value.contains("admin") || value.contains("quản")) {
            return "Admin";
        }

        return "Member";
    }

    private String getErrorMessage(Response<JsonObject> response) {
        try {
            if (response.errorBody() == null) {
                return "Thêm thành viên thất bại";
            }

            String raw = response.errorBody().string();
            JSONObject json = new JSONObject(raw);

            if (json.has("message")) {
                return json.getString("message");
            }

            if (json.has("title")) {
                return json.getString("title");
            }

            return raw;
        } catch (Exception e) {
            return "Thêm thành viên thất bại. Mã lỗi: " + response.code();
        }
    }
}
