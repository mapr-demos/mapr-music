package com.mapr.music.service;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.StatisticDao;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.exception.ResourceNotFoundException;
import com.mapr.music.exception.ValidationException;
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
public class ArtistServiceIntegrationTest {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClasses(
                        ArtistService.class, ArtistService.class, ArtistDao.class, ArtistDao.class,
                        AlbumDao.class, AlbumDao.class, MaprDbDao.class, MaprDbDao.class, SlugService.class,
                        StatisticServiceMock.class, StatisticDao.class, StatisticDao.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    ArtistService artistService;

    @Inject
    ArtistDao artistDao;

    ArtistDto firstSample;
    ArtistDto secondSample;
    ArtistDto thirdSample;

    @Before
    public void create_sample_albums() {

        // Create several sample artists
        firstSample = new ArtistDto();
        firstSample.setName("Test1");

        secondSample = new ArtistDto();
        secondSample.setName("Test2");

        thirdSample = new ArtistDto();
        thirdSample.setName("Test3");

        firstSample = artistService.createArtist(firstSample);
        secondSample = artistService.createArtist(secondSample);
        thirdSample = artistService.createArtist(thirdSample);
    }

    @After
    public void cleanup() {
        artistDao.deleteById(firstSample.getId());
        artistDao.deleteById(secondSample.getId());
        artistDao.deleteById(thirdSample.getId());
    }

    @Test
    public void should_get_by_id() {
        // Fetch artist by id and compare fields.
        ArtistDto artistDto = artistService.getArtistById(firstSample.getId());
        assertNotNull(artistDto);
        assertEquals(firstSample.getId(), artistDto.getId());
        assertEquals(firstSample.getName(), artistDto.getName());
    }

    @Test
    public void should_get_first_page() {

        ResourceDto<ArtistDto> firstPage = artistService.getArtistsPage();

        assertNotNull(firstPage);
        assertNotNull(firstPage.getPagination());
        assertEquals(1, firstPage.getPagination().getPage());

        // At least sample artist should present
        assertNotNull(firstPage.getResults());
        assertTrue(firstPage.getResults().size() >= 1);
    }

    @Test
    public void should_get_second_page() {

        // Specify per page value as 1, to be sure that second page may exist
        ResourceDto<ArtistDto> secondPage = artistService.getArtistsPage(1L, 2L, null, null);

        assertNotNull(secondPage);
        assertNotNull(secondPage.getPagination());
        assertEquals(2L, secondPage.getPagination().getPage());

        // Only one artist should present
        assertNotNull(secondPage.getResults());
        assertEquals(1, secondPage.getResults().size());

    }

    @Test
    public void should_be_ordered() {

        // Specify per page value as 1, to be sure that second page may exist
        ResourceDto<ArtistDto> firstPage = artistService.getArtistsPage(null, 1L, "desc", Collections.singletonList("name"));

        assertNotNull(firstPage);

        List<ArtistDto> artistDtoList = firstPage.getResults();
        assertNotNull(artistDtoList);

        // Assert that album's name is 'less' than the name of the previous album
        for (int i = 1; i < artistDtoList.size(); i++) {

            ArtistDto current = artistDtoList.get(i);
            ArtistDto previous = artistDtoList.get(i - 1);

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
        artistService.updateArtist(firstSample);
        ArtistDto artistDto = artistService.getArtistById(firstSample.getId());
        assertNotNull(artistDto);
        assertEquals(firstSample.getId(), artistDto.getId());
        assertEquals(newName, artistDto.getName());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void should_not_get_deleted() {

        ArtistDto sample = new ArtistDto();
        sample.setName("Sample");

        ArtistDto created = artistService.createArtist(sample);
        assertNotNull(created);

        artistDao.deleteById(created.getId());
        artistService.getArtistById(created.getId());
    }

    @Test
    public void should_find_one_artist() {

        String sampleName = firstSample.getName();
        String nameEntry = (sampleName.length() > 1) ? sampleName.substring(0, sampleName.length() - 1) : sampleName;
        long limit = 1;
        List<ArtistDto> artistDtoList = artistService.searchArtists(nameEntry, limit);
        assertNotNull(artistDtoList);
        assertEquals(limit, artistDtoList.size());
        assertTrue(artistDtoList.get(0).getName().contains(nameEntry));
    }

    @Test(expected = ValidationException.class)
    public void should_throw_exception_with_empty_slug() {
        artistService.getArtistBySlugName("");
    }

    @Test(expected = ValidationException.class)
    public void should_not_create_with_null_name() {
        artistService.createArtist(new ArtistDto());
    }

}
