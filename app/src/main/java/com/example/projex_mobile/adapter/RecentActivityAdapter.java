package com.example.projex_mobile.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.RecentItem;

import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    public interface OnRecentClickListener {
        void onClick(RecentItem item);
    }

    private final List<RecentItem> items;
    private final OnRecentClickListener listener;

    public RecentActivityAdapter(List<RecentItem> items, OnRecentClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_recent, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentItem item = items.get(position);

        holder.tvAvatar.setText(safeText(item.getAvatarText(), "NA"));
        holder.tvTitle.setText(safeText(item.getTitle(), ""));
        holder.tvMessage.setText(safeText(item.getMessage(), ""));
        holder.tvProjectName.setText(safeText(item.getProjectName(), ""));
        holder.tvTime.setText(safeText(item.getTimeAgo(), ""));
        holder.tvStatus.setText(getDisplayStatus(item.getStatus()));
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(getStatusColor(item.getStatus())));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String getDisplayStatus(String status) {
        if (status == null) return "Assigned";
        if ("Done".equalsIgnoreCase(status)) return "Done";
        if ("InProgress".equalsIgnoreCase(status) || "In Progress".equalsIgnoreCase(status)) return "In Progress";
        return "Assigned";
    }

    private int getStatusColor(String status) {
        if (status == null) return Color.parseColor("#3A3A3A");
        if ("Done".equalsIgnoreCase(status)) return Color.parseColor("#0FADFF");
        if ("InProgress".equalsIgnoreCase(status) || "In Progress".equalsIgnoreCase(status)) return Color.parseColor("#EFEB3B");
        return Color.parseColor("#48FB98");
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvTitle, tvMessage, tvProjectName, tvTime, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}