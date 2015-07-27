package org.zaproxy.zap.model;

import org.junit.Test;
import org.parosproxy.paros.Constant;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SessionUtilsUnitTest {

    private static final String ZAP_HOME = "/zap/";
    private static final String ZAP_SESSION_DIR = ZAP_HOME + Constant.FOLDER_SESSION_DEFAULT;

    @Test
    public void shouldRetrieveExistingSessionFileFromAbsolutePath() throws Exception {
        // Given
        String session = "/test.session";
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(sessionPath, is(equalTo(Paths.get("/test.session"))));
    }

    @Test
    public void shouldAppendSessionFiletypeAndRetrieveSessionFileFromAbsolutePath() throws Exception {
        // Given
        String session = "/test";
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(sessionPath, is(equalTo(Paths.get("/test.session"))));
    }

    @Test
    public void shouldRetrieveExistingSessionFileFromRelativePath() throws Exception {
        // Given
        Constant.setZapHome(ZAP_HOME);
        String session = "test.session";
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(sessionPath, is(equalTo(Paths.get(ZAP_SESSION_DIR, "test.session"))));
    }

    @Test
    public void shouldAppendSessionFiletypeAndRetrieveSessionFileFromRelativePath() throws Exception {
        // Given
        Constant.setZapHome(ZAP_HOME);
        String session = "test";
        // When
        Path sessionPath = SessionUtils.getSessionPath(session);
        // Then
        assertThat(sessionPath, is(equalTo(Paths.get(ZAP_SESSION_DIR, "test.session"))));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullForSessionInput() throws Exception {
        SessionUtils.getSessionPath(null);
    }

}