package com.example.projex_mobile.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
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

    public interface OnItemClickListener {
        void onItemClick(QuickAccessItem item);
    }

    private static final int TYPE_PORTRAIT = 0;
    private static final int TYPE_LANDSCAPE = 1;

    private final List<QuickAccessItem> items;
    private final int layoutMode;
    private final OnItemClickListener listener;

    public QuickAccessAdapter(List<QuickAccessItem> items, int layoutMode, OnItemClickListener listener) {
        this.items = items;
        this.layoutMode = layoutMode;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return layoutMode;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = (viewType == TYPE_LANDSCAPE)
                ? R.layout.home_quick_access_land
                : R.layout.home_quick_access;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuickAccessItem item = items.get(position);

        holder.ivIcon.setImageResource(item.getIconRes());
        holder.tvName.setText(item.getName());
        holder.tvLabel.setText(item.getLabel());

        switch (item.getName()) {
            case "My Tasks":
                holder.ivIcon.setImageTintList(ColorStateList.valueOf(Color.parseColor("#4A80FF")));
                break;

            default:
                holder.ivIcon.setImageTintList(null);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
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