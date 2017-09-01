package parser;

import model.Artist;
import model.ArtistUrlLink;
import org.apache.commons.lang3.StringUtils;
import util.SlugUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArtistParser {

    private static final String VALUE_NOT_DEFINED_SYMBOL = "\\N";
    private static final String TAB_SYMBOL = "\t";
    private static final String EMPTY_STRING = "";

    private String artistFilePath;
    private String areaFilePath;
    private String artistIsniFilePath;
    private String artistCreditNameFilePath;
    private String genderFilePath;
    private String artistIpiPath;
    private String artistUrlsPath;
    private String urlsPath;
    private String linkPath;
    private String linkTypePath;

    public List<Artist> parseArtists(String dumpPath, int size) {
        artistFilePath = dumpPath + File.separator + "artist";
        areaFilePath = dumpPath + File.separator + "area";
        artistIsniFilePath = dumpPath + File.separator + "artist_isni";
        artistCreditNameFilePath = dumpPath + File.separator + "artist_credit_name";
        genderFilePath = dumpPath + File.separator + "gender";
        artistIpiPath = dumpPath + File.separator + "artist_ipi";
        artistUrlsPath = dumpPath + File.separator + "l_artist_url";
        urlsPath = dumpPath + File.separator + "url";
        linkPath = dumpPath + File.separator + "link";
        linkTypePath = dumpPath + File.separator + "link_type";

        List<Artist> artists = parseArtistFile(size);
        generateSlugs(artists);

        parseAreaFile(artists);
        parseArtistIsniFile(artists);
        parseArtistIpiFile(artists);
        parseGenderFile(artists);
        parseArtistCreditNameFile(artists);


        parseArtistLinks(artists);
        parseLinks(artists);
        parseLinkTypes(artists);
        parseUrls(artists);


        return artists;
    }

    private List<Artist> parseArtistFile(int size) {
        List<Artist> artists = null;
        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(artistFilePath))) {
            Stream<String[]> rows = stream.map(strRow ->
                    Arrays.stream(strRow.split(TAB_SYMBOL))
                            .map(val -> VALUE_NOT_DEFINED_SYMBOL.equals(val) ? EMPTY_STRING : val)
                            .toArray(String[]::new)

            );
            if (size > 0) {
                rows = rows.limit(size);
            }

            artists = rows.map(this::parseArtistRow).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseAreaFile(List<Artist> artists) {

        Map<String, List<Artist>> artistMap = artists.stream()
                .filter(artist -> !StringUtils.isEmpty(artist.getArea()))
                .collect(Collectors.groupingBy(Artist::getArea));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(areaFilePath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistList = artistMap.get(row[0]);
                        if (artistList != null) {
                            artistList.forEach(artist -> artist.setArea(row[2]));
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseGenderFile(List<Artist> artists) {

        Map<String, List<Artist>> artistMap = artists.stream()
                .filter(artist -> !StringUtils.isEmpty(artist.getGender()))
                .collect(Collectors.groupingBy(Artist::getGender));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(genderFilePath))) {

            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistList = artistMap.get(row[0]);
                        if (artistList != null) {
                            artistList.forEach(artist -> artist.setGender(row[1]));
                        }
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseArtistIsniFile(List<Artist> artists) {

        Map<String, List<Artist>> artistMap = artists.stream()
                .filter(album -> !StringUtils.isEmpty(album.getPk()))
                .collect(Collectors.groupingBy(Artist::getPk));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(artistIsniFilePath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistList = artistMap.get(row[0]);
                        if (artistList != null) {
                            artistList.forEach(artist -> artist.setIsni(row[1]));
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseArtistIpiFile(List<Artist> artists) {
        Map<String, List<Artist>> artistMap = artists.stream()
                .filter(album -> !StringUtils.isEmpty(album.getPk()))
                .collect(Collectors.groupingBy(Artist::getPk));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(artistIpiPath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistList = artistMap.get(row[0]);
                        if (artistList != null) {
                            artistList.forEach(artist -> artist.setIpi(row[1]));
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseArtistCreditNameFile(List<Artist> artists) {
        Map<String, List<Artist>> artistMap = artists.stream()
                .filter(album -> !StringUtils.isEmpty(album.getPk()))
                .collect(Collectors.groupingBy(Artist::getPk));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(artistCreditNameFilePath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistList = artistMap.get(row[2]);
                        if (artistList != null) {
                            artistList.forEach(artist -> artist.setArtistCreditId(row[0]));
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseArtistLinks(List<Artist> artists) {
        Map<String, List<Artist>> artistMap = artists.stream()
                .filter(album -> !StringUtils.isEmpty(album.getPk()))
                .collect(Collectors.groupingBy(Artist::getPk));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(artistUrlsPath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistList = artistMap.get(row[2]);
                        if (artistList != null) {
                            artistList.forEach(artist -> artist.getLinks().add(new ArtistUrlLink(row[1], row[3])));
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseLinks(List<Artist> artists) {
        Map<String, List<ArtistUrlLink>> linkIdArtistLinksMap = artists.stream().flatMap(artist -> artist.getLinks().stream())
                .filter(artistUrlLink -> !StringUtils.isEmpty(artistUrlLink.getLinkId()))
                .collect(Collectors.groupingBy(ArtistUrlLink::getLinkId));


        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(linkPath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<ArtistUrlLink> artistUrlLinks = linkIdArtistLinksMap.get(row[0]);
                        if (artistUrlLinks != null) {
                            artistUrlLinks.forEach(artistUrlLink -> artistUrlLink.setLinkTypeId(row[1]));
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseLinkTypes(List<Artist> artists) {

        Map<String, List<Artist>> linkTypeArtistMap = new HashMap<>();
        artists.stream()
                .filter(artist -> !artist.getLinks().isEmpty())
                .forEach(artist -> {
                    artist.getLinks().forEach(artistUrlLink -> {
                        if (!linkTypeArtistMap.containsKey(artistUrlLink.getLinkTypeId())) {
                            linkTypeArtistMap.put(artistUrlLink.getLinkTypeId(), new ArrayList<>());
                        }
                        linkTypeArtistMap.get(artistUrlLink.getLinkTypeId()).add(artist);
                    });
                });

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(linkTypePath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistWithSameLinkType = linkTypeArtistMap.get(row[0]);
                        if (artistWithSameLinkType != null) {

                            if (!"artist".equals(row[4]) || !"image".equals(row[6])) {
                                artists.forEach(artist -> {
                                    List<ArtistUrlLink> notPictureLinks = artist.getLinks()
                                            .stream()
                                            .filter(artistUrlLink -> row[0].equals(artistUrlLink.getLinkTypeId()))
                                            .collect(Collectors.toList());// not 'picture' links. remove them
                                    artist.getLinks().removeAll(notPictureLinks);

                                });

                            }
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private List<Artist> parseUrls(List<Artist> artists) {


        Map<String, List<Artist>> urlArtistMap = new HashMap<>();
        artists.stream()
                .filter(artist -> !artist.getLinks().isEmpty())
                .forEach(artist -> {
                    artist.getLinks().forEach(artistUrlLink -> {
                        if (!urlArtistMap.containsKey(artistUrlLink.getUrlId())) {
                            urlArtistMap.put(artistUrlLink.getUrlId(), new ArrayList<>());
                        }
                        urlArtistMap.get(artistUrlLink.getUrlId()).add(artist);
                    });
                });

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(urlsPath))) {
            stream.map(strRow -> strRow.split(TAB_SYMBOL))
                    .forEach(row -> {
                        List<Artist> artistsWithSuchUrlList = urlArtistMap.get(row[0]);
                        if (artistsWithSuchUrlList != null) {

                            String pictureUrl = row[2];
                            artistsWithSuchUrlList.forEach(artist -> {

                                if (artist.getProfileImageUrl() == null || artist.getProfileImageUrl().isEmpty()) {
                                    artist.setProfileImageUrl(pictureUrl);
                                } else {
                                    artist.addImageUrl(pictureUrl);
                                }
                            });
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return artists;
    }

    private void generateSlugs(List<Artist> artists) {

        Map<String, List<Artist>> slugNameArtistMap = artists.stream()
                .filter(artist -> StringUtils.isNotEmpty(artist.getName()))
                .peek(artist -> artist.setSlugName(SlugUtil.toSlug(artist.getName())))
                .collect(Collectors.groupingBy(Artist::getSlugName));

        slugNameArtistMap.values().stream()
                .filter(artistList -> artistList.size() > 1)
                .forEach(artistList -> {
                    long slug_postfix = 1;
                    for (Artist artist : artistList) {
                        artist.setSlugPostfix(slug_postfix++);
                    }
                });

    }

    private Artist parseArtistRow(String[] values) {
        Artist artist = new Artist();
        artist.setPk(values[0]);
        artist.setId(values[1]);
        artist.setMBID(values[1]);
        artist.setName(values[2]);
        artist.setDisambiguationComment(values[13]);
        artist.setSlugPostfix(0);

        artist.setBeginDate(ParserUtils.parseTimeStamp(values[4], values[5], values[6]));
        artist.setEndDate(ParserUtils.parseTimeStamp(values[7], values[8], values[9]));

        artist.setArea(values[11]); // area id
        artist.setGender(values[12]); // gender id

        return artist;
    }

}
