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

    private final List<RecentItem> items;

    public RecentActivityAdapter(List<RecentItem> items) {
        this.items = items;
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

        holder.tvTitle.setText(item.getTitle());
        holder.tvMessage.setText(item.getMessage());
        holder.tvTicket.setText(item.getTicketCode());
        holder.tvTime.setText(item.getTimeAgo());

        String initials = item.getAvatarText();
        if (initials == null || initials.trim().isEmpty()) {
            initials = item.getTitle() != null && !item.getTitle().trim().isEmpty()
                    ? String.valueOf(item.getTitle().trim().charAt(0)).toUpperCase()
                    : "NA";
        }
        holder.tvAvatar.setText(initials);

        String status = item.getStatus();
        holder.tvStatus.setText(getDisplayStatus(status));
        holder.tvStatus.setBackgroundTintList(ColorStateList.valueOf(getStatusColor(status)));
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private String getDisplayStatus(String status) {
        if (status == null) return "Unknown";
        if ("InProgress".equalsIgnoreCase(status)) return "In Progress";
        if ("ToDo".equalsIgnoreCase(status)) return "To Do";
        return status;
    }

    private int getStatusColor(String status) {
        if ("InProgress".equalsIgnoreCase(status)) return Color.parseColor("#F4B740");
        if ("Done".equalsIgnoreCase(status)) return Color.parseColor("#0FADFF");
        if ("ToDo".equalsIgnoreCase(status)) return Color.parseColor("#22C55E");
        return Color.parseColor("#6B7280");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvTitle, tvMessage, tvTicket, tvTime, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTicket = itemView.findViewById(R.id.tvTicket);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}