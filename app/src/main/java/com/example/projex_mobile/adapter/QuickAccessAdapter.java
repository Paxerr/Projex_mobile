package com.example.projex_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.QuickAccessItem;

import java.util.List;

public class QuickAccessAdapter extends RecyclerView.Adapter<QuickAccessAdapter.ViewHolder> {

    private final List<QuickAccessItem> items;

    public QuickAccessAdapter(List<QuickAccessItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_quick_access, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuickAccessItem item = items.get(position);
        holder.ivIcon.setImageResource(item.getIconRes());
        holder.tvName.setText(item.getName());
        holder.tvLabel.setText(item.getLabel());
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvLabel;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvName = itemView.findViewById(R.id.tvName);
            tvLabel = itemView.findViewById(R.id.tvLabel);
        }
    }
}