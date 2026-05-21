package com.example.projex_mobile.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.NotificationItem;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<NotificationItem> items = new ArrayList<>();

    public NotificationAdapter(List<NotificationItem> items) {
        setData(items);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<NotificationItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
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

        String shortName = item.avatarText != null && item.avatarText.length() >= 2
                ? item.avatarText.substring(0, 2)
                : item.avatarText != null ? item.avatarText : "";
        holder.tvAvatar.setText(shortName);

        if (item.isUnread) {
            holder.vDot.setVisibility(View.VISIBLE);
            holder.vDot.setBackgroundResource(R.drawable.notice_dot_unread);
        } else {
            holder.vDot.setVisibility(View.INVISIBLE);
            holder.vDot.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.transparent));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View vDot;
        TextView tvAvatar, tvTitle, tvMessage, tvTicket;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            vDot = itemView.findViewById(R.id.vDot);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTicket = itemView.findViewById(R.id.tvTicket);
        }
    }
}