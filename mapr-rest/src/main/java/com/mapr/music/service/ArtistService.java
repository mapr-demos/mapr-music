package com.mapr.music.service;

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
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.ojai.types.ODate;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Responsible of performing all business logic on {@link Artist} model.
 */
public class ArtistService implements PaginatedService {

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
    private final StatisticService statisticService;

    @Inject
    public ArtistService(@Named("artistDao") ArtistDao artistDao, @Named("albumDao") MaprDbDao<Album> albumDao,
                         SlugService slugService, StatisticService statisticService) {

        this.artistDao = artistDao;
        this.albumDao = albumDao;
        this.slugService = slugService;
        this.statisticService = statisticService;
    }

    /**
     * {@inheritDoc}
     *
     * @return total number of artist documents.
     */
    @Override
    public long getTotalNum() {
        return statisticService.getTotalArtists();
    }

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class.
     *
     * @return artists page resource.
     */
    public ResourceDto<ArtistDto> getArtistsPage() {
        return getArtistsPage(FIRST_PAGE_NUM);
    }

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class.
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return artists page resource.
     */
    public ResourceDto<ArtistDto> getArtistsPage(Long page) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, page, null, null);
    }

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class. artists will be ordered according to the specified order and fields.
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    public ResourceDto<ArtistDto> getArtistsPage(String order, List<String> orderFields) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, FIRST_PAGE_NUM, order, orderFields);
    }

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class. artists will be ordered according to the specified order and fields.
     *
     * @param perPage     specifies number of artists per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return artists page resource.
     */
    public ResourceDto<ArtistDto> getArtistsPage(Long perPage, Long page, String order, List<String> orderFields) {

        List<SortOption> sortOptions = (order != null && orderFields != null)
                ? Collections.singletonList(new SortOption(order, orderFields))
                : Collections.emptyList();

        return getArtistsPage(perPage, page, sortOptions);
    }

    /**
     * Returns list of artists which is represented by page with default number of artists. Default number of artists
     * depends on implementation class. artists will be ordered according to the specified order and fields.
     *
     * @param perPage     specifies number of artists per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param sortOptions specifies artists ordering.
     * @return artists page resource.
     */
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
     * Returns single artist according to it's identifier.
     *
     * @param id artist's identifier.
     * @return artist with the specified identifier.
     */
    public ArtistDto getArtistById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        Artist artist = artistDao.getById(id);
        if (artist == null) {
            throw new ResourceNotFoundException("Artist with id '" + id + "' not found");
        }

        ArtistDto artistDto = artistToDto(artist);
        if (artist.getAlbums() != null) {
            List<AlbumDto> albumsDtoList = artist.getAlbums().stream()
                    .map(this::shortInfoToAlbumDto)
                    .collect(Collectors.toList());

            artistDto.setAlbums(albumsDtoList);
        }

        return artistDto;
    }

    /**
     * Returns single artist by it's slug name.
     *
     * @param slugName slug representation of artist's name which is used to generate readable and SEO-friendly URLs.
     * @return artist with specified slug name.
     */
    public ArtistDto getArtistBySlugName(String slugName) {

        if (slugName == null || slugName.isEmpty()) {
            throw new ValidationException("Artist's slug name can not be empty");
        }

        Artist artist = slugService.getArtistBySlug(slugName);
        if (artist == null) {
            throw new ResourceNotFoundException("Artist with slug name '" + slugName + "' not found");
        }

        ArtistDto artistDto = artistToDto(artist);
        if (artist.getAlbums() != null) {
            List<AlbumDto> albumsDtoList = artist.getAlbums().stream()
                    .map(this::shortInfoToAlbumDto)
                    .collect(Collectors.toList());

            artistDto.setAlbums(albumsDtoList);
        }

        return artistDto;
    }

    /**
     * Deletes single artist by it's identifier.
     *
     * @param id identifier of artist which will be deleted.
     */
    public void deleteArtistById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        Artist artist = artistDao.getById(id);
        if (artist == null) {
            throw new ResourceNotFoundException("Artist with id '" + id + "' not found");
        }

        artist.setDeleted(true);
        artistDao.update(id, artist);
    }

    /**
     * Creates artist according to the specified instance of {@link Artist} class.
     *
     * @param artistDto contains artist info.
     * @return created artist.
     */
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

        if (createdArtist.getAlbums() != null) {
            createdArtist.getAlbums().stream()
                    .filter(Objects::nonNull)
                    .map(Album.ShortInfo::getId)
                    .filter(Objects::nonNull)
                    .map(albumDao::getById)
                    .filter(Objects::nonNull)
                    .forEach(album -> {
                        album.addArtist(createdArtist.getShortInfo());
                        albumDao.update(album.getId(), album);
                    });
        }

        return artistToDto(createdArtist);
    }

    /**
     * Updates single artist according to the specified instance of {@link Artist} class.
     *
     * @param artistDto artist which will be updated. Note, that artist's id must be set, otherwise
     *                  {@link IllegalArgumentException} will be thrown.
     * @return updated artist.
     * @throws IllegalArgumentException in case when specified artist is <code>null</code> or it does not contain id.
     */
    public ArtistDto updateArtist(ArtistDto artistDto) {
        return updateArtist(artistDto.getId(), artistDto);
    }

    /**
     * Updates single artist according to the specified instance of {@link Artist} class.
     *
     * @param id        identifier of artist which will be updated.
     * @param artistDto artist which will be updated.
     * @return updated artist.
     */
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

        List<String> artistAlbumsIds = artist.getAlbums().stream()
                .filter(Objects::nonNull)
                .map(Album.ShortInfo::getId)
                .collect(Collectors.toList());

        if (artistAlbumsIds != null) {
            artistAlbumsIds.forEach(albumId -> {
                if (!albumDao.exists(albumId)) {
                    throw new ResourceNotFoundException("Albums with id " + albumId + "' not found");
                }
            });
        }

        List<String> existingArtistAlbumsIds = existingArtist.getAlbums().stream()
                .filter(Objects::nonNull)
                .map(Album.ShortInfo::getId)
                .collect(Collectors.toList());

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
                    .peek(album -> album.addArtist(existingArtist.getShortInfo()))
                    .forEach(album -> albumDao.update(album.getId(), album));
        }

        if (removedAlbumsIds != null && !removedAlbumsIds.isEmpty()) {

            removedAlbumsIds.stream()
                    .map(albumDao::getById)
                    .filter(Objects::nonNull)
                    .filter(album -> album.getArtists() != null)
                    .peek(album -> { // remove artist from albums
                        Artist.ShortInfo artistToRemove = null;
                        for (Artist.ShortInfo albumArtist : album.getArtists()) {
                            if (existingArtist.getId().equals(albumArtist.getId())) {
                                artistToRemove = albumArtist;
                                break;
                            }
                        }
                        album.getArtists().remove(artistToRemove);
                    })
                    .forEach(album -> albumDao.update(album.getId(), album));
        }

        Artist updatedArtist = artistDao.update(id, artist);
        ArtistDto updatedArtistDto = artistToDto(updatedArtist);
        if (updatedArtist.getAlbums() != null) {
            List<AlbumDto> albumsDtoList = updatedArtist.getAlbums().stream()
                    .map(this::shortInfoToAlbumDto)
                    .collect(Collectors.toList());

            updatedArtistDto.setAlbums(albumsDtoList);
        }

        return updatedArtistDto;
    }

    /**
     * Search artists according to the specified name entry. Returns artists which names start with name entry.
     *
     * @param nameEntry specifies search criteria.
     * @param limit     specifies number of artists, which will be returned. Can be overridden by actual service
     *                  implementation.
     * @return list of artists which names start with name entry.
     */
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

        if (artist.getBeginDate() != null) {
            artistDto.setBeginDateDay(artist.getBeginDate().toDate());
        }

        if (artist.getEndDate() != null) {
            artistDto.setEndDateDay(artist.getEndDate().toDate());
        }

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
            List<Album.ShortInfo> albums = artistDto.getAlbums().stream()
                    .map(this::albumDtoToShortInfo)
                    .collect(Collectors.toList());
            artist.setAlbums(albums);
        }

        if (artistDto.getBeginDateDay() != null) {
            artist.setBeginDate(new ODate(artistDto.getBeginDateDay()));
        }

        if (artistDto.getEndDateDay() != null) {
            artist.setEndDate(new ODate(artistDto.getEndDateDay()));
        }

        return artist;
    }

    private AlbumDto shortInfoToAlbumDto(Album.ShortInfo albumShortInfo) {

        AlbumDto albumDto = new AlbumDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(albumDto, albumShortInfo);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create album Data Transfer Object", e);
        }

        return albumDto;
    }

    private Album.ShortInfo albumDtoToShortInfo(AlbumDto albumDto) {

        Album.ShortInfo albumShortInfo = new Album.ShortInfo();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(albumShortInfo, albumDto);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create album Data Transfer Object", e);
        }

        return albumShortInfo;
    }

}
