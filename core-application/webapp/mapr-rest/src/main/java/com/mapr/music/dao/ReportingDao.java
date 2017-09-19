package com.mapr.music.dao;

import com.mapr.music.model.Pair;

import java.util.List;
import java.util.Map;

public interface ReportingDao {

  public List<Pair> getTopAreaForArtists(int i);

  public List<Pair> getTopLanguagesForAlbum(int numberOfRows);

  public List<Pair> getNumberOfAlbumsPerYear(int numberOfRows);


  }
