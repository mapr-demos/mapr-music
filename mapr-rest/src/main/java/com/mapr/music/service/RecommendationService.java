package com.mapr.music.service;

import com.mapr.music.dao.AlbumDao;
import com.mapr.music.dao.ArtistDao;
import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.model.Recommendation;
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

public class RecommendationService {

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
    public RecommendationService(SlugService slugService,
                                 @Named("albumDao") AlbumDao albumDao,
                                 @Named("artistDao") ArtistDao artistDao,
                                 @Named("recommendationDao") MaprDbDao<Recommendation> recommendationDao) {

        this.albumDao = albumDao;
        this.artistDao = artistDao;
        this.slugService = slugService;
        this.recommendationDao = recommendationDao;
    }

    /**
     * Returns list of recommended Artists for the specified User and Artist.
     *
     * @param id   artist's identifier, for which recommendations will be returned.
     * @param user user's principal, for which recommendations will be returned.
     * @return list of recommended Artists for the specified User and Artist.
     */
    public List<ArtistDto> getRecommendedArtists(String id, Principal user) {
        return getRecommendedArtists(id, user, DEFAULT_LIMIT);
    }

    /**
     * Returns list of recommended Artists for the specified User and Artist.
     *
     * @param id    artist's identifier, for which recommendations will be returned.
     * @param user  user's principal, for which recommendations will be returned.
     * @param limit specifies number of recommended artists.
     * @return list of recommended Artists for the specified User and Artist.
     */
    public List<ArtistDto> getRecommendedArtists(String id, Principal user, Integer limit) {

        int actualLimit = (limit == null || limit < 0) ? DEFAULT_LIMIT : (limit > MAX_LIMIT) ? MAX_LIMIT : limit;

        return getRecommendationForUser(user).getRecommendedArtistsIds().stream()
                .filter(Objects::nonNull)
                .filter(recommendedId -> !recommendedId.equals(id))
                .map(artistId -> artistDao.getById(artistId, ARTIST_SHORT_INFO_FIELDS))
                .filter(Objects::nonNull)
                .map(this::artistToDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .limit(actualLimit)
                .collect(Collectors.toList());
    }

    /**
     * Returns list of recommended Albums for the specified User and Album.
     *
     * @param id   album's identifier, for which recommendations will be returned.
     * @param user user's principal, for which recommendations will be returned.
     * @return list of recommended Albums for the specified User and Album.
     */
    public List<AlbumDto> getRecommendedAlbums(String id, Principal user) {
        return getRecommendedAlbums(id, user, DEFAULT_LIMIT);
    }


    /**
     * Returns list of recommended Albums for the specified User and Album.
     *
     * @param id    album's identifier, for which recommendations will be returned.
     * @param user  user's principal, for which recommendations will be returned.
     * @param limit specifies number of recommended albums.
     * @return list of recommended Albums for the specified User and Album.
     */
    public List<AlbumDto> getRecommendedAlbums(String id, Principal user, Integer limit) {

        int actualLimit = (limit == null || limit < 0) ? DEFAULT_LIMIT : (limit > MAX_LIMIT) ? MAX_LIMIT : limit;

        return getRecommendationForUser(user).getRecommendedAlbumsIds().stream()
                .filter(Objects::nonNull)
                .filter(recommendedId -> !recommendedId.equals(id))
                .map(albumId -> albumDao.getById(albumId, ALBUM_SHORT_INFO_FIELDS))
                .filter(Objects::nonNull)
                .map(this::albumToDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), collected -> {
                    Collections.shuffle(collected);
                    return collected.stream();
                }))
                .limit(actualLimit)
                .collect(Collectors.toList());
    }

    private Recommendation getRecommendationForUser(Principal user) {

        // There is can be such situation when user has no rates and thus has no recommendation
        String userId = (user == null) ? ANONYMOUS_USER_ID : user.getName();
        Recommendation userRecommendation = recommendationDao.getById(userId);

        return (userRecommendation != null) ? userRecommendation : recommendationDao.getById(ANONYMOUS_USER_ID);
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
