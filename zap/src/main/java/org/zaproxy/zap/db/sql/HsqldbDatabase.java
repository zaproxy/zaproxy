/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2015 The ZAP Development Team
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
package org.zaproxy.zap.db.sql;

import java.io.File;

public class HsqldbDatabase extends SqlDatabase {

    public HsqldbDatabase() {
        super();
    }

    @Override
    public void deleteSession(String sessionName) {
        super.deleteSession(sessionName);
        getLogger().debug("deleteSession {}", sessionName);

        deleteDbFile(new File(sessionName));
        deleteDbFile(new File(sessionName + ".data"));
        deleteDbFile(new File(sessionName + ".script"));
        deleteDbFile(new File(sessionName + ".properties"));
        deleteDbFile(new File(sessionName + ".backup"));
        deleteDbFile(new File(sessionName + ".lobs"));
    }

    private void deleteDbFile(File file) {
        getLogger().debug("Deleting {}", file.getAbsolutePath());
        if (file.exists()) {
            if (!file.delete()) {
                getLogger().error("Failed to delete {}", file.getAbsolutePath());
            }
        }
    }

    @Override
    protected SqlDatabaseServer createDatabaseServer(String path) throws Exception {
        return new HsqldbDatabaseServer(path);
    }

    @Override
    public void close(boolean compact, boolean cleanup) {
        getLogger().debug("close");
        super.close(compact, cleanup);
        if (this.getDatabaseServer() == null) {
            return;
        }

        try {
            // shutdown
            ((HsqldbDatabaseServer) this.getDatabaseServer()).shutdown(compact);
        } catch (Exception e) {
            getLogger().error(e.getMessage(), e);
        }
    }

    @Override
    public boolean isFileBased() {
        return true;
    }
}
