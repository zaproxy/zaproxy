/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2022 The ZAP Development Team
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
package org.zaproxy.zap.extension.ascan;

public class ActiveScanProcessedCellItem implements Comparable<ActiveScanProcessedCellItem> {

    private final boolean successful;
    private final String label;

    public ActiveScanProcessedCellItem(boolean successful, String label) {
        this.successful = successful;
        this.label = label;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public int hashCode() {
        return 31 * (successful ? 1231 : 1237);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActiveScanProcessedCellItem other = (ActiveScanProcessedCellItem) obj;
        if (successful != other.successful) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(ActiveScanProcessedCellItem other) {
        if (other == null) {
            return 1;
        }
        if (successful && !other.successful) {
            return 1;
        } else if (!successful && other.successful) {
            return -1;
        }
        return label.compareTo(other.label);
    }
}
