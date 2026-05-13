package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.projex_mobile.R;

public class RePasswordFragment extends Fragment {
    public RePasswordFragment() {
        super(R.layout.re_password_fragment);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnUpdatePassword = view.findViewById(R.id.btnUpdatePassword);
        Button btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack(null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

        btnUpdatePassword.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack(null,
                            FragmentManager.POP_BACK_STACK_INCLUSIVE);
        });

    }

}
