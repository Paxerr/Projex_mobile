package com.example.projex_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task);

        ImageView btnCreat;
        btnCreat = findViewById(R.id.btnCreate);
        btnCreat.setOnClickListener(v -> {
            Intent intent = new Intent(TaskActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });
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
    }
}