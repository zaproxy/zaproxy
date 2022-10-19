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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.apache.commons.collections.CollectionUtils;
import org.zaproxy.zap.extension.ascan.filters.impl.AbstractGenericScanFilter;

/**
 * Generic Filter Utility Class.
 *
 * @author KSASAN preetkaran20@gmail.com
 */
public class GenericFilterUtility {

    /**
     * @param <T> Generic Include/Exclude Criteria List
     * @param <R> Generic Type which extends AbstractScanFilter
     * @param incList Include Criteria List
     * @param excList Exclude Criteria List
     * @param constructor of the ScanFilter to be populated
     * @return List of {@code <R>} type
     */
    public static <T, V, R extends AbstractGenericScanFilter<T, V>> List<R> createScanFilter(
            List<T> incList, List<T> excList, Supplier<R> constructor) {
        List<R> scanFilterList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(incList)) {
            R abstractScanFilter = constructor.get();
            abstractScanFilter.setFilterCriteria(FilterCriteria.INCLUDE);
            abstractScanFilter.getFilterData().addAll(incList);
            scanFilterList.add(abstractScanFilter);
        }

        if (!CollectionUtils.isEmpty(excList)) {
            R abstractScanFilter = constructor.get();
            abstractScanFilter.setFilterCriteria(FilterCriteria.EXCLUDE);
            abstractScanFilter.getFilterData().addAll(excList);
            scanFilterList.add(abstractScanFilter);
        }
        return scanFilterList;
    }
}
