package com.mapr.music.service;

import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.ESSearchResult;

public interface ESSearchService {

    /**
     * Finds Artists and Albums by specified name entry.
     *
     * @param nameEntry specifies search query.
     * @param perPage   specifies number of search results per page. In case when value is <code>null</code> the
     *                  default value will be used. Default value depends on implementation class.
     * @param page      specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                  the first page will be returned.
     * @return list of artists and albums whose names contain specified name entry.
     */
    ResourceDto<ESSearchResult> findByNameEntry(String nameEntry, Integer perPage, Integer page);

    /**
     * Finds Albums by specified name entry.
     *
     * @param nameEntry specifies search query.
     * @param perPage   specifies number of search results per page. In case when value is <code>null</code> the
     *                  default value will be used. Default value depends on implementation class.
     * @param page      specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                  the first page will be returned.
     * @return list of albums whose names contain specified name entry.
     */
    ResourceDto<ESSearchResult> findAlbumsByNameEntry(String nameEntry, Integer perPage, Integer page);

    /**
     * Finds Artists by specified name entry.
     *
     * @param nameEntry specifies search query.
     * @param perPage   specifies number of search results per page. In case when value is <code>null</code> the
     *                  default value will be used. Default value depends on implementation class.
     * @param page      specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                  the first page will be returned.
     * @return list of artists whose names contain specified name entry.
     */
    ResourceDto<ESSearchResult> findArtistsByNameEntry(String nameEntry, Integer perPage, Integer page);
}
