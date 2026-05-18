package com.example.projex_mobile.adapter;

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

        holder.tvAvatar.setText(item.getAvatarText() != null && !item.getAvatarText().isEmpty()
                ? item.getAvatarText()
                : "NA");

        setAvatarStyle(holder.tvAvatar, item.getAvatarText());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private void setAvatarStyle(TextView avatarView, String initials) {
        if (initials != null && !initials.isEmpty()) {
            int color = getAvatarColor(initials.hashCode());
            avatarView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));
            avatarView.setText(initials);
        } else {
            avatarView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6B7280")));
            avatarView.setText("NA");
        }
    }

    private int getAvatarColor(int seed) {
        int[] colors = {
                Color.parseColor("#FF6B6B"), Color.parseColor("#4ECDC4"),
                Color.parseColor("#45B7D1"), Color.parseColor("#96CEB4"),
                Color.parseColor("#FECA57"), Color.parseColor("#FF9FF3")
        };
        return colors[Math.abs(seed) % colors.length];
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvTitle, tvMessage, tvTicket, tvTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTicket = itemView.findViewById(R.id.tvTicket);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}