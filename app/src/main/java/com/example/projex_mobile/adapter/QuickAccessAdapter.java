package com.example.projex_mobile.adapter;  // ✅ Package đúng

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.projex_mobile.R;  // ✅ Sửa package R
import com.example.projex_mobile.objects.QuickAccessItem;  // ✅ Sửa package Model
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
                .inflate(R.layout.item_quick_access, parent, false);  // ✅ Layout đúng
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuickAccessItem item = items.get(position);
        holder.ivIcon.setImageResource(item.getIconRes());  // ✅ Getter (style bạn bạn)
        holder.tvTitle.setText(item.getTitle());           // ✅ Chỉ 1 TextView (đơn giản)
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvTitle;  // ✅ Bỏ tvLabel (đơn giản)

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}