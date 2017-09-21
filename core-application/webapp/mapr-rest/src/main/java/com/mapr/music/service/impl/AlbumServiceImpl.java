package com.mapr.music.service.impl;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.LanguageDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.dto.TrackDto;
import com.mapr.music.exception.ResourceNotFoundException;
import com.mapr.music.exception.ValidationException;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.model.Language;
import com.mapr.music.model.Track;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.PaginatedService;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Actual implementation of {@link AlbumService} which is responsible of performing all business logic.
 */
public class AlbumServiceImpl implements AlbumService, PaginatedService {

    private static final long ALBUMS_PER_PAGE_DEFAULT = 50;
    private static final long FIRST_PAGE_NUM = 1;
    private static final long MAX_SEARCH_LIMIT = 15;
    private static final long MAX_RECOMMENDED_LIMIT = 5;

    /**
     * Array of album's fields that will be used for projection.
     */
    private static final String[] ALBUM_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "slug_name",
            "slug_postfix",
            "genre",
            "style",
            "barcode",
            "format",
            "country",
            "catalog_numbers",
            "released_date",
            "cover_image_url",
            "artists"
    };


    private final AlbumDao albumDao;
    private final MaprDbDao<Artist> artistDao;
    private final LanguageDao languageDao;
    private final SlugService slugService;

    @Inject
    public AlbumServiceImpl(@Named("albumDao") AlbumDao albumDao, @Named("artistDao") MaprDbDao<Artist> artistDao,
                            LanguageDao languageDao, SlugService slugService) {

        this.albumDao = albumDao;
        this.artistDao = artistDao;
        this.languageDao = languageDao;
        this.slugService = slugService;
    }

    @Override
    public long getTotalNum() {
        return albumDao.getTotalNum();
    }

    /**
     * {@inheritDoc}
     *
     * @return first albums page resource.
     */
    @Override
    public ResourceDto<AlbumDto> getAlbumsPage() {
        return getAlbumsPage(FIRST_PAGE_NUM);
    }

    /**
     * {@inheritDoc}
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<AlbumDto> getAlbumsPage(String order, List<String> orderFields) {
        return getAlbumsPage(ALBUMS_PER_PAGE_DEFAULT, FIRST_PAGE_NUM, order, orderFields);
    }

    /**
     * {@inheritDoc}
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<AlbumDto> getAlbumsPage(Long page) {
        return getAlbumsPage(ALBUMS_PER_PAGE_DEFAULT, page, null, null);
    }

    /**
     * {@inheritDoc}
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value {@link AlbumServiceImpl#ALBUMS_PER_PAGE_DEFAULT} will be used.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *                    first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<AlbumDto> getAlbumsPage(Long perPage, Long page, String order, List<String> orderFields) {

        List<SortOption> sortOptions = (order != null && orderFields != null)
                ? Collections.singletonList(new SortOption(order, orderFields))
                : Collections.emptyList();

        return getAlbumsPage(perPage, page, sortOptions);
    }

    /**
     * {@inheritDoc}
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *                    first page will be returned.
     * @param sortOptions sortOptions specifies albums ordering.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<AlbumDto> getAlbumsPage(Long perPage, Long page, List<SortOption> sortOptions) {

        if (page == null) {
            page = FIRST_PAGE_NUM;
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        if (perPage == null) {
            perPage = ALBUMS_PER_PAGE_DEFAULT;
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per page value must be greater than zero");
        }

        ResourceDto<AlbumDto> albumsPage = new ResourceDto<>();
        albumsPage.setPagination(getPaginationInfo(page, perPage));
        long offset = (page - 1) * perPage;

        List<Album> albums = albumDao.getList(offset, perPage, sortOptions, ALBUM_SHORT_INFO_FIELDS);
        List<AlbumDto> albumDtoList = albums.stream()
                .map(this::albumToDto)
                .collect(toList());

        albumsPage.setResults(albumDtoList);

        return albumsPage;
    }

    /**
     * {@inheritDoc}
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param sortOptions specifies albums ordering.
     * @param lang        language code.
     * @return albums page resource.
     */
    @Override
    public ResourceDto<AlbumDto> getAlbumsPageByLanguage(Long perPage, Long page, List<SortOption> sortOptions, String lang) {

        if (page == null) {
            page = FIRST_PAGE_NUM;
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        if (perPage == null) {
            perPage = ALBUMS_PER_PAGE_DEFAULT;
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per page value must be greater than zero");
        }

        ResourceDto<AlbumDto> albumsPage = new ResourceDto<>();
        albumsPage.setPagination(getPaginationInfo(page, perPage, albumDao.getTotalNumByLanguage(lang)));
        long offset = (page - 1) * perPage;

        List<Album> albums = albumDao.getByLanguage(offset, perPage, sortOptions, lang, ALBUM_SHORT_INFO_FIELDS);
        List<AlbumDto> albumDtoList = albums.stream()
                .map(this::albumToDto)
                .collect(toList());

        albumsPage.setResults(albumDtoList);

        return albumsPage;
    }

    /**
     * {@inheritDoc}
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
    @Override
    public AlbumDto getAlbumById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        Album album = albumDao.getById(id);
        if (album == null) {
            throw new ResourceNotFoundException("Album with id '" + id + "' not found");
        }

        return albumToDto(album);
    }

    /**
     * {@inheritDoc}
     *
     * @param slugName slug representation of album's name which is used to generate readable and SEO-friendly URLs.
     * @return album with specified slug name.
     */
    @Override
    public AlbumDto getAlbumBySlugName(String slugName) {

        if (slugName == null || slugName.isEmpty()) {
            throw new ValidationException("Album's slug name can not be empty");
        }

        Album album = slugService.getAlbumBySlug(slugName);
        if (album == null) {
            throw new ResourceNotFoundException("Album with slug name '" + slugName + "' not found");
        }

        return albumToDto(album);
    }

    /**
     * {@inheritDoc}
     *
     * @param id identifier of album which will be deleted.
     */
    @Override
    public void deleteAlbumById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        Album album = albumDao.getById(id);
        if (album == null) {
            throw new ResourceNotFoundException("Album with id '" + id + "' not found");
        }

        // Remove album from Artists' list of albums
        List<Artist> artistList = album.getArtistList();
        if (artistList != null) {
            artistList.stream()
                    .map(Artist::getId)
                    .filter(Objects::nonNull)
                    .map(artistDao::getById) // Map from artist short info to actual artist
                    .filter(artist -> artist.getAlbumsIds() != null)
                    .peek(artist -> artist.getAlbumsIds().remove(id))
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        albumDao.deleteById(id);
    }

    /**
     * {@inheritDoc}
     *
     * @param album contains album info.
     * @return created album.
     */
    @Override
    public AlbumDto createAlbum(Album album) {

        if (album == null) {
            throw new ValidationException("Album can not be null");
        }

        if (album.getName() == null) {
            throw new ValidationException("Album's name can not be null");
        }

        slugService.setSlugForAlbum(album);
        Album createdAlbum = albumDao.create(album);

        if (album.getArtistList() != null) {
            album.getArtistList().stream()
                    .filter(artist -> artist.getId() != null)
                    .map(artist -> artistDao.getById(artist.getId())) // fetch actual Artist
                    .peek(artist -> artist.addAlbumId(createdAlbum.getId()))
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        return albumToDto(createdAlbum);
    }


    /**
     * {@inheritDoc}
     *
     * @param album album which will be updated. Note, that album's id must be set, otherwise
     *              {@link IllegalArgumentException} will be thrown.
     * @return updated album.
     */
    @Override
    public AlbumDto updateAlbum(Album album) {
        return updateAlbum(album.getId(), album);
    }

    /**
     * {@inheritDoc}
     *
     * @param id    identifier of album which will be updated.
     * @param album album which will be updated.
     * @return updated album.
     */
    @Override
    @SuppressWarnings("unchecked")
    public AlbumDto updateAlbum(String id, Album album) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (album == null) {
            throw new IllegalArgumentException("Album can not be null");
        }

        Album existingAlbum = albumDao.getById(id, "artists");
        if (existingAlbum == null) {
            throw new ResourceNotFoundException("Album with id '" + id + "' not found");
        }

        List<String> albumArtistsIds = (album.getArtistList() == null)
                ? null
                : album.getArtistList().stream()
                .peek(artist -> {
                    if (artist.getId() == null) {
                        throw new ValidationException("Album's artist must have 'id' field set",
                                "Artist's id can not be empty");
                    }
                })
                .peek(artist -> {

                    Artist storedArtist = artistDao.getById(artist.getId());
                    if (storedArtist == null) {
                        throw new ResourceNotFoundException("Artist with id ='" + artist.getId() + "' not found");
                    }

                    artist.setSlugName(storedArtist.getSlugName());
                    artist.setSlugPostfix(storedArtist.getSlugPostfix());
                })
                .map(Artist::getId)
                .collect(toList());

        List<String> existingAlbumArtistsIds = (existingAlbum.getArtistList() == null)
                ? null
                : existingAlbum.getArtistList().stream()
                .map(Artist::getId)
                .collect(toList());


        List<String> addedArtistsIds = null;
        List<String> removedArtistsIds = null;
        if (albumArtistsIds == null || albumArtistsIds.isEmpty()) {
            removedArtistsIds = existingAlbumArtistsIds;
        } else if (existingAlbumArtistsIds == null || existingAlbumArtistsIds.isEmpty()) {
            addedArtistsIds = albumArtistsIds;
        } else {

            addedArtistsIds = new ArrayList<>(albumArtistsIds);
            addedArtistsIds.removeAll(existingAlbumArtistsIds);

            removedArtistsIds = new ArrayList<>(existingAlbumArtistsIds);
            removedArtistsIds.removeAll(albumArtistsIds);
        }

        if (addedArtistsIds != null && !addedArtistsIds.isEmpty()) {

            addedArtistsIds.stream()
                    .map(artistDao::getById)
                    .filter(Objects::nonNull)
                    .peek(artist -> artist.addAlbumId(id))
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        if (removedArtistsIds != null && !removedArtistsIds.isEmpty()) {

            removedArtistsIds.stream()
                    .map(artistDao::getById)
                    .filter(Objects::nonNull)
                    .filter(artist -> artist.getAlbumsIds() != null)
                    .peek(artist -> artist.getAlbumsIds().remove(id))
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        return albumToDto(albumDao.update(id, album));
    }

    /**
     * {@inheritDoc}
     *
     * @param id      identifier of album, which is associated with track.
     * @param trackId track identifier.
     * @return single track according to the specified track identifier and album identifier.
     */
    @Override
    public TrackDto getTrackById(String id, String trackId) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (trackId == null || trackId.isEmpty()) {
            throw new IllegalArgumentException("Track's identifier can not be empty");
        }

        return trackToDto(albumDao.getTrackById(id, trackId));
    }

    /**
     * {@inheritDoc}
     *
     * @param id identifier of album, whose track list will be returned.
     * @return list of tracks for the album with specified identifier.
     */
    @Override
    public List<TrackDto> getAlbumTracksList(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        return albumDao.getTracksList(id).stream().map(this::trackToDto).collect(toList());
    }

    /**
     * {@inheritDoc}
     *
     * @param id    identifier of album, for which track will be added.
     * @param track track, which will be added to the album's track list.
     * @return newly created track with id field set.
     */
    @Override
    public TrackDto addTrackToAlbumTrackList(String id, TrackDto track) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (track == null) {
            throw new IllegalArgumentException("Track can not be null");
        }

        return trackToDto(albumDao.addTrack(id, dtoToTrack(track)));
    }

    /**
     * {@inheritDoc}
     *
     * @param id     identifier of album, for which tracks will be added.
     * @param tracks list of tracks, which will be added to the album's track list.
     * @return list of newly created tracks, each of tracks has id field set.
     */
    @Override
    public List<TrackDto> addTracksToAlbumTrackList(String id, List<TrackDto> tracks) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (tracks == null) {
            throw new IllegalArgumentException("Track list can not be null");
        }

        List<Track> trackList = tracks.stream().map(this::dtoToTrack).collect(toList());

        return albumDao.addTracks(id, trackList).stream().map(this::trackToDto).collect(toList());
    }

    /**
     * {@inheritDoc}
     *
     * @param id      identifier of album, for which track will be updated.
     * @param trackId identifier of track, which will be updated.
     * @param track   contains update information.
     * @return updated track.
     */
    @Override
    public TrackDto updateAlbumTrack(String id, String trackId, TrackDto track) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (trackId == null || trackId.isEmpty()) {
            throw new IllegalArgumentException("Track's identifier can not be empty");
        }

        if (track == null) {
            throw new IllegalArgumentException("Track can not be null");
        }

        return trackToDto(albumDao.updateTrack(id, trackId, dtoToTrack(track)));
    }

    /**
     * {@inheritDoc}
     *
     * @param id        identifier of album, for which track list will be set.
     * @param trackList list of tracks. Some of them may correspond to existing tracks, that will be updated.
     * @return album's track list.
     */
    @Override
    public List<TrackDto> setAlbumTrackList(String id, List<TrackDto> trackList) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (trackList == null) {
            throw new IllegalArgumentException("Track list can not be null");
        }

        List<Track> tracks = trackList.stream().map(this::dtoToTrack).collect(toList());

        return albumDao.setTrackList(id, tracks).stream().map(this::trackToDto).collect(toList());
    }

    /**
     * {@inheritDoc}
     *
     * @param id      identifier of album, for which track will be deleted.
     * @param trackId identifier of track, which will be deleted.
     */
    @Override
    public void deleteAlbumTrack(String id, String trackId) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (trackId == null || trackId.isEmpty()) {
            throw new IllegalArgumentException("Track's identifier can not be empty");
        }

        if (!albumDao.deleteTrack(id, trackId)) {
            throw new ResourceNotFoundException("Can not find track with id = " + trackId + "' for album with id = " + id);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return list of supported albums languages.
     */
    @Override
    public List<Language> getSupportedAlbumsLanguages() {
        return languageDao.getList();
    }

    /**
     * {@inheritDoc}
     *
     * @param nameEntry specifies search criteria.
     * @param limit     specifies number of albums, which will be returned. Can be overridden by actual service
     *                  implementation.
     * @return list of albums which titles start with name entry.
     */
    @Override
    public List<AlbumDto> searchAlbums(String nameEntry, Long limit) {

        long actualLimit = (limit != null && limit > 0 && limit < MAX_SEARCH_LIMIT) ? limit : MAX_SEARCH_LIMIT;
        List<Album> albums = albumDao.getByNameStartsWith(nameEntry, actualLimit, ALBUM_SHORT_INFO_FIELDS);

        return albums.stream()
                .map(this::albumToDto)
                .collect(Collectors.toList());
    }

    /**
     * FIXME
     * {@inheritDoc}
     *
     * @param albumId identifier of album, for which recommendations will be returned.
     * @param limit   specifies number of albums, which will be returned. Can be overridden by actual service
     *                implementation.
     * @return list of recommended albums.
     */
    @Override
    public List<AlbumDto> getRecommendedById(String albumId, Long limit) {

        long actualLimit = (limit != null && limit > 0 && limit < MAX_RECOMMENDED_LIMIT) ? limit : MAX_RECOMMENDED_LIMIT;
        int maxOffset = (int) (albumDao.getTotalNum() - actualLimit);
        int offset = new Random().nextInt(maxOffset);

        List<Album> albums = albumDao.getList(offset, actualLimit, ALBUM_SHORT_INFO_FIELDS);

        return albums.stream()
                .map(this::albumToDto)
                .collect(Collectors.toList());
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

        if (album.getTrackList() != null && !album.getTrackList().isEmpty()) {

            List<TrackDto> trackDtoList = album.getTrackList().stream()
                    .map(this::trackToDto)
                    .collect(toList());

            albumDto.setTrackList(trackDtoList);
        }

        if (album.getArtistList() != null && !album.getArtistList().isEmpty()) {

            List<ArtistDto> artistDtoList = album.getArtistList().stream()
                    .map(this::artistToDto)
                    .collect(toList());

            albumDto.setArtistList(artistDtoList);
        }

        return albumDto;
    }

    private TrackDto trackToDto(Track track) {

        TrackDto trackDto = new TrackDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(trackDto, track);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create track Data Transfer Object", e);
        }

        return trackDto;
    }

    private Track dtoToTrack(TrackDto trackDto) {

        Track track = new Track();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(track, trackDto);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create track from Data Transfer Object", e);
        }

        return track;
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

}
