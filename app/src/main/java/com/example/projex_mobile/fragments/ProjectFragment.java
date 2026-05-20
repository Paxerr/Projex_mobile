package com.example.projex_mobile.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;

public class ProjectFragment extends Fragment {

    public ProjectFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.project_fragment,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        TextView btnProTask = view.findViewById(R.id.btnpro_task);

        btnProTask.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.space_fragment_container,
                            new ProTaskFragment())
                    .addToBackStack(null)
                    .commit();
        });



        // Biểu đồ

        PieChart pieChart = view.findViewById(R.id.pieChart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Done"));
        entries.add(new PieEntry(30f, "In Progress"));
        entries.add(new PieEntry(15f, "To Do"));
        entries.add(new PieEntry(15f, "Test"));

        PieDataSet dataSet = new PieDataSet(entries, "Status");

        dataSet.setColors(
                Color.parseColor("#880000FF"),
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