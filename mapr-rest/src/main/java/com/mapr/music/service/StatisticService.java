package com.mapr.music.service;

public interface StatisticService {

    long getTotalAlbums();

    long getTotalArtists();

    /**
     * Recounts number of documents for Album/Artist tables and updates corresponding statistics documents.
     */
    void recomputeStatistics();
}
