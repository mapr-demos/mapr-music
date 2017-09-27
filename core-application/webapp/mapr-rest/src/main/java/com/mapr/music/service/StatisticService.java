package com.mapr.music.service;

public interface StatisticService {

    long getTotalAlbums();

    void incrementAlbums();

    void decrementAlbums();

    long getTotalArtists();

    void incrementArtists();

    void decrementArtists();
}
