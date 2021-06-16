/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.zap.tasks;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/** A task that creates the main release (tag and GitHub release). */
public abstract class CreateMainRelease extends CreateTagAndGitHubRelease {

    public CreateMainRelease() {
        setDescription("Creates the main release.");
    }

    @Override
    public void createRelease() throws IOException {
        if (getTag().get().endsWith("-SNAPSHOT")) {
            getLogger().lifecycle("Ignoring, version is still SNAPSHOT: {}", getTag().get());
            return;
        }

        if (tagExists()) {
            getLogger().lifecycle("Ignoring, tag already exists: {}", getTag().get());
            return;
        }

        super.createRelease();
    }

    private boolean tagExists() throws IOException {
        Repository repository =
                new FileRepositoryBuilder()
                        .setGitDir(new File(getProject().getRootDir(), ".git"))
                        .build();
        return repository.findRef(getTag().get()) != null;
    }
}
