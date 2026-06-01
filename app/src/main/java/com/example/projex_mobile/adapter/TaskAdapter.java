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
    private String defaultProjectName;
    private OnTaskClickListener listener;

    public TaskAdapter(List<Task> list) {
        this.list = list;
    }

    public TaskAdapter(List<Task> list, OnTaskClickListener listener) {
        this.list = list;
        this.listener = listener;
    }
    public TaskAdapter(
            List<Task> list,
            OnTaskClickListener listener,
            String defaultProjectName) {

        this.list = list;
        this.listener = listener;
        this.defaultProjectName = defaultProjectName;
    }

    public class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtStatus;
        TextView txtProjectName;

        public ViewHolder(View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtProjectName = itemView.findViewById(R.id.txtProjectName);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(
            ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            ViewHolder holder,
            int position
    ) {
        if (list == null || position >= list.size()) {
            return;
        }

        Task task = list.get(position);

        if (task == null) {
            return;
        }

        holder.txtTitle.setText(
                task.getTitle() != null ? task.getTitle() : "No Title"
        );

        holder.txtStatus.setText(
                task.getStatus() != null ? task.getStatus() : "Unknown"
        );

        if (task.getProject() != null
                && task.getProject().getName() != null) {

            holder.txtProjectName.setText(
                    task.getProject().getName()
            );

        } else if (defaultProjectName != null
                && !defaultProjectName.isEmpty()) {

            holder.txtProjectName.setText(
                    defaultProjectName
            );

        } else {

            holder.txtProjectName.setText(
                    "No Project"
            );
        }

        holder.txtStatus.setTextColor(Color.WHITE);

        holder.txtStatus.setBackgroundTintList(
                ColorStateList.valueOf(
                        getStatusBackgroundColor(task.getStatus())
                )
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });
    }

    private int getStatusBackgroundColor(String status) {
        if (status == null) {
            return Color.parseColor("#6B7280");
        }

        switch (status) {
            case "Done":
                return Color.parseColor("#800FADFF");

            case "Assigned":
                return Color.parseColor("#8048FB98");

            case "InProgress":
                return Color.parseColor("#80EFEB3B");

            case "To do":
            default:
                return Color.parseColor("#FF0000");
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }
}