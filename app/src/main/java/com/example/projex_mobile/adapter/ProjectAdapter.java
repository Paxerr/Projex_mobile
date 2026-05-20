package com.example.projex_mobile.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projex_mobile.R;
import com.example.projex_mobile.objects.ProjectItem;

import java.util.ArrayList;
import java.util.List;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectClickListener {
        void onProjectClick(ProjectItem item);
        void onFavoriteClick(ProjectItem item);
    }

    private final List<ProjectItem> items = new ArrayList<>();
    private final OnProjectClickListener listener;

    public ProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<ProjectItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectItem item = items.get(position);

        holder.tvProjectName.setText(item.getName());
        holder.tvProjectStatus.setText(item.getStatus() != null ? item.getStatus() : "");
        holder.tvProjectMemberCount.setText(item.getMemberCount() + " members");

        if (item.getImageResId() != 0) {
            holder.imgProjectIcon.setImageResource(item.getImageResId());
        } else {
            holder.imgProjectIcon.setImageResource(R.drawable.ic_logo);
        }

        if (item.isFavorite()) {
            holder.imgFavorite.setImageResource(R.drawable.ic_favorite_border);
            holder.imgFavorite.setColorFilter(Color.parseColor("#85ADFF"));
        } else {
            holder.imgFavorite.setImageResource(R.drawable.ic_favorite);
            holder.imgFavorite.setColorFilter(Color.parseColor("#6B7280"));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProjectClick(item);
        });

        holder.imgFavorite.setOnClickListener(v -> {
            if (listener != null) listener.onFavoriteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProjectIcon, imgFavorite;
        TextView tvProjectName, tvProjectStatus, tvProjectMemberCount;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProjectIcon = itemView.findViewById(R.id.imgProjectIcon);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectStatus = itemView.findViewById(R.id.tvProjectStatus);
            tvProjectMemberCount = itemView.findViewById(R.id.tvProjectMemberCount);
        }
    }
}