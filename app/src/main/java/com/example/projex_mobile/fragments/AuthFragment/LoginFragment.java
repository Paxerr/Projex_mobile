package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;

public class LoginFragment extends Fragment {

    public LoginFragment() {
        super(R.layout.login_fragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvForgot = view.findViewById(R.id.tvForgot);

        tvForgot.setOnClickListener(v -> {
            View authContent = requireActivity().findViewById(R.id.authContent);
            authContent.setVisibility(View.GONE);

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.authOverlayContainer, new ForgotPasswordFragment())
                    .addToBackStack("forgot_password")
                    .commit();
        });
    }
}
