package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.adapter.TaskAdapter;
import com.example.projex_mobile.objects.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskFragment extends Fragment {

    public TaskFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.task_fragment,
                container,
                false
        );



        LinearLayout btnStatus =
                view.findViewById(R.id.btnStatus);

        btnStatus.setOnClickListener(v -> {

            PopupMenu popup =
                    new PopupMenu(requireContext(),
                            btnStatus);

            popup.getMenu().add("TO DO");
            popup.getMenu().add("In Progress");
            popup.getMenu().add("Done");

            TextView status =
                    view.findViewById(R.id.textStatus);

            popup.setOnMenuItemClickListener(item -> {

                switch (item.getTitle().toString()) {

                    case "TO DO":

                        status.setText("TO DO");
                        break;

                    case "In Progress":

                        status.setText("In Progress");
                        break;

                    case "Done":

                        status.setText("Done");
                        break;
                }

                return true;
            });

            popup.show();
        });

        RecyclerView rvTask =
                view.findViewById(R.id.rvTask);

        List<Task> list = new ArrayList<>();

        Task t1 = new Task();
        t1.setTitle("Thiết kế UI");
        t1.setStatus("Todo");

        Task t2 = new Task();
        t2.setTitle("Build API");
        t2.setStatus("InProgress");

        Task t3 = new Task();
        t3.setTitle("Test");
        t3.setStatus("Done");

        list.add(t1);
        list.add(t2);
        list.add(t3);

        TaskAdapter adapter =
                new TaskAdapter(list);

        rvTask.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        rvTask.setAdapter(adapter);

        return view;
    }
}