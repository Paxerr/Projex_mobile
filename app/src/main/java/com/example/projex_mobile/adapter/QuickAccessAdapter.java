package com.example.projex_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
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

        int tintColor;
        switch (item.getName()) {
            case "My Tasks":
                tintColor = Color.parseColor("#4A80FF");
                break;
            case "Projects":
                tintColor = Color.parseColor("#FFECB3");
                break;
            case "Reports":
                tintColor = Color.parseColor("#7C4DFF");
                break;
            case "Team":
                tintColor = Color.parseColor("#4ECDC4");
                break;
            default:
                tintColor = Color.parseColor("#4A80FF");
                break;
        }

        holder.ivIcon.setImageTintList(ColorStateList.valueOf(tintColor));
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