package com.mapr.music.dao;

import com.mapr.music.model.Language;

public class LanguageDao extends MaprDbDao<Language> {

    public LanguageDao() {
        super(Language.class);
    }

    /**
     * Updating language documents is not supported.
     */
    @Override
    public Language update(String id, Language entity) {
        throw new UnsupportedOperationException("Language updating is not supported");
    }
}
