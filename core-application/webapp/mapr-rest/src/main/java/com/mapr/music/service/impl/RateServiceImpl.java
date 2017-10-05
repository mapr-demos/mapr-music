package com.mapr.music.service.impl;

import com.mapr.music.dao.*;
import com.mapr.music.exception.ResourceNotFoundException;
import com.mapr.music.model.*;
import com.mapr.music.service.RateService;

import javax.inject.Inject;
import javax.inject.Named;
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
    public AlbumRate getAlbumRate(Principal user, String albumId) {

        if (user == null || user.getName() == null || user.getName().isEmpty()) {
            return new AlbumRate();
        }

        String userId = user.getName();
        if (!userDao.exists(userId)) {
            throw new ResourceNotFoundException("User with id '" + userId + "' not found");
        }

        if (!albumDao.exists(albumId)) {
            throw new ResourceNotFoundException("Album with id '" + albumId + "' not found");
        }

        AlbumRate rate = albumRateDao.getRate(userId, albumId);
        return (rate != null) ? rate : new AlbumRate();
    }

    @Override
    public AlbumRate rateAlbum(AlbumRate albumRate) {

        if (albumRate == null) {
            throw new IllegalArgumentException("Album rate can not be null");
        }

        return rateAlbum(albumRate.getUserId(), albumRate.getDocumentId(), albumRate.getRating());
    }

    @Override
    public AlbumRate rateAlbum(String albumId, AlbumRate albumRate) {

        if (albumRate == null) {
            throw new IllegalArgumentException("Album rate can not be null");
        }

        return rateAlbum(albumRate.getUserId(), albumId, albumRate.getRating());
    }

    @Override
    public AlbumRate rateAlbum(String userId, String albumId, AlbumRate albumRate) {

        if (albumRate == null) {
            throw new IllegalArgumentException("Album rate can not be null");
        }

        return rateAlbum(userId, albumId, albumRate.getRating());
    }

    @Override
    public AlbumRate rateAlbum(String userId, String albumId, double rate) {

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
            recomputeAlbumRate(newAlbumRate, existingAlbum);
            return newAlbumRate;
        }

        AlbumRate albumRate = new AlbumRate();
        albumRate.setId(UUID.randomUUID().toString());
        albumRate.setUserId(userId);
        albumRate.setDocumentId(albumId);
        albumRate.setRating(rate);
        AlbumRate newAlbumRate = albumRateDao.create(albumRate);
        recomputeAlbumRate(newAlbumRate, existingAlbum);

        return newAlbumRate;
    }

    private void recomputeAlbumRate(AlbumRate newAlbumRate, Album existingAlbum) {
        List<AlbumRate> albumRates = albumRateDao.getByAlbumId(newAlbumRate.getDocumentId());
        double aggregatedRating = albumRates.stream().mapToDouble(AlbumRate::getRating).sum() / albumRates.size();
        existingAlbum.setRating(aggregatedRating);
        albumDao.update(existingAlbum.getId(), existingAlbum);
    }

    private void recomputeArtistRate(ArtistRate newArtistRate, Artist existingArtist) {
        List<ArtistRate> artistRates = artistRateDao.getByArtistId(newArtistRate.getDocumentId());
        double aggregatedRating = artistRates.stream().mapToDouble(ArtistRate::getRating).sum() / artistRates.size();
        existingArtist.setRating(aggregatedRating);
        artistDao.update(existingArtist.getId(), existingArtist);
    }

    @Override
    public AlbumRate rateAlbum(Principal user, String albumId, AlbumRate albumRate) {

        if (user == null) {
            throw new IllegalArgumentException("User principal can not be null");
        }

        if (albumRate == null) {
            throw new IllegalArgumentException("Album rate can not be null");
        }

        return rateAlbum(user.getName(), albumId, albumRate.getRating());
    }


    @Override
    public ArtistRate getArtistRate(Principal user, String artistId) {

        if (user == null || user.getName() == null || user.getName().isEmpty()) {
            return new ArtistRate();
        }

        String userId = user.getName();
        if (!userDao.exists(userId)) {
            throw new ResourceNotFoundException("User with id '" + userId + "' not found");
        }

        if (!artistDao.exists(artistId)) {
            throw new ResourceNotFoundException("Artist with id '" + artistId + "' not found");
        }

        ArtistRate rate = artistRateDao.getRate(userId, artistId);
        return (rate != null) ? rate : new ArtistRate();
    }

    @Override
    public ArtistRate rateArtist(ArtistRate artistRate) {

        if (artistRate == null) {
            throw new IllegalArgumentException("Artist rate can not be null");
        }

        return rateArtist(artistRate.getUserId(), artistRate.getDocumentId(), artistRate.getRating());
    }

    @Override
    public ArtistRate rateArtist(String artistId, ArtistRate artistRate) {

        if (artistRate == null) {
            throw new IllegalArgumentException("Artist rate can not be null");
        }

        return rateArtist(artistRate.getUserId(), artistId, artistRate.getRating());
    }

    @Override
    public ArtistRate rateArtist(String userId, String artistId, ArtistRate artistRate) {

        if (artistRate == null) {
            throw new IllegalArgumentException("Artist rate can not be null");
        }

        return rateArtist(userId, artistId, artistRate.getRating());
    }

    @Override
    public ArtistRate rateArtist(String userId, String artistId, double rate) {

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
            recomputeArtistRate(newArtistRate, existingArtist);

            return newArtistRate;
        }

        ArtistRate artistRate = new ArtistRate();
        artistRate.setId(UUID.randomUUID().toString());
        artistRate.setUserId(userId);
        artistRate.setDocumentId(artistId);
        artistRate.setRating(rate);

        ArtistRate newArtistRate = artistRateDao.create(artistRate);
        recomputeArtistRate(newArtistRate, existingArtist);

        return newArtistRate;
    }

    @Override
    public ArtistRate rateArtist(Principal user, String artistId, ArtistRate artistRate) {

        if (user == null) {
            throw new IllegalArgumentException("User principal can not be null");
        }

        if (artistRate == null) {
            throw new IllegalArgumentException("Artist rate can not be null");
        }

        return rateArtist(user.getName(), artistId, artistRate.getRating());
    }
}
