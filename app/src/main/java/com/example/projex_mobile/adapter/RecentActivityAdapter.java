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
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {

    private List<RecentItem> items;

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
        holder.chipStatus.setText(item.getStatus());

        setAvatar(holder.imgAvatar, item.getAvatarText());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    private void setAvatar(ShapeableImageView imageView, String initials) {
        if (initials != null && !initials.isEmpty()) {
            int color = getAvatarColor(initials.hashCode());
            imageView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));

            imageView.setContentDescription(initials);  // Accessibility

            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
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
        TextView tvTitle, tvMessage, tvTicket, tvTime;
        ShapeableImageView imgAvatar;
        Chip chipStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTicket = itemView.findViewById(R.id.tvTicket);
            tvTime = itemView.findViewById(R.id.tvTime);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            chipStatus = itemView.findViewById(R.id.chipStatus);
        }
    }
}