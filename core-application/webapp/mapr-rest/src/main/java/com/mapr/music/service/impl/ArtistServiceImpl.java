package com.mapr.music.service.impl;

import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.exception.ResourceNotFoundException;
import com.mapr.music.exception.ValidationException;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.PaginatedService;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Actual implementation of {@link ArtistService} which is responsible of performing all business logic.
 */
public class ArtistServiceImpl implements ArtistService, PaginatedService {

    private static final long ARTISTS_PER_PAGE_DEFAULT = 50;
    private static final long FIRST_PAGE_NUM = 1;
    private static final long MAX_SEARCH_LIMIT = 15;

    private static final String[] ARTIST_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "slug_name",
            "slug_postfix",
            "gender",
            "area",
            "IPI",
            "ISNI",
            "MBID",
            "disambiguation_comment",
            "albums",
            "profile_image_url",
            "images_urls",
            "begin_date",
            "end_date"
    };


    private final ArtistDao artistDao;
    private final MaprDbDao<Album> albumDao;
    private final SlugService slugService;

    @Inject
    public ArtistServiceImpl(@Named("artistDao") ArtistDao artistDao,
                             @Named("albumDao") MaprDbDao<Album> albumDao, SlugService slugService) {

        this.artistDao = artistDao;
        this.albumDao = albumDao;
        this.slugService = slugService;
    }

    /**
     * {@inheritDoc}
     *
     * @return total number of artist documents.
     */
    @Override
    public long getTotalNum() {
        return artistDao.getTotalNum();
    }

    /**
     * {@inheritDoc}
     *
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage() {
        return getArtistsPage(FIRST_PAGE_NUM);
    }

    /**
     * {@inheritDoc}
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage(Long page) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, page, null, null);
    }

    /**
     * {@inheritDoc}
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage(String order, List<String> orderFields) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, FIRST_PAGE_NUM, order, orderFields);
    }

    /**
     * {@inheritDoc}
     *
     * @param perPage     specifies number of artists per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage(Long perPage, Long page, String order, List<String> orderFields) {

        List<SortOption> sortOptions = (order != null && orderFields != null)
                ? Collections.singletonList(new SortOption(order, orderFields))
                : Collections.emptyList();

        return getArtistsPage(perPage, page, sortOptions);
    }

    /**
     * {@inheritDoc}
     *
     * @param perPage     specifies number of artists per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param sortOptions specifies artists ordering.
     * @return artists page resource.
     */
    @Override
    public ResourceDto<ArtistDto> getArtistsPage(Long perPage, Long page, List<SortOption> sortOptions) {

        if (page == null) {
            page = FIRST_PAGE_NUM;
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        if (perPage == null) {
            perPage = ARTISTS_PER_PAGE_DEFAULT;
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per page value must be greater than zero");
        }

        ResourceDto<ArtistDto> artistsPage = new ResourceDto<>();
        artistsPage.setPagination(getPaginationInfo(page, perPage));
        long offset = (page - 1) * perPage;

        List<Artist> artists = artistDao.getList(offset, perPage, sortOptions, ARTIST_SHORT_INFO_FIELDS);
        List<ArtistDto> artistDtoList = artists.stream()
                .map(this::artistToDto)
                .collect(Collectors.toList());

        artistsPage.setResults(artistDtoList);
        return artistsPage;
    }

    /**
     * {@inheritDoc}
     *
     * @param id artist's identifier.
     * @return artist with the specified identifier.
     */
    @Override
    public ArtistDto getArtistById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        Artist artist = artistDao.getById(id);
        if (artist == null) {
            throw new ResourceNotFoundException("Artist with id '" + id + "' not found");
        }

        ArtistDto artistDto = artistToDto(artist);
        List<AlbumDto> albumsDtoList = albumsIdsToDtoList(artist.getAlbumsIds());
        artistDto.setAlbums(albumsDtoList);

        return artistDto;
    }

    /**
     * {@inheritDoc}
     *
     * @param slugName slug representation of artist's name which is used to generate readable and SEO-friendly URLs.
     * @return artist with specified slug name.
     */
    @Override
    public ArtistDto getArtistBySlugName(String slugName) {

        if (slugName == null || slugName.isEmpty()) {
            throw new ValidationException("Artist's slug name can not be empty");
        }

        Artist artist = slugService.getArtistBySlug(slugName);
        if (artist == null) {
            throw new ResourceNotFoundException("Artist with slug name '" + slugName + "' not found");
        }

        ArtistDto artistDto = artistToDto(artist);
        List<AlbumDto> albumsDtoList = albumsIdsToDtoList(artist.getAlbumsIds());
        artistDto.setAlbums(albumsDtoList);

        return artistDto;
    }

    /**
     * {@inheritDoc}
     *
     * @param id identifier of artist which will be deleted.
     */
    @Override
    public void deleteArtistById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        if (!artistDao.exists(id)) {
            throw new ResourceNotFoundException("Artist with id '" + id + "' not found");
        }

        artistDao.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @param artistDto contains artist info.
     * @return created artist.
     */
    @Override
    public ArtistDto createArtist(ArtistDto artistDto) {

        if (artistDto == null) {
            throw new ValidationException("Artist can not be null");
        }

        if (artistDto.getName() == null) {
            throw new ValidationException("Artist's name can not be null");
        }

        Artist artist = dtoToArtist(artistDto);

        String id = UUID.randomUUID().toString();
        artist.setId(id);

        slugService.setSlugForArtist(artist);
        Artist createdArtist = artistDao.create(artist);

        if (createdArtist.getAlbumsIds() != null) {
            createdArtist.getAlbumsIds().stream()
                    .filter(albumId -> albumId != null && !albumId.isEmpty())
                    .map(albumDao::getById)
                    .filter(Objects::nonNull)
                    .forEach(album -> {
                        album.addArtist(createdArtist);
                        albumDao.update(album.getId(), album);
                    });
        }

        return artistToDto(createdArtist);
    }

    /**
     * {@inheritDoc}
     *
     * @param artistDto artist which will be updated. Note, that artist's id must be set, otherwise
     *                  {@link IllegalArgumentException} will be thrown.
     * @return updated artist.
     */
    @Override
    public ArtistDto updateArtist(ArtistDto artistDto) {
        return updateArtist(artistDto.getId(), artistDto);
    }

    /**
     * {@inheritDoc}
     *
     * @param id        identifier of artist which will be updated.
     * @param artistDto artist which will be updated.
     * @return updated artist.
     */
    @Override
    public ArtistDto updateArtist(String id, ArtistDto artistDto) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        if (artistDto == null) {
            throw new IllegalArgumentException("Artist can not be null");
        }

        Artist artist = dtoToArtist(artistDto);
        Artist existingArtist = artistDao.getById(id);
        if (existingArtist == null) {
            throw new ResourceNotFoundException("Artist with id '" + id + "' not found");
        }

        List<String> artistAlbumsIds = artist.getAlbumsIds();

        if (artistAlbumsIds != null) {
            artistAlbumsIds.forEach(albumId -> {
                if (!albumDao.exists(albumId)) {
                    throw new ResourceNotFoundException("Albums with id " + albumId + "' not found");
                }
            });
        }

        List<String> existingArtistAlbumsIds = existingArtist.getAlbumsIds();

        List<String> addedAlbumsIds = null;
        List<String> removedAlbumsIds = null;
        if (artistAlbumsIds == null || artistAlbumsIds.isEmpty()) {
            removedAlbumsIds = existingArtistAlbumsIds;
        } else if (existingArtistAlbumsIds == null || existingArtistAlbumsIds.isEmpty()) {
            addedAlbumsIds = artistAlbumsIds;
        } else {

            addedAlbumsIds = new ArrayList<>(artistAlbumsIds);
            addedAlbumsIds.removeAll(existingArtistAlbumsIds);

            removedAlbumsIds = new ArrayList<>(existingArtistAlbumsIds);
            removedAlbumsIds.removeAll(artistAlbumsIds);
        }

        if (addedAlbumsIds != null && !addedAlbumsIds.isEmpty()) {

            addedAlbumsIds.stream()
                    .map(albumDao::getById)
                    .filter(Objects::nonNull)
                    .peek(album -> album.addArtist(existingArtist))
                    .forEach(album -> albumDao.update(album.getId(), album));
        }

        if (removedAlbumsIds != null && !removedAlbumsIds.isEmpty()) {

            removedAlbumsIds.stream()
                    .map(albumDao::getById)
                    .filter(Objects::nonNull)
                    .filter(album -> album.getArtistList() != null)
                    .peek(album -> { // remove artist from albums
                        Artist artistToRemove = null;
                        for (Artist albumArtist : album.getArtistList()) {
                            if (existingArtist.getId().equals(albumArtist.getId())) {
                                artistToRemove = albumArtist;
                                break;
                            }
                        }
                        album.getArtistList().remove(artistToRemove);
                    })
                    .forEach(album -> albumDao.update(album.getId(), album));
        }

        Artist updatedArtist = artistDao.update(id, artist);
        ArtistDto updatedArtistDto = artistToDto(updatedArtist);
        List<AlbumDto> albumsDtoList = albumsIdsToDtoList(updatedArtist.getAlbumsIds());
        updatedArtistDto.setAlbums(albumsDtoList);

        return updatedArtistDto;
    }

    /**
     * {@inheritDoc}
     *
     * @param nameEntry specifies search criteria.
     * @param limit     specifies number of artists, which will be returned. Can be overridden by actual service
     *                  implementation.
     * @return list of artists which names start with name entry.
     */
    @Override
    public List<ArtistDto> searchArtists(String nameEntry, Long limit) {

        long actualLimit = (limit != null && limit > 0 && limit < MAX_SEARCH_LIMIT) ? limit : MAX_SEARCH_LIMIT;
        List<Artist> artists = artistDao.getByNameStartsWith(nameEntry, actualLimit, ARTIST_SHORT_INFO_FIELDS);

        return artists.stream()
                .map(this::artistToDto)
                .collect(Collectors.toList());
    }


    private ArtistDto artistToDto(Artist artist) {
        ArtistDto artistDto = new ArtistDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(artistDto, artist);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create artist Data Transfer Object", e);
        }

        String slug = slugService.getSlugForArtist(artist);
        artistDto.setSlug(slug);

        return artistDto;
    }

    private Artist dtoToArtist(ArtistDto artistDto) {
        Artist artist = new Artist();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(artist, artistDto);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create Artist from Data Transfer Object", e);
        }

        if (artistDto.getAlbums() != null) {
            List<String> albumsIds = artistDto.getAlbums().stream().map(AlbumDto::getId).collect(Collectors.toList());
            artist.setAlbumsIds(albumsIds);
        }

        return artist;
    }

    private AlbumDto albumToDto(Album album) {
        AlbumDto albumDto = new AlbumDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(albumDto, album);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create album Data Transfer Object", e);
        }

        String slug = slugService.getSlugForAlbum(album);
        albumDto.setSlug(slug);

        return albumDto;
    }

    private List<AlbumDto> albumsIdsToDtoList(List<String> albumsIds) {

        if (albumsIds == null) {
            return Collections.emptyList();
        }

        return albumsIds.stream()
                .map(albumId -> albumDao.getById(albumId, "_id", "name", "slug_name", "slug_postfix", "cover_image_url", "released_date"))
                .map(this::albumToDto)
                .collect(Collectors.toList());
    }

}
