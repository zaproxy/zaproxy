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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.parosproxy.paros.model.HistoryReference;
import org.zaproxy.zap.model.StructuralNode;
import org.zaproxy.zap.scan.filters.FilterCriteria;
import org.zaproxy.zap.scan.filters.ScanFilter;
import org.zaproxy.zap.scan.filters.TagFilterBean;

/** @author KSASAN preetkaran20@gmail.com */
public class TagScanFilter implements ScanFilter {

    private Set<TagFilterBean> tagFilterBeans = new LinkedHashSet<>();

    public Set<TagFilterBean> getTagFilterBeans() {
        return tagFilterBeans;
    }

    public void setTagFilterBeans(Set<TagFilterBean> tagFilterBeans) {
        this.tagFilterBeans = tagFilterBeans;
    }

    @Override
    public boolean isFiltered(StructuralNode node) {
        HistoryReference href = node.getHistoryReference();
        if (href != null) {
            List<String> nodeTags = href.getTags();
            for (TagFilterBean tagFilterBean : tagFilterBeans) {
                FilterCriteria filterCriteria = tagFilterBean.getFilterCriteria();
                switch (filterCriteria) {
                    case INCLUDE:
                        for (String tag : nodeTags) {
                            if (tagFilterBean.getTags().contains(tag)) {
                                return true;
                            }
                        }
                    case EXCLUDE:
                        for (String tag : nodeTags) {
                            if (!tagFilterBean.getTags().contains(tag)) {
                                return false;
                            }
                        }
                    case INCLUDE_ALL:
                        return nodeTags.containsAll(tagFilterBean.getTags());
                    default:
                        return true;
                }
            }
            return true;
        } else {
            return true;
        }
    }
}
