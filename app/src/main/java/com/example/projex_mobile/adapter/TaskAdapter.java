package com.example.projex_mobile.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.Task;

import java.util.List;

public class TaskAdapter
        extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> list;

    public TaskAdapter(List<Task> list) {
        this.list = list;
    }

    public class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtStatus;
        TextView txtProjectName;

        public ViewHolder(View itemView) {
            super(itemView);

            txtTitle =
                    itemView.findViewById(R.id.txtTitle);

            txtStatus =
                    itemView.findViewById(R.id.txtStatus);

            txtProjectName =
                    itemView.findViewById(
                            R.id.txtProjectName
                    );
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(
                        R.layout.item_task,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            ViewHolder holder,
            int position
    ) {

        if (list == null
                || position >= list.size()) {
            return;
        }

        Task task = list.get(position);

        if (task == null) {
            return;
        }

        holder.txtTitle.setText(
                task.getTitle() != null
                        ? task.getTitle()
                        : "No Title"
        );

        holder.txtStatus.setText(
                task.getStatus() != null
                        ? task.getStatus()
                        : "Unknown"
        );

        if (task.getProject() != null
                && task.getProject().getName() != null) {

            holder.txtProjectName.setText(
                    task.getProject().getName()

            );
            holder.txtProjectName.setTextColor(Color.parseColor("#80FFFFFF"));



        } else {

            holder.txtProjectName.setText(
                    "No Project"
            );
        }

        // Giữ nguyên màu text
        holder.txtStatus.setTextColor(Color.WHITE);

        // Đổi màu ô chứa status
        holder.txtStatus.setBackgroundTintList(
                ColorStateList.valueOf(
                        getStatusBackgroundColor(
                                task.getStatus()
                        )
                )
        );
    }

    // Hàm đổi màu background status
    private int getStatusBackgroundColor(
            String status
    ) {

        if (status == null) {
            return Color.parseColor("#6B7280");
        }

        switch (status) {

            case "Done":
                return Color.parseColor("#0FADFF");

            case "Assigned":

            case "InProgress":
                return Color.parseColor("#EFEB3B");

            case "To do":
                return Color.parseColor("#48FB98");

            default:
                return Color.parseColor("#FF0000");
        }

    }

    @Override
    public int getItemCount() {

        return list == null ? 0 : list.size();
    }
}