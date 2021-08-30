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
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.zaproxy.zap.GitHubUser;

/** A task that creates a tag and a GitHub release. */
public abstract class CreateTagAndGitHubRelease extends CreateGitHubRelease {

    private static final String GITHUB_BASE_URL = "https://github.com/";

    private static final String GIT_REMOTE_ORIGIN = "origin";

    public CreateTagAndGitHubRelease() {
        setDescription("Creates a tag and GitHub release.");
    }

    @Input
    public abstract Property<String> getTagMessage();

    @Override
    public void createRelease() throws IOException {
        try {
            createTag();
        } catch (Exception e) {
            throw new BuildException(e);
        }

        super.createRelease();
    }

    private void createTag() throws Exception {
        Repository repository =
                new FileRepositoryBuilder()
                        .setGitDir(new File(getProject().getRootDir(), ".git"))
                        .build();
        try (Git git = new Git(repository)) {
            URIish originUri = new URIish(GITHUB_BASE_URL + getRepo().get());
            git.remoteSetUrl().setRemoteName(GIT_REMOTE_ORIGIN).setRemoteUri(originUri).call();

            GitHubUser ghUser = getUser().get();
            PersonIdent personIdent = new PersonIdent(ghUser.getName(), ghUser.getEmail());
            Ref tag =
                    git.tag()
                            .setName(getTag().get())
                            .setMessage(getTagMessage().get())
                            .setAnnotated(true)
                            .setTagger(personIdent)
                            .call();

            git.push()
                    .setCredentialsProvider(
                            new UsernamePasswordCredentialsProvider(
                                    ghUser.getName(), ghUser.getAuthToken()))
                    .add(tag)
                    .call();
        }
    }
}
