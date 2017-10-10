package com.mapr.music.service.impl;

import com.mapr.music.dao.*;
import com.mapr.music.dto.RateDto;
import com.mapr.music.exception.ResourceNotFoundException;
import com.mapr.music.model.*;
import com.mapr.music.service.RateService;
import org.apache.commons.beanutils.PropertyUtilsBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

public class RateServiceImpl implements RateService {

    private final AlbumRateDao albumRateDao;
    private final ArtistRateDao artistRateDao;
    private final AlbumDao albumDao;
    private final ArtistDao artistDao;
    private final MaprDbDao<User> userDao;

    @Inject
    public RateServiceImpl(AlbumRateDao albumRateDao,
                           ArtistRateDao artistRateDao,
                           @Named("albumDao") AlbumDao albumDao,
                           @Named("artistDao") ArtistDao artistDao,
                           @Named("userDao") MaprDbDao<User> userDao) {

        this.albumRateDao = albumRateDao;
        this.artistRateDao = artistRateDao;
        this.albumDao = albumDao;
        this.artistDao = artistDao;
        this.userDao = userDao;
    }

    @Override
    public RateDto getAlbumRate(Principal user, String albumId) {

        if (user == null || user.getName() == null || user.getName().isEmpty()) {
            return new RateDto();
        }

        String userId = user.getName();
        if (!userDao.exists(userId)) {
            throw new ResourceNotFoundException("User with id '" + userId + "' not found");
        }

        if (!albumDao.exists(albumId)) {
            throw new ResourceNotFoundException("Album with id '" + albumId + "' not found");
        }

        AlbumRate rate = albumRateDao.getRate(userId, albumId);
        return (rate != null) ? albumRateToDto(rate) : new RateDto();
    }


    @Override
    public RateDto rateAlbum(String userId, String albumId, RateDto albumRate) {

        if (albumRate == null) {
            throw new IllegalArgumentException("Album rate can not be null");
        }

        return rateAlbum(userId, albumId, albumRate.getRating());
    }

    @Override
    public RateDto rateAlbum(String userId, String albumId, double rate) {

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User id can not be empty");
        }

        if (albumId == null || albumId.isEmpty()) {
            throw new IllegalArgumentException("Album id can not be empty");
        }

        if (!userDao.exists(userId)) {
            throw new ResourceNotFoundException("User with id '" + userId + "' not found");
        }

        Album existingAlbum = albumDao.getById(albumId);
        if (existingAlbum == null) {
            throw new ResourceNotFoundException("Album with id '" + albumId + "' not found");
        }

        AlbumRate possibleExistingRate = albumRateDao.getRate(userId, albumId);
        if (possibleExistingRate != null) {
            possibleExistingRate.setRating(rate);
            AlbumRate newAlbumRate = albumRateDao.update(possibleExistingRate.getId(), possibleExistingRate);

            return recomputeAlbumRate(newAlbumRate, existingAlbum);
        }

        AlbumRate albumRate = new AlbumRate();
        albumRate.setId(UUID.randomUUID().toString());
        albumRate.setUserId(userId);
        albumRate.setDocumentId(albumId);
        albumRate.setRating(rate);
        AlbumRate newAlbumRate = albumRateDao.create(albumRate);

        return recomputeAlbumRate(newAlbumRate, existingAlbum);
    }

    private RateDto recomputeAlbumRate(AlbumRate newAlbumRate, Album existingAlbum) {
        List<AlbumRate> albumRates = albumRateDao.getByAlbumId(newAlbumRate.getDocumentId());
        double aggregatedRating = albumRates.stream().mapToDouble(AlbumRate::getRating).sum() / albumRates.size();
        existingAlbum.setRating(aggregatedRating);
        albumDao.update(existingAlbum.getId(), existingAlbum);

        return new RateDto(aggregatedRating);
    }

    private RateDto recomputeArtistRate(ArtistRate newArtistRate, Artist existingArtist) {
        List<ArtistRate> artistRates = artistRateDao.getByArtistId(newArtistRate.getDocumentId());
        double aggregatedRating = artistRates.stream().mapToDouble(ArtistRate::getRating).sum() / artistRates.size();
        existingArtist.setRating(aggregatedRating);
        artistDao.update(existingArtist.getId(), existingArtist);

        return new RateDto(aggregatedRating);
    }

    @Override
    public RateDto rateAlbum(Principal user, String albumId, RateDto albumRate) {

        if (user == null) {
            throw new IllegalArgumentException("User principal can not be null");
        }

        if (albumRate == null) {
            throw new IllegalArgumentException("Album rate can not be null");
        }

        return rateAlbum(user.getName(), albumId, albumRate.getRating());
    }


    @Override
    public RateDto getArtistRate(Principal user, String artistId) {

        if (user == null || user.getName() == null || user.getName().isEmpty()) {
            return new RateDto();
        }

        String userId = user.getName();
        if (!userDao.exists(userId)) {
            throw new ResourceNotFoundException("User with id '" + userId + "' not found");
        }

        if (!artistDao.exists(artistId)) {
            throw new ResourceNotFoundException("Artist with id '" + artistId + "' not found");
        }

        ArtistRate rate = artistRateDao.getRate(userId, artistId);
        return (rate != null) ? artistRateToDto(rate) : new RateDto();
    }

    @Override
    public RateDto rateArtist(String userId, String artistId, RateDto artistRate) {

        if (artistRate == null) {
            throw new IllegalArgumentException("Artist rate can not be null");
        }

        return rateArtist(userId, artistId, artistRate.getRating());
    }

    @Override
    public RateDto rateArtist(String userId, String artistId, double rate) {

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("User id can not be empty");
        }

        if (artistId == null || artistId.isEmpty()) {
            throw new IllegalArgumentException("Artist id can not be empty");
        }

        if (!userDao.exists(userId)) {
            throw new ResourceNotFoundException("User with id '" + userId + "' not found");
        }

        Artist existingArtist = artistDao.getById(artistId);
        if (existingArtist == null) {
            throw new ResourceNotFoundException("Artist with id '" + artistId + "' not found");
        }

        ArtistRate possibleExistingRate = artistRateDao.getRate(userId, artistId);
        if (possibleExistingRate != null) {
            possibleExistingRate.setRating(rate);
            ArtistRate newArtistRate = artistRateDao.update(possibleExistingRate.getId(), possibleExistingRate);

            return recomputeArtistRate(newArtistRate, existingArtist);
        }

        ArtistRate artistRate = new ArtistRate();
        artistRate.setId(UUID.randomUUID().toString());
        artistRate.setUserId(userId);
        artistRate.setDocumentId(artistId);
        artistRate.setRating(rate);

        ArtistRate newArtistRate = artistRateDao.create(artistRate);

        return recomputeArtistRate(newArtistRate, existingArtist);
    }

    @Override
    public RateDto rateArtist(Principal user, String artistId, RateDto artistRate) {

        if (user == null) {
            throw new IllegalArgumentException("User principal can not be null");
        }

        if (artistRate == null) {
            throw new IllegalArgumentException("Artist rate can not be null");
        }

        return rateArtist(user.getName(), artistId, artistRate.getRating());
    }

    private RateDto artistRateToDto(ArtistRate artistRate) {

        RateDto rateDto = new RateDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(rateDto, artistRate);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create artist rate Data Transfer Object", e);
        }

        return rateDto;
    }

    private RateDto albumRateToDto(AlbumRate albumRate) {

        RateDto rateDto = new RateDto();
        PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
        try {
            propertyUtilsBean.copyProperties(rateDto, albumRate);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Can not create album rate Data Transfer Object", e);
        }

        return rateDto;
    }

}
