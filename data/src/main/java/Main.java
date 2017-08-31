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
    private static final String JSON_EXTENSION_NAME = ".json";

    private static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        if (args.length < 2){
            System.out.println("Required 2 arguments: DumpPath and ResultDirectory");
            return;
        }

        String dumpPath = args[0];
        String artistDirectoryPath = args[1] + File.separator + "artists";
        String albumDirectoryPath = args[1] + File.separator + "albums";

        ArtistParser artistParser = new ArtistParser();
        List<Artist> artists = artistParser.parseArtists(dumpPath, 10);

        AlbumParser albumParser = new AlbumParser();
        List<Album> albums = albumParser.parsAlbums(dumpPath, artists);

        artists.forEach(artist -> writeArtistJson(artist, artistDirectoryPath));
        albums.forEach(album -> writeAlbumJson(album, albumDirectoryPath));
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
