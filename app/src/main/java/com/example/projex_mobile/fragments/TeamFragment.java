package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.ThemThanhVienDialogFragment;

public class TeamFragment extends Fragment {

    private ImageView btnAddMember;
    private ImageView btnEditRoleMember1;
    private ImageView btnEditRoleMember2;

    public TeamFragment() {
        super(R.layout.fragment_team);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        handleEvents();
    }

    private void initViews(View view) {
        btnAddMember = view.findViewById(R.id.btn_add_member);
        btnEditRoleMember1 = view.findViewById(R.id.btn_edit_role_member1);
        btnEditRoleMember2 = view.findViewById(R.id.btn_edit_role_member2);
    }

    private void handleEvents() {
        if (btnAddMember != null) {
            btnAddMember.setOnClickListener(v -> openAddMemberScreen());
        }

        if (btnEditRoleMember1 != null) {
            btnEditRoleMember1.setOnClickListener(v ->
                    openRoleFragment(
                            "Đăng Nguyễn",
                            "dang.nguyen@company.com",
                            "member",
                            "12/05/2023",
                            "● Đang hoạt động"
                    )
            );
        }

        if (btnEditRoleMember2 != null) {
            btnEditRoleMember2.setOnClickListener(v ->
                    openRoleFragment(
                            "sdf fdsf",
                            "sdffdsf@gmail.com",
                            "admin",
                            "20/08/2023",
                            "● Đang hoạt động"
                    )
            );
        }
    }

    private void openAddMemberScreen() {
        try {
            ThemThanhVienDialogFragment dialog = new ThemThanhVienDialogFragment();
            dialog.show(getParentFragmentManager(), "ThemThanhVienDialog");
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Không mở được popup thêm thành viên", Toast.LENGTH_SHORT).show();
        }
    }

    private void openRoleFragment(String memberName,
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
                    memberName,
                    memberEmail,
                    memberRole,
                    joinDate,
                    status
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
}