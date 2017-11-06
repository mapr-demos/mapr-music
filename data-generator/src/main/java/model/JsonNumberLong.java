package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonNumberLong {

    @JsonProperty("$numberLong")
    private Long $numberLong;

    public JsonNumberLong(long $numberLong) {
        this.$numberLong = $numberLong;
    }

    public JsonNumberLong(int $numberLong) {
        this.$numberLong = Long.valueOf($numberLong);
    }

    public Long get$numberLong() {
        return $numberLong;
    }

    public void set$numberLong(Long $numberLong) {
        this.$numberLong = $numberLong;
    }
}
