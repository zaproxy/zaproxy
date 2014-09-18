package org.zaproxy.zap.model;

import org.junit.Test;

import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class SessionUtilsUnitTest {

    @Test
    public void shouldRetrieveExistingSessionFile() throws Exception {
        String session = "test.session";

        assertThat(SessionUtils.getSessionPath(session), is(equalTo(Paths.get("test.session"))));
    }

    @Test
    public void shouldAppendSessionFiletypeAndRetrieveSessionFile() throws Exception {
        String session = "test";

        assertThat(SessionUtils.getSessionPath(session), is(equalTo(Paths.get("test.session"))));
    }

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullForSessionInput() throws Exception {
        SessionUtils.getSessionPath(null);
    }

}