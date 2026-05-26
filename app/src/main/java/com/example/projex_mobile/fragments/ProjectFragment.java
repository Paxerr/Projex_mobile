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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.project_fragment, container, false);

        if(getArguments() != null){

            int id = getArguments().getInt("project_id");
            String name = getArguments().getString("project_name");

        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout btnProTask = view.findViewById(R.id.btnpro_task);
        btnProTask.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, new ProTaskFragment())
                    .addToBackStack(null)
                    .commit();
        });

        LinearLayout btnTeam = view.findViewById(R.id.btnteam);
        btnTeam.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_container, new TeamFragment())
                    .addToBackStack(null)
                    .commit();
        });

        PieChart pieChart = view.findViewById(R.id.pieChart);

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f));
        entries.add(new PieEntry(30f));
        entries.add(new PieEntry(30f));


        PieDataSet dataSet = new PieDataSet(entries, "Status");
        dataSet.setColors(
                Color.parseColor("#0FADFF"),
                Color.parseColor("#EFEB3B"),
                Color.parseColor("#48FB98")

        );

        PieData data = new PieData(dataSet);
        data.setValueTextSize(0f);

        pieChart.getLegend().setEnabled(false);
        pieChart.setData(data);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();
        pieChart.animateY(1000);
    }
}