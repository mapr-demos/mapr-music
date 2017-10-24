package com.mapr.music.service;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.AlbumRateDao;
import com.mapr.music.dao.LanguageDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.model.Artist;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.mockito.Mockito.mock;

public class AlbumServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void getNegativePageTest() {
        thrown.expect(IllegalArgumentException.class);

        AlbumDao albumDao = mock(AlbumDao.class);
        LanguageDao languageDao = mock(LanguageDao.class);
        SlugService slugService = mock(SlugService.class);
        StatisticService statisticService = mock(StatisticService.class);
        MaprDbDao<Artist> artistDao = mock(MaprDbDao.class);
        AlbumRateDao albumRateDao = mock(AlbumRateDao.class);
        AlbumService albumService = new AlbumService(albumDao, artistDao, languageDao, slugService,
                statisticService, albumRateDao);
        albumService.getAlbumsPage(-1L);
    }

    @Test
    public void getByNullId() {
        thrown.expect(IllegalArgumentException.class);

        AlbumDao albumDao = mock(AlbumDao.class);
        LanguageDao languageDao = mock(LanguageDao.class);
        SlugService slugService = mock(SlugService.class);
        StatisticService statisticService = mock(StatisticService.class);
        MaprDbDao<Artist> artistDao = mock(MaprDbDao.class);
        AlbumRateDao albumRateDao = mock(AlbumRateDao.class);
        AlbumService albumService = new AlbumService(albumDao, artistDao, languageDao, slugService,
                statisticService, albumRateDao);
        albumService.getAlbumById("");
    }

}
