package com.example.projex_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.NotificationItem;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationItem> items;

    public NotificationAdapter(List<NotificationItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationItem item = items.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvMessage.setText(item.message);
        holder.tvTicket.setText(item.ticket);

        // Avatar chữ cái
        String shortName = item.avatarText.length() >= 2
                ? item.avatarText.substring(0, 2)
                : item.avatarText;
        holder.tvAvatar.setText(shortName);

        // Hiển thị dot chưa đọc hoặc bỏ (trong suốt)
        if (item.isUnread) {
            holder.vDot.setBackgroundResource(R.drawable.notice_dot_unread);
        } else {
            holder.vDot.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
        }

        // Ẩn separator nếu là item cuối
        boolean isLast = position == getItemCount() - 1;
        holder.vSeparator.setVisibility(isLast ? View.GONE : View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View vDot, vSeparator;
        TextView tvAvatar, tvTitle, tvMessage, tvTicket;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vDot = itemView.findViewById(R.id.vDot);
            vSeparator = itemView.findViewById(R.id.vSeparator);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTicket = itemView.findViewById(R.id.tvTicket);
        }
    }
}