package com.example.projex_mobile.fragments.AuthFragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.example.projex_mobile.api.ApiService;
import com.example.projex_mobile.api.RetrofitClient;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyFragment extends Fragment {
    private static final String ARG_EMAIL = "email";

    public VerifyFragment(){
        super(R.layout.verify_fragment);
    }

    public static VerifyFragment newInstance(String email) {
        VerifyFragment fragment = new VerifyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EMAIL, email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String email = getArguments() == null ? "" : getArguments().getString(ARG_EMAIL, "");

        Button btnBack = view.findViewById(R.id.btnBack);
        Button btnVerify = view.findViewById(R.id.btnVerify);
        EditText otp1 = view.findViewById(R.id.otp1);
        EditText otp2 = view.findViewById(R.id.otp2);
        EditText otp3 = view.findViewById(R.id.otp3);
        EditText otp4 = view.findViewById(R.id.otp4);

        btnBack.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });

        btnVerify.setOnClickListener(v -> {
            String code = getText(otp1) + getText(otp2) + getText(otp3) + getText(otp4);

            if (email.isEmpty()) {
                Toast.makeText(requireContext(), "Thiếu email xác thực", Toast.LENGTH_SHORT).show();
                return;
            }

            if (code.length() != 4) {
                Toast.makeText(requireContext(), "Vui lòng nhập đủ 4 số", Toast.LENGTH_SHORT).show();
                return;
            }

            btnVerify.setEnabled(false);
            Map<String, String> body = new HashMap<>();
            body.put("email", email);
            body.put("code", code);

            ApiService apiService = RetrofitClient.getApiService(null);
            apiService.verifyResetCode(body).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    btnVerify.setEnabled(true);

                    if (!response.isSuccessful()) {
                        Toast.makeText(requireContext(), "Mã xác thực không đúng hoặc đã hết hạn", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    requireActivity()
                            .getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.authOverlayContainer, RePasswordFragment.newInstance(email, code))
                            .addToBackStack("re_password_fragment")
                            .commit();
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    btnVerify.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private String getText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
