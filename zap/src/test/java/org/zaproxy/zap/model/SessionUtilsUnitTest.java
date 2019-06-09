/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2014 The ZAP Development Team
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
package org.zaproxy.zap.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.parosproxy.paros.Constant;

public class SessionUtilsUnitTest {

    @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        tempFolder.create();
    }

    @Test
    public void shouldRetrieveExistingSessionFileFromAbsolutePath() throws Exception {
        // Given
        Path path = newFile("test.session");
        String session = path.toString();
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(sessionPath, is(equalTo(path)));
    }

    @Test
    public void shouldAppendSessionFiletypeAndRetrieveSessionFileFromAbsolutePath()
            throws Exception {
        // Given
        Path path = newFile("test.session");
        String session = path.toString().replace(".session$", "");
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(sessionPath, is(equalTo(path)));
    }

    @Test
    public void shouldRetrieveExistingSessionFileFromRelativePath() throws Exception {
        // Given
        String zapHome = createZapHome();
        String session = "test.session";
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(
                sessionPath,
                is(equalTo(pathWith(zapHome, Constant.FOLDER_SESSION_DEFAULT, "test.session"))));
    }

    @Test
    public void shouldAppendSessionFiletypeAndRetrieveSessionFileFromRelativePath()
            throws Exception {
        // Given
        String zapHome = createZapHome();
        String session = "test";
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(
                sessionPath,
                is(equalTo(pathWith(zapHome, Constant.FOLDER_SESSION_DEFAULT, "test.session"))));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullForSessionInput() throws Exception {
        SessionUtils.getSessionPath(null);
    }

    private Path newFile(String name) throws IOException {
        return tempFolder.newFile(name).toPath();
    }

    private static Path pathWith(String baseDir, String... paths) {
        return Paths.get(baseDir, paths);
    }

    private String createZapHome() throws IOException {
        String zapHome = tempFolder.newFolder("zap").toPath().toString();
        Constant.setZapHome(zapHome);
        return zapHome;
    }
}
