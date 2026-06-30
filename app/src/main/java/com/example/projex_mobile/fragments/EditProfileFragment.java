package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class EditProfileFragment extends Fragment {

    public static final String REQUEST_KEY_EDIT_PROFILE = "edit_profile_result";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_AVATAR_URL = "avatar_url";

    private TextView tvAvatar;
    private TextInputEditText etName, etEmail, etPhone;
    private String currentAvatarUrl = null;

    private static final ExecutorService executor = Executors.newCachedThreadPool();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private androidx.activity.result.ActivityResultLauncher<String> galleryLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        galleryLauncher = registerForActivityResult(
                new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadAvatar(uri);
                    }
                }
        );
    }

    public EditProfileFragment() {
        super(R.layout.fragment_edit_profile);
    }

    public static EditProfileFragment newInstance(String name, String email, String phone, String avatarUrl) {
        EditProfileFragment fragment = new EditProfileFragment();

        Bundle args = new Bundle();
        args.putString(KEY_NAME, name);
        args.putString(KEY_EMAIL, email);
        args.putString(KEY_PHONE, phone);
        args.putString(KEY_AVATAR_URL, avatarUrl);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        receiveDataFromAccount();
        handleEvents(view);
        handleTextBoxEvents();
    }

    private void initViews(View view) {
        tvAvatar = view.findViewById(R.id.tvAvatar);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
    }

    private void receiveDataFromAccount() {
        Bundle args = getArguments();

        if (args == null) {
            return;
        }

        String name = args.getString(KEY_NAME);
        String email = args.getString(KEY_EMAIL);
        String phone = args.getString(KEY_PHONE);
        currentAvatarUrl = args.getString(KEY_AVATAR_URL);

        if (name != null) {
            etName.setText(name);
            tvAvatar.setText(makeAvatarText(name));
        }

        if (email != null) {
            etEmail.setText(email);
        }

        if (phone != null) {
            etPhone.setText(phone);
        }

        setupAvatarImage(currentAvatarUrl);
    }

    private void handleEvents(View view) {
        View btnBack = view.findViewById(R.id.btnBack);
        View btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        View btnSave = view.findViewById(R.id.btnSave);
        View btnCancel = view.findViewById(R.id.btnCancel);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> closeFragment());
        }

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> closeFragment());
        }

        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v ->
                    galleryLauncher.launch("image/*")
            );
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveProfile());
        }
    }

    private void handleTextBoxEvents() {
        etName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String name = getText(etName);

                if (name.isEmpty()) {
                    etName.setError("Vui lòng nhập họ và tên");
                }
            }
        });

        etEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String email = getText(etEmail);

                if (!email.isEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    etEmail.setError("Email không hợp lệ");
                }
            }
        });

        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String phone = getText(etPhone);

                if (phone.isEmpty()) {
                    etPhone.setError("Vui lòng nhập số điện thoại");
                }
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvAvatar.setText(makeAvatarText(s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void saveProfile() {
        String name = getText(etName);
        String email = getText(etEmail);
        String phone = getText(etPhone);

        if (name.isEmpty()) {
            etName.setError("Vui lòng nhập họ và tên");
            etName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Vui lòng nhập email");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Vui lòng nhập số điện thoại");
            etPhone.requestFocus();
            return;
        }

        Bundle result = new Bundle();
        result.putString(KEY_NAME, name);
        result.putString(KEY_EMAIL, email);
        result.putString(KEY_PHONE, phone);
        result.putString(KEY_AVATAR_URL, currentAvatarUrl);

        getParentFragmentManager().setFragmentResult(REQUEST_KEY_EDIT_PROFILE, result);

        Toast.makeText(requireContext(), "Đã cập nhật hồ sơ", Toast.LENGTH_SHORT).show();

        closeFragment();
    }

    private void closeFragment() {
        requireActivity()
                .getSupportFragmentManager()
                .popBackStack();
    }

    private String getText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String makeAvatarText(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "U";
        }

        String[] words = name.trim().split("\\s+");

        if (words.length == 1) {
            return words[0].substring(0, Math.min(2, words[0].length()))
                    .toUpperCase(new Locale("vi", "VN"));
        }

        String firstChar = words[0].substring(0, 1);
        String lastChar = words[words.length - 1].substring(0, 1);

        return (firstChar + lastChar).toUpperCase(new Locale("vi", "VN"));
    }

    public static void loadImage(String urlString, ImageView imageView) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return;
        }

        if ((urlString.contains("localhost") || urlString.contains("127.0.0.1"))
                && urlString.contains("/uploads/")) {
            String path = urlString.substring(urlString.indexOf("/uploads/"));
            String baseUrl = com.example.projex_mobile.api.RetrofitClient.BASE_URL;
            urlString = baseUrl.substring(0, baseUrl.length() - 1) + path;
        } else if (urlString.contains("localhost")) {
            urlString = urlString.replace("localhost", "10.0.2.2");
        } else if (urlString.contains("127.0.0.1")) {
            urlString = urlString.replace("127.0.0.1", "10.0.2.2");
        }

        final String finalUrl = urlString;
        executor.execute(() -> {
            try {
                URL url = new URL(finalUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);

                if (bitmap != null) {
                    mainHandler.post(() -> imageView.setImageBitmap(bitmap));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void setupAvatarImage(String avatarUrl) {
        View view = getView();
        if (view == null) return;

        FrameLayout cardAvatar = view.findViewById(R.id.cardAvatar);
        if (cardAvatar == null) return;

        com.google.android.material.imageview.ShapeableImageView ivAvatar = view.findViewById(R.id.ivAvatar);
        if (ivAvatar == null) {
            ivAvatar = new com.google.android.material.imageview.ShapeableImageView(requireContext());
            ivAvatar.setId(R.id.ivAvatar);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            );
            ivAvatar.setLayoutParams(params);
            ivAvatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
            ivAvatar.setVisibility(View.GONE);

            float density = getResources().getDisplayMetrics().density;
            int cornerRadiusPx = (int) (24 * density);
            ivAvatar.setShapeAppearanceModel(
                    ivAvatar.getShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(cornerRadiusPx)
                            .build()
            );

            cardAvatar.addView(ivAvatar);
        }

        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            ivAvatar.setVisibility(View.VISIBLE);
            if (tvAvatar != null) {
                tvAvatar.setVisibility(View.GONE);
            }
            loadImage(avatarUrl, ivAvatar);
        } else {
            ivAvatar.setVisibility(View.GONE);
            if (tvAvatar != null) {
                tvAvatar.setVisibility(View.VISIBLE);
            }
        }
    }

    private String getAuthToken() {
        if (getContext() == null) return null;
        return getContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                .getString("token", null);
    }

    private void uploadAvatar(android.net.Uri uri) {
        String token = getAuthToken();
        if (token == null) {
            Toast.makeText(requireContext(), "Phiên đăng nhập hết hạn", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(requireContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show();

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Toast.makeText(requireContext(), "Không thể mở tệp ảnh", Toast.LENGTH_SHORT).show();
                return;
            }

            byte[] bytes = getBytes(inputStream);
            inputStream.close();

            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse(requireContext().getContentResolver().getType(uri)),
                    bytes
            );

            okhttp3.MultipartBody.Part filePart = okhttp3.MultipartBody.Part.createFormData(
                    "File",
                    "avatar.jpg",
                    requestFile
            );

            com.example.projex_mobile.api.ApiService apiService = com.example.projex_mobile.api.RetrofitClient.getApiService(token);
            apiService.uploadAvatar(token, filePart).enqueue(new retrofit2.Callback<com.google.gson.JsonObject>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull retrofit2.Response<com.google.gson.JsonObject> response) {
                    if (!isAdded()) return;

                    if (response.isSuccessful() && response.body() != null) {
                        com.google.gson.JsonObject body = response.body();
                        if (body.has("avatarUrl")) {
                            currentAvatarUrl = body.get("avatarUrl").getAsString();
                            setupAvatarImage(currentAvatarUrl);
                            Toast.makeText(requireContext(), "Cập nhật ảnh đại diện thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Không tìm thấy URL ảnh mới", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Không thể tải lên ảnh", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<com.google.gson.JsonObject> call, @NonNull Throwable t) {
                    if (!isAdded()) return;
                    Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Lỗi xử lý ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
