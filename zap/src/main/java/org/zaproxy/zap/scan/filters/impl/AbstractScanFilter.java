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
package org.zaproxy.zap.scan.filters.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import org.zaproxy.zap.scan.filters.FilterResult;
import org.zaproxy.zap.scan.filters.GenericFilterBean;
import org.zaproxy.zap.scan.filters.GenericFilterUtility;
import org.zaproxy.zap.scan.filters.ScanFilter;

/** @author KSASAN preetkaran20@gmail.com */
public abstract class AbstractScanFilter<T> implements ScanFilter {
    private Collection<GenericFilterBean<T>> genericFilterBeans = new LinkedHashSet<>();

    public Collection<GenericFilterBean<T>> getGenericFilterBeans() {
        return genericFilterBeans;
    }

    public void setGenericFilterBeans(Collection<GenericFilterBean<T>> tagFilterBeans) {
        this.genericFilterBeans.addAll(tagFilterBeans);
    }

    public FilterResult isFiltered(Collection<T> values) {
        return GenericFilterUtility.isFiltered(genericFilterBeans, values, this.getFilterType());
    }

    public FilterResult isFiltered(T value) {
        return GenericFilterUtility.isFiltered(genericFilterBeans, value, this.getFilterType());
    }
}
