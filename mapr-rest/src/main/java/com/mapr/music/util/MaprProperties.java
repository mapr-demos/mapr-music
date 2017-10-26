package com.mapr.music.util;

public final class MaprProperties {

    private MaprProperties() {
    }

    public static final String MAPR_USER_NAME = getOrDefault("MAPR_USER_NAME", "mapr");
    public static final String MAPR_USER_GROUP = getOrDefault("MAPR_USER_GROUP", "mapr");

    public static final String THREAD_FACTORY = "java:jboss/ee/concurrency/factory/MaprMusicThreadFactory";
    public static final String DRILL_DATA_SOURCE = "java:/datasources/mapr-music-drill";

    public static final String ALBUMS_TABLE_NAME = "/apps/albums";
    public static final String ARTISTS_TABLE_NAME = "/apps/artists";
    public static final String ALBUMS_RATINGS_TABLE_NAME = "/apps/albums_ratings";
    public static final String ARTISTS_RATINGS_TABLE_NAME = "/apps/artists_ratings";
    public static final String LANGUAGES_TABLE_NAME = "/apps/languages";
    public static final String RECOMMENDATIONS_TABLE_NAME = "/apps/recommendations";
    public static final String STATISTICS_TABLE_NAME = "/apps/statistics";
    public static final String USERS_TABLE_NAME = "/apps/users";

    public static final String ARTISTS_CHANGE_LOG = getOrDefault("ARTISTS_CHANGE_LOG", "/apps/mapr_music_changelog:artists");
    public static final String ALBUMS_CHANGE_LOG = getOrDefault("ALBUMS_CHANGE_LOG", "/apps/mapr_music_changelog:albums");

    public static final String ES_REST_HOST = getOrDefault("ELASTIC_SEARCH_REST_HOST", "localhost");
    public static final int ES_REST_PORT = getOrDefault("ELASTIC_SEARCH_REST_PORT", 9200);
    public static final String ES_ALBUMS_INDEX = getOrDefault("ES_ALBUMS_INDEX", "albums");
    public static final String ES_ALBUMS_TYPE = getOrDefault("ES_ALBUMS_TYPE", "album");
    public static final String ES_ARTISTS_INDEX = getOrDefault("ES_ARTISTS_INDEX", "artists");
    public static final String ES_ARTISTS_TYPE = getOrDefault("ES_ARTISTS_TYPE", "artist");


    public static String getOrDefault(String envName, String defaultValue) {
        String environmentValue = System.getenv(envName);
        return (environmentValue != null) ? environmentValue : defaultValue;
    }

    public static int getOrDefault(String env, int defaultValue) {

        String environmentValue = System.getenv(env);
        if (environmentValue == null) {
            return defaultValue;
        }

        Integer environmentIntegerValue = null;
        try {
            environmentIntegerValue = Integer.valueOf(environmentValue);
        } catch (NumberFormatException e) {
            // value is not numeric
            e.printStackTrace();
        }

        return (environmentIntegerValue != null) ? environmentIntegerValue : defaultValue;
    }

}
