package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;

public class ProTaskFragment extends Fragment {

    public ProTaskFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.pro_task_fragment,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();

        });
        FrameLayout btnAdd = view.findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(v -> {

            requireActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.frame_container,
                            new Add_TaskFragment())
                    .addToBackStack(null)
                    .commit();
        });


        // Trạng thái
        LinearLayout btnStatus = view.findViewById(R.id.btnStatus);

        btnStatus.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(requireContext(), btnStatus);

            popup.getMenu().add("TO DO");
            popup.getMenu().add("In Progress");
            popup.getMenu().add("Done");

            TextView status = view.findViewById(R.id.textStatus);

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

        // Người thực hiện
        LinearLayout ngth = view.findViewById(R.id.ngth);

        ngth.setOnClickListener(v -> {

            PopupMenu popup = new PopupMenu(requireContext(), ngth);

            popup.getMenu().add("A");
            popup.getMenu().add("B");
            popup.getMenu().add("C");

            TextView ngthText = view.findViewById(R.id.ngth_text);

            popup.setOnMenuItemClickListener(item -> {

                switch (item.getTitle().toString()) {

                    case "A":
                        ngthText.setText("A");
                        break;

                    case "B":
                        ngthText.setText("B");
                        break;

                    case "C":
                        ngthText.setText("C");
                        break;
                }

                return true;
            });

            popup.show();
        });
    }
}