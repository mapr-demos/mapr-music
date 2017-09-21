package com.mapr.elasticsearch.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface SearchService {

    JsonNode search(JsonNode query);
}
