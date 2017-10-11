package com.mapr.music.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.music.annotation.MaprDbTable;

import static com.mapr.music.util.MaprProperties.LANGUAGES_TABLE_NAME;

/**
 * Model class, which represents 'Language' document stored in MapR DB.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable(LANGUAGES_TABLE_NAME)
public class Language {

    @JsonProperty("_id")
    private String id;
    private String name;

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
}
