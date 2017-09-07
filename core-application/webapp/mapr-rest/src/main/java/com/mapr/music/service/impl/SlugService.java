package com.mapr.music.service.impl;

import com.mapr.music.dao.MaprDbDao;
import com.mapr.music.model.Album;
import com.mapr.music.model.Artist;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.ojai.Document;
import org.ojai.store.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Normalizer;
import java.util.Iterator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Service responsible for managing slugs.
 */
public class SlugService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    private static final String SLUG_POSTFIX_DELIMITER = "-";
    private static final Long SLUG_FIRST_POSTFIX = 0L;

    private final MaprDbDao<Artist> artistDao;
    private final MaprDbDao<Album> albumDao;

    class GetLastPostfixAction implements MaprDbDao.OjaiStoreAction<Long> {

        String slugName;

        GetLastPostfixAction(String slugName) {
            this.slugName = slugName;
        }

        @Override
        public Long process(Connection connection, DocumentStore store) {
            QueryCondition condition = connection.newCondition()
                    .is("slug_name", QueryCondition.Op.EQUAL, slugName);

            Query query = connection.newQuery()
                    .select("slug_postfix")
                    .where(condition.build())
                    .orderBy("slug_postfix", SortOrder.DESC)
                    .build();

            Iterator<Document> iterator = store.findQuery(query).iterator();
            if (!iterator.hasNext()) {
                return null;
            }

            return iterator.next().getLong("slug_postfix");
        }
    }

    @Inject
    public SlugService(@Named("artistDao") MaprDbDao<Artist> artistDao, @Named("albumDao") MaprDbDao<Album> albumDao) {
        this.artistDao = artistDao;
        this.albumDao = albumDao;
    }

    /**
     * Converts specified string to it's slug representation, which can be used to generate readable and SEO-friendly
     * URLs.
     *
     * @param input string, which will be converted.
     * @return slug representation of string, which can be used to generate readable and SEO-friendly
     * URLs.
     */
    public static String toSlug(String input) {

        String noWhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");

        return slug.toLowerCase(Locale.ENGLISH);
    }

    /**
     * Sets slug fields for the specified instance of {@link Album} class according to album's name. Should be
     * invoked while creating new {@link Album} in order to generate and set slug values.
     *
     * @param album album, for which slug values will be set.
     */
    public void setSlugForAlbum(Album album) {

        if (album == null) {
            throw new IllegalArgumentException("Album can not be null");
        }

        String slugName = toSlug(album.getName());
        Long lastPostfix = albumDao.processStore(new GetLastPostfixAction(slugName));
        long postfix = (lastPostfix == null) ? SLUG_FIRST_POSTFIX : lastPostfix + 1;
        album.setSlugName(slugName);
        album.setSlugPostfix(postfix);
    }

    /**
     * Sets slug fields for the specified instance of {@link Artist} class according to artist's name. Should be
     * invoked while creating new {@link Artist} in order to generate and set slug values.
     *
     * @param artist artist, for which slug values will be set.
     */
    public void setSlugForArtist(Artist artist) {

        if (artist == null) {
            throw new IllegalArgumentException("Artist can not be null");
        }

        String slugName = toSlug(artist.getName());
        Long lastPostfix = artistDao.processStore(new GetLastPostfixAction(slugName));
        long postfix = (lastPostfix == null) ? SLUG_FIRST_POSTFIX : lastPostfix + 1;
        artist.setSlugName(slugName);
        artist.setSlugPostfix(postfix);
    }


    /**
     * Returns single album by it's slug. If there is no such document <code>null</code> will be returned.
     *
     * @param slug albums's slug.
     * @return album with the specified slug.
     */
    public Album getAlbumBySlug(String slug) {
        return getBySlug(albumDao, slug);
    }

    /**
     * Returns single album by it's slug. If there is no such document <code>null</code> will be returned.
     *
     * @param slug albums's slug.
     * @return album with the specified slug.
     */
    public Artist getArtistBySlug(String slug) {
        return getBySlug(artistDao, slug);
    }

    /**
     * Construct and returns slug for album according to it's slug name and slug postfix value.
     *
     * @param album contains slug name and slug postfix values.
     * @return slug sting which contains slug name and slug postfix, separated by
     * {@link SlugService#SLUG_POSTFIX_DELIMITER}.
     */
    public String getSlugForAlbum(Album album) {

        if (album == null) {
            throw new IllegalArgumentException("Album can not be null");
        }

        return constructSlugString(album.getSlugName(), album.getSlugPostfix());
    }

    /**
     * Construct and returns slug for artist according to it's slug name and slug postfix value.
     *
     * @param artist contains slug name and slug postfix values.
     * @return slug sting which contains slug name and slug postfix, separated by
     * {@link SlugService#SLUG_POSTFIX_DELIMITER}.
     */
    public String getSlugForArtist(Artist artist) {

        if (artist == null) {
            throw new IllegalArgumentException("Artist can not be null");
        }

        return constructSlugString(artist.getSlugName(), artist.getSlugPostfix());
    }

    private String constructSlugString(String slugName, Long slugPostfix) {

        if (slugName != null && slugPostfix != null) {
            return slugName + SLUG_POSTFIX_DELIMITER + slugPostfix;
        }

        return null;
    }

    private <T> T getBySlug(MaprDbDao<T> dbDao, String slug) {

        Pair<String, Long> slugPostfixPair = getSlugPostfixPair(slug);
        String slugWithoutPostfix = slugPostfixPair.getFirst();
        Long postfix = slugPostfixPair.getSecond();

        if (postfix == null) {
            throw new IllegalArgumentException("Slug name must contain numeric postfix");
        }

        return dbDao.processStore((connection, store) -> {

            QueryCondition condition = connection.newCondition()
                    .and()
                    .is("slug_name", QueryCondition.Op.EQUAL, slugWithoutPostfix)
                    .is("slug_postfix", QueryCondition.Op.EQUAL, postfix)
                    .close();


            Query query = connection.newQuery()
                    .select("*")
                    .where(condition.build())
                    .build();

            Iterator<Document> iterator = store.findQuery(query).iterator();
            if (!iterator.hasNext()) {
                return null;
            }

            return dbDao.mapOjaiDocument(iterator.next());
        });
    }

    private Pair<String, Long> getSlugPostfixPair(String slug) {

        if (!slug.contains(SLUG_POSTFIX_DELIMITER)) {
            return new Pair<>(slug, null);
        }

        int indexOfPossiblePostfix = slug.lastIndexOf(SLUG_POSTFIX_DELIMITER);
        String possiblePostifxAsString = slug.substring(indexOfPossiblePostfix + 1, slug.length());

        if (!StringUtils.isNumeric(possiblePostifxAsString)) {
            return new Pair<>(slug, null);
        }

        // slug name has numeric postfix
        String slugNameWithoutNumericPostfix = slug.substring(0, indexOfPossiblePostfix);
        Long postfix = Long.parseLong(possiblePostifxAsString);

        return new Pair<>(slugNameWithoutNumericPostfix, postfix);
    }

}
