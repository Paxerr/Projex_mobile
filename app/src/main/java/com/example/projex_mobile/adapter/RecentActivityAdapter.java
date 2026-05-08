package com.example.projex_mobile.adapter;  // ✅ Package đúng

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;  // ✅ Import này
import androidx.recyclerview.widget.RecyclerView;
import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.RecentActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import java.util.List;

public class RecentActivityAdapter extends RecyclerView.Adapter<RecentActivityAdapter.ViewHolder> {
    private List<RecentActivity> items;

    public RecentActivityAdapter(List<RecentActivity> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_activity, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentActivity item = items.get(position);

        holder.tvTitle.setText(item.getTitle());
        holder.tvMessage.setText(item.getMessage());
        holder.tvTicket.setText(item.getTicketCode());
        holder.tvTime.setText(item.getTimeAgo());
        holder.chipStatus.setText(item.getStatus());
        holder.imgAvatar.setText(item.getAvatarText());
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {  // ✅ static OK
        TextView tvTitle, tvMessage, tvTicket, tvTime;
        ShapeableImageView imgAvatar;
        Chip chipStatus;

        ViewHolder(@NonNull View itemView) {  // ✅ @NonNull OK
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