package com.mapr.music.service;

public interface StatisticService {

    /**
     * Returns total number of Album documents.
     *
     * @return total number of Album documents
     */
    long getTotalAlbums();

    /**
     * Returns total number of Artist documents.
     *
     * @return total number of Artist documents.
     */
    long getTotalArtists();

    /**
     * Recounts number of documents for Album/Artist tables and updates corresponding statistics documents.
     */
    void recomputeStatistics();
}
