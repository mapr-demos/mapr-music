import com.fasterxml.jackson.databind.ObjectMapper;
import model.Album;
import model.Artist;
import parser.AlbumParser;
import parser.ArtistParser;

import java.io.File;
import java.io.IOException;
import java.util.List;

// TODO type tags
public class Main {

    //TODO specify PATH
    private static final String DUMP_PATH = "/home/user108a/mb-data/mbdump/mbdump";
    private static final String ARTISTS_DIRECTORY_PATH = "/home/user108a/artists";
    private static final String ALBUMS_DIRECTORY_PATH = "/home/user108a/albums";
    private static final String JSON_EXTENSION_NAME = ".json";


    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        ArtistParser artistParser = new ArtistParser();
        List<Artist> artists = artistParser.parseArtists(DUMP_PATH, 10);

        AlbumParser albumParser = new AlbumParser();
        List<Album> albums = albumParser.parsAlbums(DUMP_PATH, artists);

        artists.forEach(artist -> writeArtistJson(artist, ARTISTS_DIRECTORY_PATH));
        albums.forEach(album -> writeAlbumJson(album, ALBUMS_DIRECTORY_PATH));
    }

    private static void writeArtistJson(Artist artist, String directoryPath) {
        try {
            mapper.writeValue(new File(directoryPath + File.separator + artist.getMbid() + JSON_EXTENSION_NAME), artist);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeAlbumJson(Album album, String directoryPath) {
        try {
            mapper.writeValue(new File(directoryPath + File.separator + album.getMbid() + JSON_EXTENSION_NAME), album);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
