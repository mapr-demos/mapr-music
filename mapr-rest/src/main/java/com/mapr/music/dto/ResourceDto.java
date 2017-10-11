package com.mapr.music.dto;

import java.util.List;

/**
 * Data Transfer Object which is used to provide resource page with pagination information.
 *
 * @param <T> type of actual resource.
 */
public class ResourceDto<T> {

    private Pagination pagination;
    private List<T> results;

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }
}
