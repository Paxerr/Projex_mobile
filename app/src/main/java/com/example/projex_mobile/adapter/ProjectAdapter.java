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
                .inflate(R.layout.project_item, parent, false);
        return new ProjectViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static String getInitial(String name) {
        if (name == null || name.trim().isEmpty()) return "?";

        String[] parts = name.trim().split("[\\s_\\-]+");
        StringBuilder sb = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (sb.length() == 2) break;
            }
        }

        if (sb.length() == 0) {
            sb.append(Character.toUpperCase(name.trim().charAt(0)));
        }

        return sb.toString();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvProjectInitial, tvProjectName, tvProjectStatus, tvProjectMemberCount;
        ImageView imgFavorite;
        private ProjectItem currentItem;

        public ProjectViewHolder(@NonNull View itemView, OnProjectClickListener listener) {
            super(itemView);

            tvProjectInitial = itemView.findViewById(R.id.tvProjectInitial);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvProjectName = itemView.findViewById(R.id.tvProjectName);
            tvProjectStatus = itemView.findViewById(R.id.tvProjectStatus);
            tvProjectMemberCount = itemView.findViewById(R.id.tvProjectMemberCount);

            itemView.setOnClickListener(v -> {
                if (listener != null && currentItem != null) {
                    listener.onProjectClick(currentItem);
                }
            });

            imgFavorite.setOnClickListener(v -> {
                if (listener != null && currentItem != null) {
                    listener.onFavoriteClick(currentItem);
                }
            });
        }

        public void bind(ProjectItem item) {
            currentItem = item;

            tvProjectName.setText(item.getName());
            tvProjectStatus.setText(item.getStatus() != null ? item.getStatus() : "");
            tvProjectMemberCount.setText(item.getMemberCount() + " members");
            tvProjectInitial.setText(getInitial(item.getName()));

            if (item.isFavorite()) {
                imgFavorite.setImageResource(R.drawable.ic_favorite);
                imgFavorite.setColorFilter(Color.parseColor("#85ADFF"));
            } else {
                imgFavorite.setImageResource(R.drawable.ic_favorite_border);
                imgFavorite.setColorFilter(Color.parseColor("#6B7280"));
            }
        }
    }
}