package com.mapr.music.service;

import com.mapr.music.model.AlbumRate;
import com.mapr.music.model.ArtistRate;

import java.security.Principal;

public interface RateService {

    AlbumRate getAlbumRate(Principal user, String albumId);

    AlbumRate rateAlbum(AlbumRate albumRate);

    AlbumRate rateAlbum(String albumId, AlbumRate albumRate);

    AlbumRate rateAlbum(String userId, String albumId, AlbumRate albumRate);

    AlbumRate rateAlbum(String userId, String albumId, double rate);

    AlbumRate rateAlbum(Principal user, String albumId, AlbumRate albumRate);

    ArtistRate getArtistRate(Principal user, String artistId);

    ArtistRate rateArtist(ArtistRate artistRate);

    ArtistRate rateArtist(String artistId, ArtistRate artistRate);

    ArtistRate rateArtist(String userId, String artistId, ArtistRate artistRate);

    ArtistRate rateArtist(String userId, String artistId, double rate);

    ArtistRate rateArtist(Principal user, String artistId, ArtistRate artistRate);

}
