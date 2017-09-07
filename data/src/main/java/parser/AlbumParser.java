package parser;

import client.CoverArtArchiveClient;
import model.Album;
import model.Artist;
import model.Language;
import model.Track;
import org.apache.commons.lang3.StringUtils;
import util.SlugUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlbumParser {

    private static final String VALUE_NOT_DEFINED_SYMBOL = "\\N";
    private static final String TAB_SYMBOL = "\t";
    private static final String EMPTY_STRING = "";

    private List<Artist> artists;
    private String albumFilePath;
    private String albumStatusFilePath;
    private String albumPackagingFilePath;
    private String languageFilePath;
    private String mediumFilePath;
    private String trackFilePath;

    private Set<Language> existingLanguages = new HashSet<>();

    public List<Album> parsAlbums(String dumpPath, List<Artist> artists) {
        this.artists = artists;

        albumFilePath = dumpPath + File.separator + "release";
        albumStatusFilePath = dumpPath + File.separator + "release_status";
        albumPackagingFilePath = dumpPath + File.separator + "release_packaging";
        languageFilePath = dumpPath + File.separator + "language";
        mediumFilePath = dumpPath + File.separator + "medium";
        trackFilePath = dumpPath + File.separator + "track";

        List<Album> albums = parseAlbumFile();
        generateSlugs(albums);

        parseReleaseStatusFile(albums);
        parseReleasePackagingFile(albums);
        parseReleaseLanguageFile(albums);

        parseMediumFile(albums);
        parseTrackFile(albums);

        return albums;
    }

    private List<Album> parseAlbumFile() {
        Map<String, List<Artist>> artistMap = artists.stream()
                .filter(artist -> !StringUtils.isEmpty(artist.getArtistCreditId()))
                .collect(Collectors.groupingBy(Artist::getArtistCreditId));

        List<Album> albums = null;
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(albumFilePath))) {
            Stream<String[]> rows = stream.map(strRow ->
                    Arrays.stream(strRow.split(TAB_SYMBOL))
                            .map(val -> val.equals(VALUE_NOT_DEFINED_SYMBOL) ? EMPTY_STRING : val)
                            .toArray(String[]::new)

            );
            albums = rows.flatMap(row -> {
                List<Album> albumList = new LinkedList<>();
                List<Artist> artistList = artistMap.get(row[3]);
                if (artistList != null) {
                    artistList.forEach(artist -> {
                        Album album = parseAlbumRow(row, artist);
                        album.getArtistList().add(artist);
                        albumList.add(album);
                    });
                }

                return albumList.stream();
            }).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return albums;
    }

    private List<Album> parseReleaseStatusFile(List<Album> albums) {
        Map<String, List<Album>> albumMap = albums.stream()
                .filter(album -> !StringUtils.isEmpty(album.getStatus()))
                .collect(Collectors.groupingBy(Album::getStatus));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(albumStatusFilePath))) {
            Stream<String[]> rows = stream.map(strRow -> strRow.split(TAB_SYMBOL));
            rows.forEach(row -> {
                List<Album> albumList = albumMap.get(row[0]);
                if (albumList != null) {
                    albumList.forEach(album -> album.setStatus(row[1]));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return albums;
    }

    private List<Album> parseReleasePackagingFile(List<Album> albums) {
        Map<String, List<Album>> albumMap = albums.stream()
                .filter(album -> !StringUtils.isEmpty(album.getPackaging()))
                .collect(Collectors.groupingBy(Album::getPackaging));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(albumPackagingFilePath))) {
            Stream<String[]> rows = stream.map(strRow -> strRow.split(TAB_SYMBOL));
            rows.forEach(row -> {
                List<Album> albumList = albumMap.get(row[0]);
                if (albumList != null) {
                    albumList.forEach(album -> album.setPackaging(row[1]));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return albums;
    }

    private List<Album> parseReleaseLanguageFile(List<Album> albums) {

        Map<String, List<Album>> albumMap = albums.stream()
                .filter(album -> StringUtils.isNotEmpty(album.getLanguage()))
                .collect(Collectors.groupingBy(Album::getLanguage));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(languageFilePath))) {
            Stream<String[]> rows = stream.map(strRow -> strRow.split(TAB_SYMBOL));
            rows.forEach(row -> {
                List<Album> albumList = albumMap.get(row[0]);
                if (albumList != null) {

                    String iso639Third = row[6]; // ISO 639-3
                    String iso639SecondT = row[1]; // ISO 639-2 (T)
                    String iso639SecondB = row[2]; // ISO 639-2 (B)
                    String iso639First = row[3]; // ISO 639

                    final String code = (!VALUE_NOT_DEFINED_SYMBOL.equals(iso639Third)) ? iso639Third
                            : (!VALUE_NOT_DEFINED_SYMBOL.equals(iso639SecondT)) ? iso639SecondT
                            : (!VALUE_NOT_DEFINED_SYMBOL.equals(iso639SecondB)) ? iso639SecondB
                            : (!VALUE_NOT_DEFINED_SYMBOL.equals(iso639First)) ? iso639First
                            : null;

                    existingLanguages.add(new Language(code, row[4]));
                    albumList.forEach(album -> album.setLanguage(code));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return albums;
    }

    private List<Album> parseMediumFile(List<Album> albums) {

        Map<String, List<Album>> albumIdAlbumMap = albums.stream()
                .filter(album -> !StringUtils.isEmpty(album.getPk()))
                .collect(Collectors.groupingBy(Album::getPk));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(mediumFilePath))) {
            Stream<String[]> rows = stream.map(strRow -> strRow.split(TAB_SYMBOL));
            rows.forEach(row -> {
                List<Album> albumList = albumIdAlbumMap.get(row[1]);
                if (albumList != null) {
                    albumList.forEach(album -> album.setMediumId(row[0]));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return albums;
    }

    private List<Album> parseTrackFile(List<Album> albums) {

        Map<String, List<Album>> mediumIdAlbumMap = albums.stream()
                .filter(album -> !StringUtils.isEmpty(album.getMediumId()))
                .collect(Collectors.groupingBy(Album::getMediumId));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(trackFilePath))) {
            Stream<String[]> rows = stream.map(strRow -> strRow.split(TAB_SYMBOL));
            rows.forEach(row -> {
                List<Album> albumList = mediumIdAlbumMap.get(row[3]);
                if (albumList != null) {
                    parseTrack(row, albumList);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return albums;
    }

    private void generateSlugs(List<Album> albums) {
        Map<String, List<Album>> slugNameAlbumMap = albums.stream()
                .filter(album -> StringUtils.isNotEmpty(album.getName()))
                .peek(album -> album.setSlugName(SlugUtil.toSlug(album.getName())))
                .collect(Collectors.groupingBy(Album::getSlugName));

        slugNameAlbumMap.values().stream()
                .filter(albumList -> albumList.size() > 1)
                .forEach(albumList -> {
                    long slug_postfix = 1;
                    for (Album album : albumList) {
                        album.setSlugPostfix(slug_postfix++);
                    }
                });
    }

    private Album parseAlbumRow(String[] values, Artist artist) {

        Album album = new Album();
        album.setId(values[1]);
        album.setPk(values[0]);
        album.setName(values[2]);

        if (!VALUE_NOT_DEFINED_SYMBOL.equals(values[5])) {
            album.setStatus(values[5]); //Status ID
        }

        if (!VALUE_NOT_DEFINED_SYMBOL.equals(values[6])) {
            album.setPackaging(values[6]); //Packaging ID
        }

        if (!VALUE_NOT_DEFINED_SYMBOL.equals(values[7])) {
            album.setLanguage(values[7]); //Language ID
        }
        album.setScript(values[8]);
        album.setBarcode(values[9]);
        album.setMBID(values[1]);
        album.setSlugPostfix(0);

        artist.getAlbumsIds().add(album.getId());

        CoverArtArchiveClient artArchiveClient = new CoverArtArchiveClient(album.getMBID());
        album.setCoverImageUrl(artArchiveClient.getCoverImage());
        album.setImagesUrls(artArchiveClient.getImages());

        return album;
    }

    public static List<Album> parseTrack(String[] values, List<Album> albumList) {
        Track track = new Track();

        if (!VALUE_NOT_DEFINED_SYMBOL.equals(values[8])) {
            track.setLength(Integer.parseInt(values[8]));
        }
        track.setId(values[1]);
        track.setMBID(values[1]);
        track.setName(values[6]);
        track.setPosition(Integer.parseInt(values[4]));

        albumList.forEach(album -> album.addTrack(track));
        return albumList;
    }

    public Set<Language> getExistingLanguages() {
        return existingLanguages;
    }
}