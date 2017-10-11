package client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverArtArchiveResponse {

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Image {

        public static final String FRONT_TYPE = "Front";

        List<String> types;
        boolean front;
        boolean back;
        private String image;

        public List<String> getTypes() {
            return (types != null) ? types : Collections.emptyList();
        }

        public void setTypes(List<String> types) {
            this.types = types;
        }

        public boolean isFront() {
            return front;
        }

        public void setFront(boolean front) {
            this.front = front;
        }

        public boolean isBack() {
            return back;
        }

        public void setBack(boolean back) {
            this.back = back;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }

    private List<Image> images;
    private String release;

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }
}
