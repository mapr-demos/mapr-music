package com.mapr.music.service;

import com.mapr.music.dto.Pagination;

/**
 * Defines default method for computing pagination info for paginated services.
 */
public interface PaginatedService {

    /**
     * Returns total number of documents.
     *
     * @return total number of documents.
     */
    long getTotalNum();

    /**
     * Computes pagination info according to the specified page number and number of documents per page.
     *
     * @param page    page number.
     * @param perPage number of documents per page.
     * @return pagination info.
     */
    default Pagination getPaginationInfo(long page, long perPage) {
        return getPaginationInfo(page, perPage, getTotalNum());
    }

    /**
     * Computes pagination info according to the specified page number, number of documents per page and total number of
     * documents.
     *
     * @param page     page number.
     * @param perPage  number of documents per page.
     * @param totalNum total number of documents.
     * @return pagination info.
     */
    default Pagination getPaginationInfo(long page, long perPage, long totalNum) {
        long remainder = totalNum % perPage;
        long pages = (remainder == 0) ? totalNum / perPage : totalNum / perPage + 1;

        return new Pagination(perPage, totalNum, page, pages);
    }

}
