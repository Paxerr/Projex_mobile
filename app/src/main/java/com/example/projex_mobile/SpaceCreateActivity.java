package com.example.projex_mobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SpaceCreateActivity extends AppCompatActivity {

    private TextView btnHuy, btnTao;
    private EditText edtTenKhongGian, edtMoTaKhongGian, edtStartDate, edtEndDate;

    private String startDateIso = null;
    private String endDateIso = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.space_create_activity);

        btnHuy = findViewById(R.id.btnHuy);
        btnTao = findViewById(R.id.btnTao);
        edtTenKhongGian = findViewById(R.id.edtTenKhongGian);
        edtMoTaKhongGian = findViewById(R.id.edtMoTaKhongGian);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        edtStartDate.setFocusable(false);
        edtEndDate.setFocusable(false);

        edtStartDate.setOnClickListener(v -> openDatePicker(true));
        edtEndDate.setOnClickListener(v -> openDatePicker(false));

        btnHuy.setOnClickListener(v -> finish());

        btnTao.setOnClickListener(v -> {
            String tenKhongGian = edtTenKhongGian.getText() != null ? edtTenKhongGian.getText().toString().trim() : "";
            String moTa = edtMoTaKhongGian.getText() != null ? edtMoTaKhongGian.getText().toString().trim() : "";

            if (tenKhongGian.isEmpty()) {
                edtTenKhongGian.setError("Vui lòng nhập tên không gian");
                return;
            }

            Intent result = new Intent();
            result.putExtra("space_name", tenKhongGian);
            result.putExtra("space_desc", moTa);
            result.putExtra("start_date", startDateIso);
            result.putExtra("end_date", endDateIso);

            setResult(Activity.RESULT_OK, result);
            finish();
        });
    }

    private void openDatePicker(boolean isStart) {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStart ? "Chọn ngày bắt đầu" : "Chọn ngày kết thúc")
                .build();

        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formatted = sdf.format(new Date(selection));

            if (isStart) {
                startDateIso = formatted + "T00:00:00Z";
                edtStartDate.setText(formatted);
            } else {
                endDateIso = formatted + "T23:59:59Z";
                edtEndDate.setText(formatted);
            }
        });

        picker.show(getSupportFragmentManager(), "date_picker");
    }

}