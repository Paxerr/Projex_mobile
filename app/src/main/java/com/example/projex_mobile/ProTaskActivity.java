package com.example.projex_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pro_task);


        TextView overview = findViewById(R.id.overview);

        overview.setOnClickListener(v -> {
            finish();
        });
        //trang thai
        LinearLayout btnStatus = findViewById(R.id.btnStatus) ;
        btnStatus.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, btnStatus);

            popup.getMenu().add("TO DO");
            popup.getMenu().add("In Progress");
            popup.getMenu().add("Done");
            TextView status=findViewById(R.id.textStatus);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "TO DO":
                        // xử lý
                        status.setText("TO DO");
                        break;
                    case "In Progress":
                        // xử lý
                        status.setText("TIn Progress");
                        break;
                    case "Done":
                        // xử lý
                        status.setText("Done");
                        break;
                }
                return true;
            });

            popup.show();
        });

        //ng thuc hien

        LinearLayout ngth = findViewById(R.id.ngth) ;
        ngth.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, ngth);

            popup.getMenu().add("A");
            popup.getMenu().add("B");
            popup.getMenu().add("C");
            TextView ngth_text=findViewById(R.id.ngth_text);
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getTitle().toString()) {
                    case "A":
                        // xử lý
                        ngth_text.setText("A");
                        break;
                    case "B":
                        // xử lý
                        ngth_text.setText("B");
                        break;
                    case "C":
                        // xử lý
                        ngth_text.setText("C");
                        break;
                }
                return true;
            });

            popup.show();
        });

    }
}