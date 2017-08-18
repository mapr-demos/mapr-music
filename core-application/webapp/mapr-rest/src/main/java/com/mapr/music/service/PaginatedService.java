package com.mapr.music.service;

import com.mapr.music.dto.Pagination;

public interface PaginatedService {

    long getTotalNum();

    default Pagination getPaginationInfo(long page, long pageSize) {
        long totalNum = getTotalNum();
        long remainder = totalNum % pageSize;
        long pages = (remainder == 0) ? totalNum / pageSize : totalNum / pageSize + 1;

        return new Pagination(pageSize, totalNum, page, pages);
    }
}
