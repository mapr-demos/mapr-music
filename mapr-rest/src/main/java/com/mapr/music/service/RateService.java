package com.mapr.music.service;

import com.mapr.music.dto.RateDto;

import java.security.Principal;

public interface RateService {

    RateDto getAlbumRate(Principal user, String albumId);

    RateDto rateAlbum(String userId, String albumId, RateDto albumRate);

    RateDto rateAlbum(String userId, String albumId, double rate);

    RateDto rateAlbum(Principal user, String albumId, RateDto albumRate);

    RateDto getArtistRate(Principal user, String artistId);

    RateDto rateArtist(String userId, String artistId, RateDto artistRate);

    RateDto rateArtist(String userId, String artistId, double rate);

    RateDto rateArtist(Principal user, String artistId, RateDto artistRate);

}
