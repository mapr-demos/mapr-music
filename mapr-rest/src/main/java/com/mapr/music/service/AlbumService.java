package com.mapr.music.service;

import com.mapr.music.dao.*;
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
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Responsible of performing all business on {@link Album} model.
 */
public class AlbumService implements PaginatedService {

    private static final long ALBUMS_PER_PAGE_DEFAULT = 50;
    private static final long FIRST_PAGE_NUM = 1;
    private static final long MAX_SEARCH_LIMIT = 15;

    /**
     * Array of album's fields that will be used for projection.
     */
    private static final String[] ALBUM_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "slug_name",
            "slug_postfix",
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
    private final StatisticService statisticService;
    private final AlbumRateDao albumRateDao;

    @Inject
    public AlbumService(@Named("albumDao") AlbumDao albumDao, @Named("artistDao") MaprDbDao<Artist> artistDao,
                        LanguageDao languageDao, SlugService slugService, StatisticService statisticService,
                        AlbumRateDao albumRateDao) {

        this.albumDao = albumDao;
        this.artistDao = artistDao;
        this.languageDao = languageDao;
        this.slugService = slugService;
        this.statisticService = statisticService;
        this.albumRateDao = albumRateDao;
    }

    /**
     * {@inheritDoc}
     *
     * @return total number of album documents.
     */
    @Override
    public long getTotalNum() {
        return statisticService.getTotalAlbums();
    }

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class.
     *
     * @return first albums page resource.
     */
    public ResourceDto<AlbumDto> getAlbumsPage() {
        return getAlbumsPage(FIRST_PAGE_NUM);
    }

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class. Albums will be ordered according to the specified order and fields.
     *
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    public ResourceDto<AlbumDto> getAlbumsPage(String order, List<String> orderFields) {
        return getAlbumsPage(ALBUMS_PER_PAGE_DEFAULT, FIRST_PAGE_NUM, order, orderFields);
    }

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class.
     *
     * @param page specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *             first page will be returned.
     * @return albums page resource.
     */
    public ResourceDto<AlbumDto> getAlbumsPage(Long page) {
        return getAlbumsPage(ALBUMS_PER_PAGE_DEFAULT, page, null, null);
    }

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class. Albums will be ordered according to the specified order and fields.
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code> the
     *                    first page will be returned.
     * @param order       string representation of the order. Valid values are: "asc", "ASC", "desc", "DESC".
     * @param orderFields fields by which ordering will be performed.
     * @return albums page resource.
     */
    public ResourceDto<AlbumDto> getAlbumsPage(Long perPage, Long page, String order, List<String> orderFields) {

        List<SortOption> sortOptions = (order != null && orderFields != null)
                ? Collections.singletonList(new SortOption(order, orderFields))
                : Collections.emptyList();

        return getAlbumsPage(perPage, page, sortOptions);
    }

    /**
     * Returns list of albums which is represented by page with default number of albums. Default number of albums
     * depends on implementation class. Albums will be ordered according to the specified list of sort options.
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param sortOptions specifies albums ordering.
     * @return albums page resource.
     */
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
     * Returns list of albums by language code. List of albums is represented by page with default number of albums.
     * Default number of albums depends on implementation class. Albums will be ordered according to the specified list
     * of sort options.
     *
     * @param perPage     specifies number of albums per page. In case when value is <code>null</code> the
     *                    default value will be used. Default value depends on implementation class.
     * @param page        specifies number of page, which will be returned. In case when page value is <code>null</code>
     *                    the first page will be returned.
     * @param sortOptions specifies albums ordering.
     * @param lang        language code.
     * @return albums page resource.
     */
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
     * Returns single album according to it's identifier.
     *
     * @param id album's identifier.
     * @return album with the specified identifier.
     */
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
     * Returns single album by it's slug name.
     *
     * @param slugName slug representation of album's name which is used to generate readable and SEO-friendly URLs.
     * @return album with specified slug name.
     */
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
     * Deletes single album by it's identifier.
     *
     * @param id identifier of album which will be deleted.
     */
    public void deleteAlbumById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        Album album = albumDao.getById(id);
        if (album == null) {
            throw new ResourceNotFoundException("Album with id '" + id + "' not found");
        }

        // Remove Album's rates
        albumRateDao.getByAlbumId(id).forEach(albumRate -> albumRateDao.deleteById(albumRate.getId()));

        // Remove album from Artists' list of albums
        List<Artist.ShortInfo> artistList = album.getArtists();
        if (artistList != null) {
            artistList.stream()
                    .map(Artist.ShortInfo::getId)
                    .filter(Objects::nonNull)
                    .map(artistDao::getById) // Map from artist short info to actual artist
                    .filter(artist -> artist.getAlbums() != null)
                    .peek(artist -> {
                        List<Album.ShortInfo> toDelete = artist.getAlbums().stream()
                                .filter(Objects::nonNull)
                                .filter(a -> id.equals(a.getId()))
                                .collect(toList());

                        artist.getAlbums().removeAll(toDelete);
                    })
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        albumDao.deleteById(id);
    }

    /**
     * Creates album according to the specified instance of {@link AlbumDto} class.
     *
     * @param albumDto contains album info.
     * @return created album.
     */
    public AlbumDto createAlbum(AlbumDto albumDto) {

        if (albumDto == null) {
            throw new ValidationException("Album can not be null");
        }

        if (albumDto.getName() == null) {
            throw new ValidationException("Album's name can not be null");
        }

        Album album = dtoToAlbum(albumDto);


        if (album.getArtists() != null) {
            List<Artist.ShortInfo> actualArtistsInfo = album.getArtists().stream()
                    .filter(Objects::nonNull)
                    .filter(artist -> artist.getId() != null)
                    .map(artist -> artistDao.getById(artist.getId())) // fetch actual Artist
                    .filter(Objects::nonNull)
                    .map(Artist::getShortInfo)
                    .collect(toList());

            album.setArtists(actualArtistsInfo);
        }

        slugService.setSlugForAlbum(album);
        Album createdAlbum = albumDao.create(album);

        if (album.getArtists() != null) {
            album.getArtists().stream()
                    .filter(Objects::nonNull)
                    .map(Artist.ShortInfo::getId)
                    .map(artistDao::getById)
                    .peek(artist -> artist.addAlbum(createdAlbum.getShortInfo()))
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        return albumToDto(createdAlbum);
    }

    /**
     * Updates single album according to the specified instance of {@link AlbumDto} class.
     *
     * @param albumDto album which will be updated. Note, that album's id must be set, otherwise
     *                 {@link IllegalArgumentException} will be thrown.
     * @return updated album.
     * @throws IllegalArgumentException in case when specified album is <code>null</code> or it does not contain id.
     */
    public AlbumDto updateAlbum(AlbumDto albumDto) {
        return updateAlbum(albumDto.getId(), albumDto);
    }

    /**
     * Updates single album according to the specified instance of {@link AlbumDto} class.
     *
     * @param id       identifier of album which will be updated.
     * @param albumDto album which will be updated.
     * @return updated album.
     */
    @SuppressWarnings("unchecked")
    public AlbumDto updateAlbum(String id, AlbumDto albumDto) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        Album album = dtoToAlbum(albumDto);
        if (album == null) {
            throw new IllegalArgumentException("Album can not be null");
        }

        Album existingAlbum = albumDao.getById(id);
        if (existingAlbum == null) {
            throw new ResourceNotFoundException("Album with id '" + id + "' not found");
        }

        List<String> albumArtistsIds = (album.getArtists() == null)
                ? null
                : album.getArtists().stream()
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

                    artist.setSlug(storedArtist.getShortInfo().getSlug());
                })
                .map(Artist.ShortInfo::getId)
                .collect(toList());

        List<String> existingAlbumArtistsIds = (existingAlbum.getArtists() == null)
                ? null
                : existingAlbum.getArtists().stream()
                .map(Artist.ShortInfo::getId)
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
                    .peek(artist -> artist.addAlbum(existingAlbum.getShortInfo()))
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        if (removedArtistsIds != null && !removedArtistsIds.isEmpty()) {

            removedArtistsIds.stream()
                    .map(artistDao::getById)
                    .filter(Objects::nonNull)
                    .filter(artist -> artist.getAlbums() != null)
                    .peek(artist -> {
                        List<Album.ShortInfo> toDelete = artist.getAlbums().stream()
                                .filter(a -> id.equals(a.getId()))
                                .collect(toList());

                        artist.getAlbums().removeAll(toDelete);
                    })
                    .forEach(artist -> artistDao.update(artist.getId(), artist));
        }

        return albumToDto(albumDao.update(id, album));
    }

    /**
     * Returns single track according to the specified track identifier and album identifier.
     *
     * @param id      identifier of album, which is associated with track.
     * @param trackId track identifier.
     * @return single track according to the specified track identifier and album identifier.
     */
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
     * Returns list of tracks for the album with specified identifier.
     *
     * @param id identifier of album, whose track list will be returned.
     * @return list of tracks for the album with specified identifier.
     */
    public List<TrackDto> getAlbumTracksList(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        return albumDao.getTracksList(id).stream().map(this::trackToDto).collect(toList());
    }

    /**
     * Adds single track to the album with specified identifier.
     *
     * @param id    identifier of album, for which track will be added.
     * @param track track, which will be added to the album's track list.
     * @return newly created track with id field set.
     */
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
     * Adds list of tracks to the album with specified identifier.
     *
     * @param id     identifier of album, for which tracks will be added.
     * @param tracks list of tracks, which will be added to the album's track list.
     * @return list of newly created tracks, each of tracks has id field set.
     */
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
     * Updates single track.
     *
     * @param id      identifier of album, for which track will be updated.
     * @param trackId identifier of track, which will be updated.
     * @param track   contains update information.
     * @return updated track.
     */
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
     * Sets track list for the album with specified identifier. Note, that in case when track's identifier corresponds
     * to the existing track, track will be updated, otherwise new track will be created.
     *
     * @param id        identifier of album, for which track list will be set.
     * @param trackList list of tracks. Some of them may correspond to existing tracks, that will be updated.
     * @return album's track list.
     */
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
     * Deletes single track according to the specified album identifier and track identifier.
     *
     * @param id      identifier of album, for which track will be deleted.
     * @param trackId identifier of track, which will be deleted.
     */
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
     * Returns list of supported albums languages.
     *
     * @return list of supported albums languages.
     */
    public List<Language> getSupportedAlbumsLanguages() {
        return languageDao.getList();
    }

    /**
     * Search albums according to the specified name entry. Returns albums which titles start with name entry.
     *
     * @param nameEntry specifies search criteria.
     * @param limit     specifies number of albums, which will be returned. Can be overridden by actual service
     *                  implementation.
     * @return list of albums which titles start with name entry.
     */
    public List<AlbumDto> searchAlbums(String nameEntry, Long limit) {

        long actualLimit = (limit != null && limit > 0 && limit < MAX_SEARCH_LIMIT) ? limit : MAX_SEARCH_LIMIT;
        List<Album> albums = albumDao.getByNameStartsWith(nameEntry, actualLimit, ALBUM_SHORT_INFO_FIELDS);

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

        if (album.getArtists() != null && !album.getArtists().isEmpty()) {

            List<ArtistDto> artistDtoList = album.getArtists().stream()
                    .map(this::artistShortInfoToDto)
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

    private Album dtoToAlbum(AlbumDto albumDto) {

        Album album = new Album();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(album, albumDto);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create album Data Transfer Object", e);
        }

        if (albumDto.getTrackList() != null && !albumDto.getTrackList().isEmpty()) {

            List<Track> trackList = albumDto.getTrackList().stream()
                    .map(this::dtoToTrack)
                    .collect(toList());

            album.setTrackList(trackList);
        }

        if (albumDto.getArtistList() != null && !albumDto.getArtistList().isEmpty()) {

            List<Artist.ShortInfo> artistShortInfoList = albumDto.getArtistList().stream()
                    .map(this::artistDtoToShortInfo)
                    .collect(toList());

            album.setArtists(artistShortInfoList);
        }

        return album;
    }

    private ArtistDto artistShortInfoToDto(Artist.ShortInfo artistShortInfo) {

        ArtistDto artistDto = new ArtistDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(artistDto, artistShortInfo);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create artist Data Transfer Object", e);
        }

        return artistDto;
    }

    private Artist.ShortInfo artistDtoToShortInfo(ArtistDto artistDto) {

        Artist.ShortInfo artistShortInfo = new Artist.ShortInfo();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(artistShortInfo, artistDto);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create album Data Transfer Object", e);
        }

        return artistShortInfo;
    }

}
