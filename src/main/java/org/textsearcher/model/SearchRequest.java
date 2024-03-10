package org.textsearcher.model;

import java.util.List;

public class SearchRequest {
    private String url;
    private List<String> searchStrings;

    private Boolean isCaseSensitive;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getSearchStrings() {
        return searchStrings;
    }

    public void setSearchStrings(List<String> searchStrings) {
        this.searchStrings = searchStrings;
    }

    public Boolean getCaseSensitive() {
        return isCaseSensitive;
    }

    public void setCaseSensitive(Boolean caseSensitive) {
        isCaseSensitive = caseSensitive;
    }
}
