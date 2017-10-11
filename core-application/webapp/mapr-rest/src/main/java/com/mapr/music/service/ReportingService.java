package com.mapr.music.service;

import com.mapr.music.model.Pair;

import java.util.List;

public interface ReportingService {

    List<Pair> getTopArtistByArea(int numberOfRows);

    List<Pair> getTopLanguagesForAlbum(int numberOfRows);

    List<Pair> getNumberOfAlbumsPerYear(int numberOfRows);

}
