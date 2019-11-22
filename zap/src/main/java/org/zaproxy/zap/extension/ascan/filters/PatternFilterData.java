/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2019 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.zap.extension.ascan.filters;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** @author KSASAN preetkaran20@gmail.com */
public class PatternFilterData {

    private Set<Pattern> patterns = new LinkedHashSet<>();

    private FilterCriteria filterCriteria;

    public PatternFilterData(FilterCriteria filterCriteria, Collection<Pattern> urlPatterns) {
        this.setFilterCriteria(filterCriteria);
        this.setPatterns(urlPatterns);
    }

    public Set<Pattern> getPatterns() {
        return patterns;
    }

    private void setPatterns(Collection<Pattern> patterns) {
        Objects.requireNonNull(patterns);
        this.patterns.addAll(patterns);
    }

    public FilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    private void setFilterCriteria(FilterCriteria filterCriteria) {
        this.filterCriteria = filterCriteria;
    }
}
