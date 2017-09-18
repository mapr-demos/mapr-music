package com.mapr.music.service;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.LanguageDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.impl.AlbumDaoImpl;
import com.mapr.music.dao.impl.ArtistDaoImpl;
import com.mapr.music.dao.impl.LanguageDaoImpl;
import com.mapr.music.dao.impl.MaprDbDaoImpl;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.exception.ValidationException;
import com.mapr.music.model.Album;
import com.mapr.music.service.impl.AlbumServiceImpl;
import com.mapr.music.service.impl.SlugService;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class AlbumServiceIntegrationTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        AlbumService.class, AlbumServiceImpl.class, AlbumDao.class, ArtistDaoImpl.class,
                        LanguageDao.class, LanguageDaoImpl.class, AlbumDaoImpl.class, MaprDbDao.class,
                        MaprDbDaoImpl.class, SlugService.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    AlbumService albumService;

    Album firstSample;
    Album secondSample;
    Album thirdSample;

    @Before
    public void create_sample_albums() {

        // Create several albums to be sure that second page may exist
        firstSample = new Album().setName("Test1").setLanguage("Test1").setStyle("Test1");
        secondSample = new Album().setName("Test2").setLanguage("Test2").setStyle("Test2");
        thirdSample = new Album().setName("Test3").setLanguage("Test3").setStyle("Test3");

        albumService.createAlbum(firstSample);
        albumService.createAlbum(secondSample);
        albumService.createAlbum(thirdSample);
    }

    @After
    public void cleanup() {
        albumService.deleteAlbumById(firstSample.getId());
        albumService.deleteAlbumById(secondSample.getId());
        albumService.deleteAlbumById(thirdSample.getId());
    }

    @Test
    public void should_get_by_id() {
        // Fetch album by id and compare fields.
        AlbumDto albumDto = albumService.getAlbumById(firstSample.getId());
        assertNotNull(albumDto);
        assertEquals(firstSample.getId(), albumDto.getId());
        assertEquals(firstSample.getName(), albumDto.getName());
        assertEquals(firstSample.getLanguage(), albumDto.getLanguage());
        assertEquals(firstSample.getStyle(), albumDto.getStyle());
    }

    @Test
    public void should_get_first_page() {

        ResourceDto<AlbumDto> firstPage = albumService.getAlbumsPage();

        assertNotNull(firstPage);
        assertNotNull(firstPage.getPagination());
        assertEquals(1, firstPage.getPagination().getPage());

        // At least sample album should present
        assertNotNull(firstPage.getResults());
        assertTrue(firstPage.getResults().size() >= 1);
    }

    @Test
    public void should_get_second_page() {

        // Specify per page value as 1, to be sure that second page may exist
        ResourceDto<AlbumDto> secondPage = albumService.getAlbumsPage(1L, 2L, null, null);

        assertNotNull(secondPage);
        assertNotNull(secondPage.getPagination());
        assertEquals(2L, secondPage.getPagination().getPage());

        // Only one album should present
        assertNotNull(secondPage.getResults());
        assertEquals(1, secondPage.getResults().size());

    }

    @Test
    public void should_be_ordered() {

        // Specify per page value as 1, to be sure that second page may exist
        ResourceDto<AlbumDto> firstPage = albumService.getAlbumsPage(null, 1L, "desc", Collections.singletonList("name"));

        assertNotNull(firstPage);

        List<AlbumDto> albumDtoList = firstPage.getResults();
        assertNotNull(albumDtoList);

        // Assert that album's name is 'less' than the name of the previous album
        for (int i = 1; i < albumDtoList.size(); i++) {

            AlbumDto current = albumDtoList.get(i);
            AlbumDto previous = albumDtoList.get(i - 1);

            assertNotNull(current);
            assertNotNull(previous);

            if (current.getName() != null && previous.getName() != null) {
                assertTrue(current.getName().compareTo(previous.getName()) <= 0);
            }

        }

    }

    @Test
    public void should_update() {

        String newName = "Updated";
        firstSample.setName(newName);
        albumService.updateAlbum(firstSample);
        AlbumDto albumDto = albumService.getAlbumById(firstSample.getId());
        assertNotNull(albumDto);
        assertEquals(firstSample.getId(), albumDto.getId());
        assertEquals(newName, albumDto.getName());

    }

    @Test(expected = Exception.class)
    public void should_delete() {

        Album sample = new Album().setName("Sample").setLanguage("Sample").setStyle("Sample");
        AlbumDto created = albumService.createAlbum(sample);

        assertNotNull(created);

        albumService.deleteAlbumById(sample.getId());
        albumService.getAlbumById(sample.getId());
    }

    @Test
    public void should_find_one_album() {

        String sampleName = firstSample.getName();
        String nameEntry = (sampleName.length() > 1) ? sampleName.substring(0, sampleName.length() - 1) : sampleName;
        long limit = 1;
        List<AlbumDto> albumDtoList = albumService.searchAlbums(nameEntry, limit);
        assertNotNull(albumDtoList);
        assertEquals(limit, albumDtoList.size());
        assertTrue(albumDtoList.get(0).getName().contains(nameEntry));
    }

    @Test(expected = ValidationException.class)
    public void should_throw_exception_with_empty_slug() {
        albumService.getAlbumBySlugName("");
    }

    @Test(expected = ValidationException.class)
    public void should_not_create_with_null_name() {
        albumService.createAlbum(new Album());
    }

}
