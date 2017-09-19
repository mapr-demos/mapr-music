package com.mapr.music.service;

import com.mapr.music.model.Pair;

import java.util.List;
import java.util.Map;

public interface ReportingService {

  public List<Pair> getTopArtistByArea(int numberOfRows);

  public List<Pair> getTopLanguagesForAlbum(int numberOfRows);

  public List<Pair> getNumberOfAlbumsPerYear(int numberOfRows);

}
