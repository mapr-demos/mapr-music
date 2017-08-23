package com.mapr.music.service.impl;

import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.dao.SortOption;
import com.mapr.music.dao.impl.AlbumDao;
import com.mapr.music.dao.impl.ArtistsDao;
import com.mapr.music.dto.ResourceDto;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import com.mapr.music.service.ArtistService;
import com.mapr.music.service.PaginatedService;

import javax.inject.Named;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Named
public class ArtistServiceImpl implements ArtistService, PaginatedService {

    private static final long ARTISTS_PER_PAGE_DEFAULT = 50;
    private static final long FIRST_PAGE_NUM = 1;

    // FIXME '_id' hardcode
    private static final String JSON_STRING_ID_TEMPLATE = "{\"_id\": \"%s\", ";

    private static final String[] ARTIST_SHORT_INFO_FIELDS = {
            "_id",
            "name",
            "gender",
            "area",
            "IPI",
            "ISNI",
            "MBID",
            "disambiguation_comment",
            "albums",
            "profile_image_url",
            "images_urls",
            "begin_date",
            "end_date"
    };

    private final MaprDbDao<Artist> artistDao;
    private final MaprDbDao<Album> albumDao;

    // FIXME use DI
    public ArtistServiceImpl() {
        this.artistDao = new ArtistsDao();
        this.albumDao = new AlbumDao();
    }

    @Override
    public long getTotalNum() {
        return artistDao.getTotalNum();
    }

    @Override
    public ResourceDto<Artist> getArtistsPage() {
        return getArtistsPage(FIRST_PAGE_NUM);
    }

    @Override
    public ResourceDto<Artist> getArtistsPage(Long page) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, page, null, null);
    }

    @Override
    public ResourceDto<Artist> getArtistsPage(String order, List<String> orderFields) {
        return getArtistsPage(ARTISTS_PER_PAGE_DEFAULT, FIRST_PAGE_NUM, order, orderFields);
    }

    @Override
    public ResourceDto<Artist> getArtistsPage(Long perPage, Long page, String order, List<String> orderFields) {

        if (page == null) {
            page = FIRST_PAGE_NUM;
        }

        if (page <= 0) {
            throw new IllegalArgumentException("Page must be greater than zero");
        }

        if (perPage == null) {
            perPage = ARTISTS_PER_PAGE_DEFAULT;
        }

        if (perPage <= 0) {
            throw new IllegalArgumentException("Per page value must be greater than zero");
        }

        ResourceDto<Artist> artistsPage = new ResourceDto<>();
        artistsPage.setPagination(getPaginationInfo(page, perPage));
        long offset = (page - 1) * perPage;

        SortOption[] sortOptions = null;
        if (order != null && orderFields != null && orderFields.size() > 0) {
            SortOption.Order sortOrder = SortOption.Order.valueOf(order.toUpperCase());
            sortOptions = new SortOption[]{new SortOption(sortOrder, orderFields)};
        }

        List<Artist> artists = artistDao.getList(offset, perPage, sortOptions, ARTIST_SHORT_INFO_FIELDS);
        artistsPage.setResults(artists);

        return artistsPage;
    }


    public Artist getById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        Artist artist = artistDao.getById(id);
        List albumsShortInfo = (List) artist.getAlbums().stream()
                .map(albumId -> albumDao.getById((String) albumId, "_id", "name", "cover_image_url", "released_date"))
                .collect(Collectors.toList());
        artist.setAlbums(albumsShortInfo);

        return artist;
    }

    @Override
    public void deleteArtistById(String id) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        artistDao.deleteById(id);
    }

    @Override
    public Artist createArtist(Artist artist) {

        if (artist == null) {
            throw new IllegalArgumentException("Artist can not be null");
        }

        String id = UUID.randomUUID().toString();
        artist.setId(id);

        return artistDao.create(artist);
    }

    @Override
    public Artist createArtist(String jsonString) {

        if (jsonString == null || jsonString.isEmpty()) {
            throw new IllegalArgumentException("Artist JSON string can not be empty");
        }

        return artistDao.create(appendId(jsonString));
    }

    @Override
    public Artist updateArtist(Artist artist) {

        if (artist == null || artist.getId() == null || artist.getId().isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        return artistDao.update(artist.getId(), artist);
    }

    @Override
    public Artist updateArtist(String id, Artist artist) {

        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Artist's identifier can not be empty");
        }

        if (artist == null) {
            throw new IllegalArgumentException("Artist can not be null");
        }

        return artistDao.update(id, artist);
    }

    // FIXME hardcode, duplication
    private static String appendId(String jsonString) {

        String id = UUID.randomUUID().toString();
        int indexOfIdKey = jsonString.indexOf("\"_id\"");
        if (indexOfIdKey < 0) {

            String idFormatted = String.format(JSON_STRING_ID_TEMPLATE, id);
            return jsonString.replaceFirst("\\{", idFormatted);
        }

        String replacement = String.format("$1 \"%s\"$3", id);
        return jsonString.replaceAll("(\"_id\":)(.*?)(,)", replacement);
    }

}
