package com.example.projex_mobile;

import android.app.Dialog;
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

public class ThemThanhVienDialogFragment extends DialogFragment {

    private View modalCard;
    private EditText edtEmailOrName;
    private TextView tvRole;

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

        if (btnSend == null) missing.append(" btnSend");
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

        roleBox.setOnClickListener(v -> showRoleDropdown(v));

        View.OnClickListener submit = v -> inviteMember();
        btnAdd.setOnClickListener(submit);
        btnSend.setOnClickListener(submit);
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
        if (edtEmailOrName == null || tvRole == null) {
            Toast.makeText(requireContext(), "Thiếu thành phần nhập liệu", Toast.LENGTH_SHORT).show();
            return;
        }

        String email = edtEmailOrName.getText() == null
                ? ""
                : edtEmailOrName.getText().toString().trim();
        String role = tvRole.getText() == null
                ? "Member"
                : tvRole.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(
                requireContext(),
                "Mời " + email + " với role " + role + " thành công",
                Toast.LENGTH_SHORT
        ).show();

        dismiss();
    }
}
