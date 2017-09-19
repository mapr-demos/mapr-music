package com.mapr.music.service.impl;

import com.mapr.music.dao.ReportingDao;
import com.mapr.music.model.Pair;
import com.mapr.music.service.ReportingService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Implementation of {@link ReportingService} which is responsible of performing all business logic.
 */
public class ReportingServiceImpl implements ReportingService {

  private final ReportingDao reportingDao;


  @Inject
  public ReportingServiceImpl(@Named("reportingDao") ReportingDao reportingDao) {
    this.reportingDao = reportingDao;
  }

  @Override
  public List<Pair> getTopArtistByArea(int numberOfRows) {
    return reportingDao.getTopAreaForArtists(numberOfRows);
  }



  @Override
  public List<Pair> getTopLanguagesForAlbum(int numberOfRows) {
    return reportingDao.getTopLanguagesForAlbum(numberOfRows);
  }

  @Override
  public List<Pair> getNumberOfAlbumsPerYear(int numberOfRows) {
    return reportingDao.getNumberOfAlbumsPerYear(numberOfRows);
  }

}
