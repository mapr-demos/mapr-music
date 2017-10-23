package com.mapr.music.service;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.StatisticDao;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Wrapper for actual {@link CdcStatisticService}, since {@link CdcStatisticService} has {@link javax.ejb.Startup}
 * annotation and can not be created at test execution time.
 */
public class StatisticServiceMock implements StatisticService {

    private StatisticService actualService;

    @Inject
    public StatisticServiceMock(@Named("statisticDao") StatisticDao statisticDao, @Named("albumDao") AlbumDao albumDao,
                                @Named("artistDao") ArtistDao artistDao) {
        this.actualService = new CdcStatisticService(statisticDao, albumDao, artistDao);
    }

    @Override
    public long getTotalAlbums() {
        return this.actualService.getTotalAlbums();
    }

    @Override
    public long getTotalArtists() {
        return this.actualService.getTotalArtists();
    }

    @Override
    public void recomputeStatistics() {
        this.actualService.recomputeStatistics();
    }
}
