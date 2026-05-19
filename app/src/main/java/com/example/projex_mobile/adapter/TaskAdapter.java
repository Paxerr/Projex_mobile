package com.example.projex_mobile.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> list;

    public TaskAdapter(List<Task> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Task task = list.get(position);

        // set dữ liệu
        holder.txtTitle.setText(task.getTitle());
        holder.txtStatus.setText(task.getStatus());

        // đổi màu status
        String status = task.getStatus();

        if (status == null) {
            holder.txtStatus.setTextColor(Color.GRAY);
            return;
        }

        switch (status) {

            case "Done":
                holder.txtStatus.setTextColor(Color.GREEN);
                break;

            case "InProgress":
                holder.txtStatus.setTextColor(Color.YELLOW);
                break;

            case "Pending":
                holder.txtStatus.setTextColor(Color.RED);
                break;

            default:
                holder.txtStatus.setTextColor(Color.GRAY);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }
}