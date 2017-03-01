package org.zaproxy.zap.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.parosproxy.paros.Constant;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SessionUtilsUnitTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

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
    public void shouldAppendSessionFiletypeAndRetrieveSessionFileFromAbsolutePath() throws Exception {
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
        assertThat(sessionPath, is(equalTo(pathWith(zapHome, Constant.FOLDER_SESSION_DEFAULT, "test.session"))));
    }

    @Test
    public void shouldAppendSessionFiletypeAndRetrieveSessionFileFromRelativePath() throws Exception {
        // Given
        String zapHome = createZapHome();
        String session = "test";
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(sessionPath, is(equalTo(pathWith(zapHome, Constant.FOLDER_SESSION_DEFAULT, "test.session"))));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullForSessionInput() throws Exception {
        SessionUtils.getSessionPath(null);
    }

    private Path newFile(String name) throws IOException {
        return tempFolder.newFile(name).toPath();
    }

    private Path pathWith(String baseDir, String... paths) {
        return Paths.get(baseDir, paths);
    }

    private String createZapHome() throws IOException {
        String zapHome = tempFolder.newFolder("zap").toPath().toString();
        Constant.setZapHome(zapHome);
        return zapHome;
    }

}