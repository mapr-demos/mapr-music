package com.mapr.music.service;

import com.mapr.music.dto.AlbumDto;
import com.mapr.music.dto.ArtistDto;

import java.security.Principal;
import java.util.List;

public interface RecommendationService {

    List<ArtistDto> getRecommendedArtists(String artistId, Principal user);

    List<ArtistDto> getRecommendedArtists(String artistId, Principal user, Integer limit);

    List<AlbumDto> getRecommendedAlbums(String albumId, Principal user);

    List<AlbumDto> getRecommendedAlbums(String albumId, Principal user, Integer limit);
}
