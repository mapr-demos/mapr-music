package com.mapr.music.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mapr.music.annotation.MaprDbTable;

/**
 * Represents MapR Statistic related tot the separate table.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@MaprDbTable("/apps/statistics")
public class Statistic {

    /**
     * Natural primary key, which is represents MapR-DB table name.
     */
    @JsonProperty("_id")
    private String tableName;

    @JsonProperty("document_number")
    private Long documentNumber;

    public Statistic() {
    }

    public Statistic(String tableName, long documentNumber) {
        this.tableName = tableName;
        this.documentNumber = documentNumber;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(Long documentNumber) {
        this.documentNumber = documentNumber;
    }
}
