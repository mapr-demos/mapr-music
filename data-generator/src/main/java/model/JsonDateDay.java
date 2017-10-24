package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonDateDay {

    @JsonProperty("$dateDay")
    private String $dateDay;

    public JsonDateDay(String dateDayString) {
        this.$dateDay = dateDayString;
    }

    public String get$dateDay() {
        return $dateDay;
    }

    public void set$dateDay(String $dateDay) {
        this.$dateDay = $dateDay;
    }
}
