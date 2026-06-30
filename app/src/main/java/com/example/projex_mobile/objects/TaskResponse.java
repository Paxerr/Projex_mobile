package com.example.projex_mobile.objects;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TaskResponse {

    @SerializedName(value = "items", alternate = {"data"})
    private List<Task> items;
    @SerializedName(value = "page", alternate = {"currentPage", "pageNumber", "pageIndex"})
    private Integer page;
    @SerializedName(value = "pageSize", alternate = {"size", "limit", "perPage"})
    private Integer pageSize;
    @SerializedName(value = "totalItems", alternate = {"totalCount", "total", "count", "totalRecords", "totalItemCount"})
    private Integer totalItems;
    @SerializedName(value = "totalPages", alternate = {"pages", "pageCount", "totalPage"})
    private Integer totalPages;

    public List<Task> getItems() {
        return items;
    }

    public void setItems(List<Task> items) {
        this.items = items;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public int resolveTotalPages(int fallbackPage, int fallbackPageSize) {
        if (totalPages != null) {
            return Math.max(1, totalPages);
        }

        int size = pageSize != null && pageSize > 0 ? pageSize : fallbackPageSize;
        if (totalItems != null && size > 0) {
            return Math.max(1, (int) Math.ceil(totalItems / (double) size));
        }

        int itemCount = items == null ? 0 : items.size();
        if (size > 0 && itemCount >= size) {
            return fallbackPage + 1;
        }

        return Math.max(1, fallbackPage);
    }
}
