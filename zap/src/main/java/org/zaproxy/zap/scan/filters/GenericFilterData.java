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
package org.zaproxy.zap.scan.filters;

import java.util.Collection;
import java.util.LinkedHashSet;
import org.apache.commons.collections.CollectionUtils;

/**
 * Generic Filter Data which can be applied to any Class Object which implements hashCode and equals
 *
 * @author KSASAN preetkaran20@gmail.com
 * @param <T>
 */
public class GenericFilterData<T> {

    private Collection<T> values = new LinkedHashSet<>();

    private FilterCriteria filterCriteria;

    public Collection<T> getValues() {
        return values;
    }

    public void setValues(Collection<T> tags) {
        if (CollectionUtils.isEmpty(values)) {
            this.values.addAll(tags);
        }
    }

    public FilterCriteria getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(FilterCriteria filterCriteria) {
        this.filterCriteria = filterCriteria;
    }
}
