package com.mapr.music.service;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.LanguageDao;
import com.mapr.music.service.impl.AlbumServiceImpl;
import com.mapr.music.service.impl.SlugService;
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
        AlbumService albumService = new AlbumServiceImpl(albumDao, languageDao, slugService);
        albumService.getAlbumsPage(-1L);
    }

    @Test
    public void getByNullId() {
        thrown.expect(IllegalArgumentException.class);

        AlbumDao albumDao = mock(AlbumDao.class);
        LanguageDao languageDao = mock(LanguageDao.class);
        SlugService slugService = mock(SlugService.class);
        AlbumService albumService = new AlbumServiceImpl(albumDao, languageDao, slugService);
        albumService.getAlbumById("");
    }

}
