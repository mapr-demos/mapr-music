package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Track {

    private String id;
    private String name;
    private JsonNumberLong length;

    @JsonProperty("MBID")
    private String MBID;
    private JsonNumberLong position;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonNumberLong getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = new JsonNumberLong(length);
    }

    public String getMBID() {
        return MBID;
    }

    public void setMBID(String MBID) {
        this.MBID = MBID;
    }

    public JsonNumberLong getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = new JsonNumberLong(position);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("length", length)
                .append("MBID", MBID)
                .append("position", position)
                .build();
    }
}
