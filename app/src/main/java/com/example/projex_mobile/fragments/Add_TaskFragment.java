package com.example.projex_mobile.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projex_mobile.R;

public class Add_TaskFragment extends Fragment {

    public Add_TaskFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.add_task_fragment,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        TextView btnHuy = view.findViewById(R.id.btnHuy);

        if (btnHuy != null) {
            btnHuy.setOnClickListener(v -> {

                requireActivity()
                        .getSupportFragmentManager()
                        .popBackStack();

            });
        }
    }
}