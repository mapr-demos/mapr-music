package com.mapr.music.service.impl;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.model.Recommendation;
import com.mapr.music.service.RecommendationService;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class RecommendationServiceImpl implements RecommendationService {

    private static final int MAX_LIMIT = 10;
    private static final int DEFAULT_LIMIT = 5;

    private static final String ANONYMOUS_USER_ID = "anonymous";

    /**
     * Array of album's fields that will be used for projection.
     */
    private static final String[] ALBUM_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "slug_name",
            "slug_postfix",
            "cover_image_url",
            "artists"
    };

    /**
     * Array of album's fields that will be used for projection.
     */
    private static final String[] ARTIST_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "slug_name",
            "slug_postfix",
            "profile_image_url"
    };

    private final AlbumDao albumDao;
    private final ArtistDao artistDao;
    private final SlugService slugService;
    private final MaprDbDao<Recommendation> recommendationDao;

    @Inject
    public RecommendationServiceImpl(SlugService slugService,
                                     @Named("albumDao") AlbumDao albumDao,
                                     @Named("artistDao") ArtistDao artistDao,
                                     @Named("recommendationDao") MaprDbDao<Recommendation> recommendationDao) {

        this.albumDao = albumDao;
        this.artistDao = artistDao;
        this.slugService = slugService;
        this.recommendationDao = recommendationDao;
    }

    @Override
    public List<ArtistDto> getRecommendedArtists(String id, Principal user) {
        return getRecommendedArtists(id, user, DEFAULT_LIMIT);
    }

    @Override
    public List<ArtistDto> getRecommendedArtists(String id, Principal user, Integer limit) {

        int actualLimit = (limit == null || limit < 0) ? DEFAULT_LIMIT : (limit > MAX_LIMIT) ? MAX_LIMIT : limit;

        // FIXME determine user id according to the User Principal.
        return recommendationDao.getById(ANONYMOUS_USER_ID).getRecommendedArtistsIds().stream()
                .filter(Objects::nonNull)
                .filter(recommendedId -> !recommendedId.equals(id))
                .map(artistId -> artistDao.getById(artistId, ARTIST_SHORT_INFO_FIELDS))
                .map(this::artistToDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .limit(actualLimit)
                .collect(Collectors.toList());
    }

    @Override
    public List<AlbumDto> getRecommendedAlbums(String id, Principal user) {
        return getRecommendedAlbums(id, user, DEFAULT_LIMIT);
    }


    @Override
    public List<AlbumDto> getRecommendedAlbums(String id, Principal user, Integer limit) {

        int actualLimit = (limit == null || limit < 0) ? DEFAULT_LIMIT : (limit > MAX_LIMIT) ? MAX_LIMIT : limit;

        // FIXME determine user id according to the User Principal.
        return recommendationDao.getById(ANONYMOUS_USER_ID).getRecommendedAlbumsIds().stream()
                .filter(Objects::nonNull)
                .filter(recommendedId -> !recommendedId.equals(id))
                .map(albumId -> albumDao.getById(albumId, ALBUM_SHORT_INFO_FIELDS))
                .map(this::albumToDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .limit(actualLimit)
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

        if (album.getArtists() != null && !album.getArtists().isEmpty()) {

            List<ArtistDto> artistDtoList = album.getArtists().stream()
                    .map(this::artistShortInfoToDto)
                    .collect(toList());

            albumDto.setArtistList(artistDtoList);
        }

        return albumDto;
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
