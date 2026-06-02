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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // Cấu hình ô nhập liệu động để hỗ trợ nhập nhiều email (hàng dọc/ngang) trên mọi kích thước màn hình
        if (edtEmailOrName != null) {
            ViewGroup.LayoutParams lp = edtEmailOrName.getLayoutParams();
            if (lp != null) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                edtEmailOrName.setLayoutParams(lp);
            }
            edtEmailOrName.setMinimumHeight((int) (44 * requireContext().getResources().getDisplayMetrics().density));
            edtEmailOrName.setHint("Ví dụ: email1@company.com, email2@company.com");
            edtEmailOrName.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            edtEmailOrName.setSingleLine(false);
            edtEmailOrName.setMaxLines(3);
        }

        TextView tvLabelEmail = view.findViewById(R.id.tvLabelEmail);
        if (tvLabelEmail != null) {
            tvLabelEmail.setText("DANH SÁCH EMAIL (CÁCH NHAU BẰNG DẤU PHẨY/KHOẢNG TRẮNG)");
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

        String input = edtEmailOrName.getText() == null
                ? ""
                : edtEmailOrName.getText().toString().trim();
        String role = tvRole.getText() == null
                ? "Member"
                : normalizeRole(tvRole.getText().toString().trim());

        if (input.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập ít nhất một email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tách các email dựa trên dấu phẩy, dấu chấm phẩy, khoảng trắng hoặc xuống dòng
        String[] rawEmails = input.split("[,;\\s]+");
        List<String> emails = new ArrayList<>();
        List<String> invalidEmails = new ArrayList<>();

        for (String raw : rawEmails) {
            String trimmed = raw.trim();
            if (trimmed.isEmpty()) continue;
            if (Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()) {
                if (!emails.contains(trimmed)) {
                    emails.add(trimmed);
                }
            } else {
                if (!invalidEmails.contains(trimmed)) {
                    invalidEmails.add(trimmed);
                }
            }
        }

        if (!invalidEmails.isEmpty()) {
            Toast.makeText(
                    requireContext(),
                    "Email không hợp lệ: " + android.text.TextUtils.join(", ", invalidEmails),
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (emails.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập ít nhất một email hợp lệ", Toast.LENGTH_SHORT).show();
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

        // Gọi phương thức đệ quy để gửi yêu cầu và tự động lọc nếu có lỗi
        sendInviteRequest(authToken, emails, role, new ArrayList<>());
    }

    private void sendInviteRequest(
            final String authToken,
            final List<String> emailsToSubmit,
            final String role,
            final List<String> accumulatedBadEmails
    ) {
        // Tạo cấu trúc dữ liệu gửi lên API: {"members": [{"email": "...", "role": "..."}, ...]}
        Map<String, Object> body = new HashMap<>();
        List<Map<String, String>> membersList = new ArrayList<>();
        for (String email : emailsToSubmit) {
            Map<String, String> memberItem = new HashMap<>();
            memberItem.put("email", email);
            memberItem.put("role", role);
            membersList.add(memberItem);
        }
        body.put("members", membersList);

        ApiService apiService = RetrofitClient.getApiService(null);
        apiService.addProjectMemberByEmail(authToken, projectId, body)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (!isAdded()) return;

                        if (response.isSuccessful()) {
                            // Thành công hoàn toàn hoặc thành công sau khi đã tự động lọc
                            StringBuilder msg = new StringBuilder("Đã thêm thành công " + emailsToSubmit.size() + " thành viên.");
                            if (!accumulatedBadEmails.isEmpty()) {
                                msg.append("\nBỏ qua các email lỗi (không tồn tại/đã là thành viên): ")
                                   .append(android.text.TextUtils.join(", ", accumulatedBadEmails));
                            }

                            Toast.makeText(
                                    requireContext(),
                                    msg.toString(),
                                    Toast.LENGTH_LONG
                            ).show();

                            Bundle result = new Bundle();
                            result.putBoolean("success", true);
                            getParentFragmentManager().setFragmentResult("member_added", result);

                            dismiss();
                        } else {
                            // Thất bại! Đọc chuỗi JSON lỗi một lần duy nhất để tránh stream closed
                            String errorRaw = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorRaw = response.errorBody().string();
                                }
                            } catch (Exception ignored) {}

                            // Trích xuất danh sách email gây lỗi từ phản hồi của Server
                            List<String> badEmailsFromResponse = extractBadEmailsFromRaw(errorRaw);
                            if (!badEmailsFromResponse.isEmpty()) {
                                List<String> remainingEmails = new ArrayList<>();
                                for (String e : emailsToSubmit) {
                                    if (!badEmailsFromResponse.contains(e.toLowerCase())) {
                                        remainingEmails.add(e);
                                    }
                                }

                                // Gom các email lỗi mới phát hiện vào danh sách tích lũy
                                List<String> newBadEmails = new ArrayList<>(accumulatedBadEmails);
                                for (String badEmail : badEmailsFromResponse) {
                                    if (!newBadEmails.contains(badEmail)) {
                                        newBadEmails.add(badEmail);
                                    }
                                }

                                // Nếu vẫn còn lại email hợp lệ khác, tự động thực hiện lại request gửi đi
                                if (!remainingEmails.isEmpty()) {
                                    sendInviteRequest(authToken, remainingEmails, role, newBadEmails);
                                    return;
                                }
                            }

                            // Nếu không còn email nào hợp lệ để thử lại, hiển thị thông báo lỗi tiếng Việt thân thiện
                            Toast.makeText(
                                    requireContext(),
                                    getErrorMessageFromRaw(errorRaw, response.code()),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        if (!isAdded()) return;
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private List<String> extractBadEmailsFromRaw(String raw) {
        List<String> badEmails = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) {
            return badEmails;
        }
        try {
            JSONObject json = new JSONObject(raw);
            if (json.has("emails")) {
                org.json.JSONArray arr = json.getJSONArray("emails");
                for (int i = 0; i < arr.length(); i++) {
                    String email = arr.getString(i);
                    if (email != null) {
                        badEmails.add(email.trim().toLowerCase());
                    }
                }
            }
        } catch (Exception ignored) {}
        return badEmails;
    }

    private String getErrorMessageFromRaw(String raw, int statusCode) {
        try {
            if (raw == null || raw.trim().isEmpty()) {
                return "Thêm thành viên thất bại. Mã lỗi: " + statusCode;
            }

            JSONObject json = new JSONObject(raw);

            if (json.has("message")) {
                return translateErrorMessage(json.getString("message"));
            }

            if (json.has("title")) {
                return json.getString("title");
            }

            return raw;
        } catch (Exception e) {
            return "Thêm thành viên thất bại. Mã lỗi: " + statusCode;
        }
    }

    private String translateErrorMessage(String original) {
        if (original == null) return "Thêm thành viên thất bại";
        if (original.contains("Some users were not found")) {
            return "Một số email không tồn tại trong hệ thống.";
        }
        if (original.contains("Some users are already members")) {
            return "Một số email đã là thành viên của dự án.";
        }
        return original;
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
