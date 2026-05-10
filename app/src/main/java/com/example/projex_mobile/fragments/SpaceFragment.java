package com.example.projex_mobile.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.SpaceCreateActivity;

public class SpaceFragment extends Fragment {

    private ActivityResultLauncher<Intent> createSpaceLauncher;

    public SpaceFragment() {
    }

    public static SpaceFragment newInstance() {
        return new SpaceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createSpaceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        String spaceName = result.getData().getStringExtra("space_name");
                        String managerName = result.getData().getStringExtra("manager_name");
                        String spaceDesc = result.getData().getStringExtra("space_desc");

                        reloadSpaces();
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.space_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FrameLayout btnAddSpace = view.findViewById(R.id.btnAddSpace);
        if (btnAddSpace != null) {
            btnAddSpace.setOnClickListener(v -> openCreateSpace());
        }
    }

    private void openCreateSpace() {
        Intent intent = new Intent(requireContext(), SpaceCreateActivity.class);
        createSpaceLauncher.launch(intent);
    }

    private void reloadSpaces() {
        // Sau này: gọi lại API hoặc refresh adapter
    }
}