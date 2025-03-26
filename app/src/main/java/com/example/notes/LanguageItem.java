package com.example.notes;

public class LanguageItem {
    private String languageName;
    private String languageCode;
    private String language;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public LanguageItem(String languageName, String languageCode, String language) {
        this.languageName = languageName;
        this.languageCode = languageCode;
        this.language = language;
    }


    // Getters and Setters
    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }
}
