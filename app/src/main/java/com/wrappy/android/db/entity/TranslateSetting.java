package com.wrappy.android.db.entity;

/**
 *
 */
public class TranslateSetting {
    private boolean isAutoTranslate;
    private String language;

    public boolean isAutoTranslate() {
        return isAutoTranslate;
    }

    public void setAutoTranslate(boolean autoTranslate) {
        isAutoTranslate = autoTranslate;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
