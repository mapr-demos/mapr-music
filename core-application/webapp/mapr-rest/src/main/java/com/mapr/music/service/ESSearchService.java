package com.mapr.music.service;

import com.mapr.music.model.ESSearchResult;

public interface ESSearchService {

    /**
     * Finds Artists and Albums by specified name entry.
     *
     * @param nameEntry specifies search query.
     * @return list of artists and albums whose names contain specified name entry.
     */
    ESSearchResult findByNameEntry(String nameEntry);
}
