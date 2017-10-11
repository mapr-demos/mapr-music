package com.mapr.music.service;

import com.mapr.music.dto.Pagination;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PaginatedServiceTest {

    @Test
    public void testPaginationInfoByNonExistentPage() {

        final long itemsAmount = 11;
        final long page = 3;
        final long pageSize = 10;

        PaginatedService paginatedService = new PaginatedService() {
            @Override
            public long getTotalNum() {
                return itemsAmount;
            }
        };

        Pagination pagination = paginatedService.getPaginationInfo(3, 10);

        assertEquals("Number of items should be ", itemsAmount, pagination.getItems());
        assertEquals("Page should be ", page, pagination.getPage());
        assertEquals("Per page value should be ", pageSize, pagination.getPerPage());
        assertEquals("Number of pages should be", 2, pagination.getPages());
    }
}
