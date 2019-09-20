package org.zaproxy.zap.scan.filters;

import java.util.LinkedHashSet;
import java.util.Set;

/** @author KSASAN preetkaran20@gmail.com */
public class TagFilterBean {

    private Set<String> tags = new LinkedHashSet<>();

    private FilterCriteria filterCriteria;

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public FilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(FilterCriteria filterCriteria) {
        this.filterCriteria = filterCriteria;
    }
}
