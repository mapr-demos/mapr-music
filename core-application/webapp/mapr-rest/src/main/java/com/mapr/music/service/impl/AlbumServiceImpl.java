package com.mapr.music.service.impl;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.LanguageDao;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Language;
import com.mapr.music.model.Track;
import com.mapr.music.service.AlbumService;
import com.mapr.music.service.PaginatedService;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.NotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Actual implementation of {@link AlbumService} which is responsible of performing all business logic.
 */
public class AlbumServiceImpl implements AlbumService, PaginatedService {

    private static final long ALBUMS_PER_PAGE_DEFAULT = 50;
    private static final long FIRST_PAGE_NUM = 1;

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
            "artist_list"
    };


    private final AlbumDao albumDao;
    private final LanguageDao languageDao;
    private final SlugService slugService;

    @Inject
    public AlbumServiceImpl(@Named("albumDao") AlbumDao albumDao, LanguageDao languageDao, SlugService slugService) {
        this.albumDao = albumDao;
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
                .collect(Collectors.toList());

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
                .collect(Collectors.toList());

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
            throw new NotFoundException("Album with id '" + id + "' not found");
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
            throw new IllegalArgumentException("Album's slug name can not be empty");
        }

        Album album = slugService.getAlbumBySlug(slugName);
        if (album == null) {
            throw new NotFoundException("Album with slug name '" + slugName + "' not found");
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

        if (!albumDao.exists(id)) {
            throw new NotFoundException("Album with id '" + id + "' not found");
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
            throw new IllegalArgumentException("Album can not be null");
        }

        slugService.setSlugForAlbum(album);

        return albumToDto(albumDao.create(album));
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
    public AlbumDto updateAlbum(String id, Album album) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (album == null) {
            throw new IllegalArgumentException("Album can not be null");
        }

        if (!albumDao.exists(id)) {
            throw new NotFoundException("Album with id '" + id + "' not found");
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
    public Track getTrackById(String id, String trackId) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (trackId == null || trackId.isEmpty()) {
            throw new IllegalArgumentException("Track's identifier can not be empty");
        }

        return albumDao.getTrackById(id, trackId);
    }

    /**
     * {@inheritDoc}
     *
     * @param id identifier of album, whose track list will be returned.
     * @return list of tracks for the album with specified identifier.
     */
    @Override
    public List<Track> getAlbumTracksList(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        return albumDao.getTracksList(id);
    }

    /**
     * {@inheritDoc}
     *
     * @param id    identifier of album, for which track will be added.
     * @param track track, which will be added to the album's track list.
     * @return newly created track with id field set.
     */
    @Override
    public Track addTrackToAlbumTrackList(String id, Track track) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (track == null) {
            throw new IllegalArgumentException("Track can not be null");
        }

        return albumDao.addTrack(id, track);
    }

    /**
     * {@inheritDoc}
     *
     * @param id     identifier of album, for which tracks will be added.
     * @param tracks list of tracks, which will be added to the album's track list.
     * @return list of newly created tracks, each of tracks has id field set.
     */
    @Override
    public List<Track> addTracksToAlbumTrackList(String id, List<Track> tracks) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (tracks == null) {
            throw new IllegalArgumentException("Track list can not be null");
        }

        return albumDao.addTracks(id, tracks);
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
    public Track updateAlbumTrack(String id, String trackId, Track track) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (trackId == null || trackId.isEmpty()) {
            throw new IllegalArgumentException("Track's identifier can not be empty");
        }

        if (track == null) {
            throw new IllegalArgumentException("Track can not be null");
        }

        return albumDao.updateTrack(id, trackId, track);
    }

    /**
     * {@inheritDoc}
     *
     * @param id        identifier of album, for which track list will be set.
     * @param trackList list of tracks. Some of them may correspond to existing tracks, that will be updated.
     * @return album's track list.
     */
    @Override
    public List<Track> setAlbumTrackList(String id, List<Track> trackList) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Album's identifier can not be empty");
        }

        if (trackList == null) {
            throw new IllegalArgumentException("Track list can not be null");
        }

        return albumDao.setTrackList(id, trackList);
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
            throw new NotFoundException("Can not find track with id = " + trackId + "' for album with id = " + id);
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

}