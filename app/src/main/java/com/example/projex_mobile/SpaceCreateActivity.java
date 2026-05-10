package com.example.projex_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SpaceCreateActivity extends AppCompatActivity {

    private TextView btnHuy, btnTao;
    private EditText edtTenKhongGian, edtTenNguoiQuanLy, edtMoTaKhongGian;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.space_create_activity);

        btnHuy = findViewById(R.id.btnHuy);
        btnTao = findViewById(R.id.btnTao);
        edtTenKhongGian = findViewById(R.id.edtTenKhongGian);
        edtTenNguoiQuanLy = findViewById(R.id.edtTenNguoiQuanLy);
        edtMoTaKhongGian = findViewById(R.id.edtMoTaKhongGian);

        btnHuy.setOnClickListener(v -> finish());

        btnTao.setOnClickListener(v -> {
            String tenKhongGian = edtTenKhongGian.getText() != null ? edtTenKhongGian.getText().toString().trim() : "";
            String tenNguoiQuanLy = edtTenNguoiQuanLy.getText() != null ? edtTenNguoiQuanLy.getText().toString().trim() : "";
            String moTa = edtMoTaKhongGian.getText() != null ? edtMoTaKhongGian.getText().toString().trim() : "";

            Intent result = new Intent();
            result.putExtra("space_name", tenKhongGian);
            result.putExtra("manager_name", tenNguoiQuanLy);
            result.putExtra("space_desc", moTa);

            setResult(Activity.RESULT_OK, result);
            finish();
        });
    }
}