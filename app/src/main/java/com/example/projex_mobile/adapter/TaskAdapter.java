package com.example.projex_mobile.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.object.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.ViewHolder> {

    private List<Task> list;

    public TaskAdapter(List<Task> list) {
        this.list = list;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtStatus = itemView.findViewById(R.id.txtStatus);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task task = list.get(position);

        holder.txtTitle.setText(task.title);
        holder.txtStatus.setText(task.status);

        // 🎨 đổi màu theo trạng thái
        switch (task.status) {
            case "Done":
                holder.txtStatus.setTextColor(Color.GREEN);
                break;
            case "InProgress":
                holder.txtStatus.setTextColor(Color.YELLOW);
                break;
            default:
                holder.txtStatus.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
