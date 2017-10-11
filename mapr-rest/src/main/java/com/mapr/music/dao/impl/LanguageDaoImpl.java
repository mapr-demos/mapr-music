package com.mapr.music.dao.impl;

import com.mapr.music.dao.LanguageDao;
import com.mapr.music.model.Language;

import java.util.List;

public class LanguageDaoImpl extends MaprDbDaoImpl<Language> implements LanguageDao {

    public LanguageDaoImpl() {
        super(Language.class);
    }

    @Override
    public List<Language> getList() {
        return super.getList();
    }

    @Override
    public Language update(String id, Language entity) {
        throw new UnsupportedOperationException("Language updating is not supported");
    }
}
