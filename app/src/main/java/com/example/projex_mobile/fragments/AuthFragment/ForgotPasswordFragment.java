package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;

public class ForgotPasswordFragment extends Fragment {

    public ForgotPasswordFragment() {
        super(R.layout.forgot_password_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnBack = view.findViewById(R.id.btnBack);
        Button btnNext = view.findViewById(R.id.btnNext);

        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        btnNext.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authOverlayContainer, new VerifyFragment())
                    .addToBackStack("verify_fragment")
                    .commit();
        });


    }
}
