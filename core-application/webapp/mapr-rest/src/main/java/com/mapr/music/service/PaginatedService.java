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
        long totalNum = getTotalNum();
        long remainder = totalNum % perPage;
        long pages = (remainder == 0) ? totalNum / perPage : totalNum / perPage + 1;

        return new Pagination(perPage, totalNum, page, pages);
    }
}
