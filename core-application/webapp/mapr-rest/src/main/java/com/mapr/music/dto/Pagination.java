package com.mapr.music.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object which is used to provide pagination information.
 */
public class Pagination {

    @JsonProperty("per_page")
    private long perPage;
    private long items;
    private long page;
    private long pages;

    public Pagination() {
    }

    public Pagination(long perPage, long items, long page, long pages) {
        this.perPage = perPage;
        this.items = items;
        this.page = page;
        this.pages = pages;
    }

    public long getPerPage() {
        return perPage;
    }

    public void setPerPage(long perPage) {
        this.perPage = perPage;
    }

    public long getItems() {
        return items;
    }

    public void setItems(long items) {
        this.items = items;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}
