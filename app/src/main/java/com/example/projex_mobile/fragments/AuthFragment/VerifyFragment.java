package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;

public class VerifyFragment extends Fragment {
    public VerifyFragment(){
        super(R.layout.verify_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Button btnBack = view.findViewById(R.id.btnBack);
        Button btnVerify = view.findViewById(R.id.btnVerify);

        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
        btnVerify.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authOverlayContainer, new RePasswordFragment())
                    .addToBackStack("re_password_fragment")
                    .commit();
        });
    }

}
