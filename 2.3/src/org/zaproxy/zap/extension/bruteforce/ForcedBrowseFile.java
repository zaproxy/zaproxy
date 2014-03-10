/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2013 The ZAP Development Team
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.bruteforce;

import java.io.File;

public class ForcedBrowseFile implements Comparable<ForcedBrowseFile> {

    private final File file;

    private final String name;

    public ForcedBrowseFile(File file) {
        super();

        if (file == null) {
            throw new IllegalArgumentException("Parameter file must not be null.");
        }

        this.file = file;
        this.name = file.getName();
    }

    public File getFile() {
        return file;
    }

    @Override
    public int hashCode() {
        return 31 + ((file == null) ? 0 : file.hashCode());
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
        ForcedBrowseFile other = (ForcedBrowseFile) obj;
        if (file == null) {
            if (other.file != null) {
                return false;
            }
        } else if (!file.equals(other.file)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(ForcedBrowseFile o) {
        if (o == null) {
            return 1;
        }
        return file.compareTo(o.file);
    }

}
