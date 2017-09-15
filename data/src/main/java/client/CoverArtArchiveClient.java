package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static client.CoverArtArchiveResponse.Image.FRONT_TYPE;

public class CoverArtArchiveClient {

    private static final String RELEASE_COVERS_ENDPOINT = "http://coverartarchive.org/release/";

    private static final ObjectMapper mapper = new ObjectMapper();

    private String releaseId;
    private CoverArtArchiveResponse coverArtResponse;
    private boolean available;

    public CoverArtArchiveClient(String releaseId) {
        this.releaseId = releaseId;
        this.available = true;
    }

    public String getCoverImage() {

        ensureApiCalled();
        if (coverArtResponse == null) {
            return null;
        }

        Optional<CoverArtArchiveResponse.Image> cover = coverArtResponse.getImages().stream()
                .filter(image -> StringUtils.isNotEmpty(image.getImage()))
                .filter(image -> image.getTypes().contains(FRONT_TYPE) || image.isFront())
                .findAny();

        if (cover.isPresent()) {
            return cover.get().getImage();
        }

        Optional<CoverArtArchiveResponse.Image> arbitrary = coverArtResponse.getImages().stream()
                .filter(image -> StringUtils.isNotEmpty(image.getImage()))
                .findAny();

        return (arbitrary.isPresent()) ? arbitrary.get().getImage() : null;

    }

    public List<String> getImages() {

        ensureApiCalled();
        if (coverArtResponse == null) {
            return Collections.emptyList();
        }

        String coverImageUrl = getCoverImage();

        return coverArtResponse.getImages().stream()
                .map(CoverArtArchiveResponse.Image::getImage)
                .filter(StringUtils::isNotEmpty)
                .filter(image -> !image.equals(coverImageUrl))
                .collect(Collectors.toList());
    }

    public boolean isImagesExist() {

        ensureApiCalled();
        return coverArtResponse != null;
    }

    private void ensureApiCalled() {

        if (!this.available) {
            return;
        }

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet getRequest = new HttpGet(RELEASE_COVERS_ENDPOINT + releaseId);
        getRequest.addHeader("accept", "application/json");

        try {

            HttpResponse response = httpClient.execute(getRequest);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                this.available = false;
                return;
            }

            this.coverArtResponse = mapper.readValue(response.getEntity().getContent(), CoverArtArchiveResponse.class);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
