package com.example.projex_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProjectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project);

        LinearLayout btnTask = findViewById(R.id.btnTasks);

        btnTask.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectActivity.this, TaskActivity.class);
            startActivity(intent);
        });

        TextView btnProTask;
        btnProTask = findViewById(R.id.btnpro_task);

        btnProTask.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectActivity.this, ProTaskActivity.class);
            startActivity(intent);
        });
    }

}