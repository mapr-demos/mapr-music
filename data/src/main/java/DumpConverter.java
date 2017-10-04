import com.fasterxml.jackson.databind.ObjectMapper;
import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import parser.AlbumParser;
import parser.ArtistParser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DumpConverter {

    public static final int DEFAULT_NUMBER_OF_USERS = 3_000;
    public static final long DEFAULT_NUMBER_OF_ARTIST_DOCS = 10_000;

    private static final String ARTISTS_DIRECTORY_NAME = "artists";
    private static final String ALBUMS_DIRECTORY_NAME = "albums";
    private static final String LANGUAGES_DIRECTORY_NAME = "languages";
    private static final String USERS_DIRECTORY_NAME = "users";
    private static final String ARTISTS_RATINGS_DIRECTORY_NAME = "ratings-artists";
    private static final String ALBUMS_RATINGS_DIRECTORY_NAME = "ratings-albums";
    private static final String JSON_EXTENSION_NAME = ".json";

    private static final Logger log = LoggerFactory.getLogger(DumpConverter.class);

    private String dumpDirectory;
    private String destinationDirectory;
    private Long numberOfArtists;
    private Integer numberOfUsers;
    private boolean convertOnlyWithImages;

    private static ObjectMapper mapper = new ObjectMapper();

    public DumpConverter() {
    }

    public DumpConverter(String dumpDirectory, String destinationDirectory) {
        this.dumpDirectory = dumpDirectory;
        this.destinationDirectory = destinationDirectory;
    }

    public DumpConverter(String dumpDirectory, String destinationDirectory, Long numberOfArtists, boolean convertOnlyWithImages) {
        this(dumpDirectory, destinationDirectory);
        this.numberOfArtists = numberOfArtists;
        this.convertOnlyWithImages = convertOnlyWithImages;
    }

    public void setDumpDirectory(String dumpDirectory) {
        this.dumpDirectory = dumpDirectory;
    }

    public void setDestinationDirectory(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public void setNumberOfArtists(Long numberOfArtists) {
        this.numberOfArtists = numberOfArtists;
    }

    public void setNumberOfUsers(Integer numberOfUsers) {
        this.numberOfUsers = numberOfUsers;
    }

    public void setConvertOnlyWithImages(boolean convertOnlyWithImages) {
        this.convertOnlyWithImages = convertOnlyWithImages;
    }

    public void convert() {

        if (dumpDirectory == null || destinationDirectory == null ||
                dumpDirectory.isEmpty() || destinationDirectory.isEmpty()) {
            throw new IllegalStateException("Directory path can not be empty");
        }

        if (numberOfArtists == null) {
            numberOfArtists = DEFAULT_NUMBER_OF_ARTIST_DOCS;
        }

        if (numberOfUsers == null) {
            numberOfUsers = DEFAULT_NUMBER_OF_USERS;
        }

        long startTime = System.currentTimeMillis();
        log.info("Started converting Music Brainz dump into Dataset");

        ArtistParser artistParser = new ArtistParser();
        List<Artist> artists = artistParser.parseArtists(dumpDirectory, numberOfArtists, convertOnlyWithImages);
        log.info("{} artists parsed. Parsing artists' albums ...", artists.size());

        AlbumParser albumParser = new AlbumParser();
        List<Album> albums = albumParser.parseAlbums(dumpDirectory, artists, convertOnlyWithImages);
        log.info("{} albums parsed.", albums.size());

        // Generate users
        log.info("Generating '{}' users ...", numberOfUsers);
        Set<User> users = UserGenerator.generate(numberOfUsers);

        // Generate Artists ratings
        log.info("Generating ratings for the artists. Number of users: {}", numberOfUsers);
        Set<String> usersIds = users.stream().map(User::getId).collect(Collectors.toSet());
        Set<String> artistsIds = artists.stream().map(Artist::getId).collect(Collectors.toSet());
        Set<Rating> artistsRatings = RatingGenerator.generateRatings(artistsIds, usersIds);
        log.info("{} artist ratings generated.", artistsRatings.size());

        // Generate Albums ratings
        log.info("Generating ratings for the albums. Number of users: {}", numberOfUsers);
        Set<String> albumsIds = albums.stream().map(Album::getId).collect(Collectors.toSet());
        Set<Rating> albumsRatings = RatingGenerator.generateRatings(albumsIds, usersIds);
        log.info("{} albums ratings generated.", albumsRatings.size());

        // Save artists
        String artistsDirectoryPath = destinationDirectory + File.separator + ARTISTS_DIRECTORY_NAME;
        createDirectoryIfNotExists(artistsDirectoryPath);

        log.info("Saving Artist JSON files to '{}'", artistsDirectoryPath);
        artists.forEach(artist -> writeJson(artist, artistsDirectoryPath, artist.getMBID()));

        // Save albums
        String albumsDirectoryPath = destinationDirectory + File.separator + ALBUMS_DIRECTORY_NAME;
        createDirectoryIfNotExists(albumsDirectoryPath);

        log.info("Saving Albums JSON files to '{}'", albumsDirectoryPath);
        albums.forEach(album -> writeJson(album, albumsDirectoryPath, album.getMBID()));

        // Save languages
        String languagesDirectoryPath = destinationDirectory + File.separator + LANGUAGES_DIRECTORY_NAME;
        createDirectoryIfNotExists(languagesDirectoryPath);
        Set<Language> existingLanguages = albumParser.getExistingLanguages();

        log.info("Saving Language JSON files to '{}'", languagesDirectoryPath);
        existingLanguages.forEach(language -> writeJson(language, languagesDirectoryPath, language.getId()));

        // Save users
        String usersDirectoryPath = destinationDirectory + File.separator + USERS_DIRECTORY_NAME;
        createDirectoryIfNotExists(usersDirectoryPath);
        log.info("Saving User JSON files to '{}'", usersDirectoryPath);
        users.forEach(user -> writeJson(user, usersDirectoryPath, user.getId()));

        // Save artists ratings
        String artistsRatingsDirectoryPath = destinationDirectory + File.separator + ARTISTS_RATINGS_DIRECTORY_NAME;
        createDirectoryIfNotExists(artistsRatingsDirectoryPath);

        log.info("Saving Artists ratings JSON files to '{}'", artistsRatingsDirectoryPath);
        artistsRatings.forEach(rating -> writeJson(rating, artistsRatingsDirectoryPath, rating.getId()));

        // Save artists ratings
        String albumsRatingsDirectoryPath = destinationDirectory + File.separator + ALBUMS_RATINGS_DIRECTORY_NAME;
        createDirectoryIfNotExists(albumsRatingsDirectoryPath);

        log.info("Saving Albums ratings JSON files to '{}'", albumsRatingsDirectoryPath);
        albumsRatings.forEach(rating -> writeJson(rating, albumsRatingsDirectoryPath, rating.getId()));

        long conversionTookMillis = System.currentTimeMillis() - startTime;
        long hours = TimeUnit.MILLISECONDS.toHours(conversionTookMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(conversionTookMillis) - TimeUnit.HOURS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(conversionTookMillis) - TimeUnit.HOURS.toSeconds(hours) -
                TimeUnit.MINUTES.toSeconds(minutes);

        String conversionTookFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        log.info("Music Brainz dump converted in '{}'. '{}' Artist, '{}' Album and '{}' Language JSON documents " +
                        "created. Resulting dataset can be found at '{}'", conversionTookFormatted, artists.size(),
                albums.size(), existingLanguages.size(), destinationDirectory);

    }

    private static void writeJson(Object value, String directoryPath, String id) {
        try {
            mapper.writeValue(new File(directoryPath + File.separator + id + JSON_EXTENSION_NAME), value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createDirectoryIfNotExists(String directoryPath) {

        File directory = new File(directoryPath);
        if (!directory.isDirectory() && !directory.mkdirs()) {
            throw new IllegalStateException("Unable to create output directory at '" + directoryPath + "'");
        }
    }

}
