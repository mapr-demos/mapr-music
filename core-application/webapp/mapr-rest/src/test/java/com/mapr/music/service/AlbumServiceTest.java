package com.mapr.music.service;

import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.service.impl.AlbumServiceImpl;
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

        MaprDbDao<Album> albumDao = mock(MaprDbDao.class);
        MaprDbDao<Artist> artistDao = mock(MaprDbDao.class);
        AlbumService albumService = new AlbumServiceImpl(albumDao, artistDao);
        albumService.getAlbumsPage(-1L);
    }

    @Test
    public void getByNullId() {
        thrown.expect(IllegalArgumentException.class);
        MaprDbDao<Album> albumDao = mock(MaprDbDao.class);
        MaprDbDao<Artist> artistDao = mock(MaprDbDao.class);
        AlbumService albumService = new AlbumServiceImpl(albumDao, artistDao);
        albumService.getAlbumById("");
    }

}
