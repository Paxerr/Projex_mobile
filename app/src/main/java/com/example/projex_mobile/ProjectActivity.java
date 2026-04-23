package com.example.projex_mobile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

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


        //bieu do

        PieChart pieChart = findViewById(R.id.pieChart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Done"));
        entries.add(new PieEntry(30f, "In Progress"));
        entries.add(new PieEntry(15f, "To Do"));
        entries.add(new PieEntry(15f, "Test"));

        PieDataSet dataSet = new PieDataSet(entries, "Status");

        dataSet.setColors(
                Color.parseColor("#0000FF"),
                Color.parseColor("#00FF00"),
                Color.parseColor("#FF00FF"),
                Color.parseColor("#FF0000")
        );

        PieData data = new PieData(dataSet);
        data.setValueTextSize(14f);

        pieChart.setUsePercentValues(true);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);

        pieChart.invalidate();
        pieChart.animateY(1000);
    }

}