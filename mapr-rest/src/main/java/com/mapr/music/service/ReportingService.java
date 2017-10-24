package com.mapr.music.service;

import com.mapr.music.dao.ReportingDao;
import com.mapr.music.model.Pair;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * Responsible of performing reporting business logic.
 */
public class ReportingService {

    private final ReportingDao reportingDao;

    @Inject
    public ReportingService(@Named("reportingDao") ReportingDao reportingDao) {
        this.reportingDao = reportingDao;
    }

    /**
     * Returns top artists by area.
     *
     * @param numberOfRows specifies number of 'artists by area' rows, which will be returned.
     * @return top artists by area.
     */
    public List<Pair> getTopArtistByArea(int numberOfRows) {
        return reportingDao.getTopAreaForArtists(numberOfRows);
    }

    /**
     * Returns top languages for album.
     *
     * @param numberOfRows specifies number of 'languages for album' rows, which will be returned.
     * @return top languages for album.
     */
    public List<Pair> getTopLanguagesForAlbum(int numberOfRows) {
        return reportingDao.getTopLanguagesForAlbum(numberOfRows);
    }

    /**
     * Returns number of albums per year.
     *
     * @param numberOfRows specifies number of 'number of albums per year' rows, which will be returned.
     * @return number of albums per year.
     */
    public List<Pair> getNumberOfAlbumsPerYear(int numberOfRows) {
        return reportingDao.getNumberOfAlbumsPerYear(numberOfRows);
    }

}
