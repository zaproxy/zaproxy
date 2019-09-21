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
import java.util.Set;

/**
 * @author KSASAN preetkaran20@gmail.com
 *     <p>Generic Filter Class of handling most of the cases.
 * @param <T>
 */
public class GenericFilterUtility {

    /**
     * @param <T>
     * @param genericFilterBeans
     * @param nodeValues
     * @return boolean
     *     <p>this Utility is Generic Utility for various criterias and can be used by any Filter
     *     which is based on classes which are implementing hashCode and equals
     */
    public static <T> boolean isFiltered(
            Collection<GenericFilterBean<T>> genericFilterBeans, Collection<T> nodeValues) {
        for (GenericFilterBean<T> tagFilterBean : genericFilterBeans) {
            FilterCriteria filterCriteria = tagFilterBean.getFilterCriteria();
            switch (filterCriteria) {
                case INCLUDE:
                    for (T value : nodeValues) {
                        if (tagFilterBean.getValues().contains(value)) {
                            return true;
                        }
                    }
                case EXCLUDE:
                    for (T value : nodeValues) {
                        if (!tagFilterBean.getValues().contains(value)) {
                            return false;
                        }
                    }
                case INCLUDE_ALL:
                    return nodeValues.containsAll(tagFilterBean.getValues());
                default:
                    return true;
            }
        }
        return true;
    }

    /**
     * @param <T>
     * @param genericFilterBeans
     * @param nodeValue
     * @return boolean
     *     <p>this method will call {@code #isFiltered(Collection, Collection)} method internally so
     *     basic requirement of equals and hasCode for the Classes holds same.
     */
    public static <T> boolean isFiltered(
            Collection<GenericFilterBean<T>> genericFilterBeans, T nodeValue) {
        Set<T> nodeValues = new LinkedHashSet<>();
        nodeValues.add(nodeValue);
        return isFiltered(genericFilterBeans, nodeValues);
    }
}
