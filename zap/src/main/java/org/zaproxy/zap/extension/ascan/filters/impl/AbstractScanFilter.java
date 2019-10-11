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
package org.zaproxy.zap.extension.ascan.filters.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import org.zaproxy.zap.extension.ascan.filters.FilterResult;
import org.zaproxy.zap.extension.ascan.filters.GenericFilterData;
import org.zaproxy.zap.extension.ascan.filters.GenericFilterUtility;
import org.zaproxy.zap.extension.ascan.filters.ScanFilter;

/**
 * Abstract ScanFilter for handling generic filter usecases.
 *
 * @author KSASAN preetkaran20@gmail.com
 * @since 2.9.0
 */
public abstract class AbstractScanFilter<T> implements ScanFilter {
    private Collection<GenericFilterData<T>> genericFilterDataCollection = new LinkedHashSet<>();

    public abstract String getFilterType();

    public Collection<GenericFilterData<T>> getGenericFilterDataCollection() {
        return genericFilterDataCollection;
    }

    public void setGenericFilterDataCollection(
            Collection<GenericFilterData<T>> genericFilterDataCollection) {
        this.genericFilterDataCollection.addAll(genericFilterDataCollection);
    }

    public FilterResult isFiltered(Collection<T> values) {
        return GenericFilterUtility.isFiltered(
                genericFilterDataCollection, values, this.getFilterType());
    }

    public FilterResult isFiltered(T value) {
        return GenericFilterUtility.isFiltered(
                genericFilterDataCollection, value, this.getFilterType());
    }
}
