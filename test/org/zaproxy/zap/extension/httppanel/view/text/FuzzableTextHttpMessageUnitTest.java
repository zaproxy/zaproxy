package org.zaproxy.zap.extension.httppanel.view.text;

import org.apache.commons.httpclient.URI;
import org.junit.Test;
import org.parosproxy.paros.network.HttpMessage;
import org.zaproxy.zap.extension.httppanel.Message;
import org.zaproxy.zap.extension.httppanel.view.FuzzableMessage;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.zaproxy.zap.extension.httppanel.view.text.FuzzableTextHttpMessage.Location.BODY;
import static org.zaproxy.zap.extension.httppanel.view.text.FuzzableTextHttpMessageBuilder.aFuzzableTextHttpMessage;

public class FuzzableTextHttpMessageUnitTest {

    FuzzableMessage fuzzableMessage;

    @Test(expected = NullPointerException.class)
    public void shouldFailOnNullInputFuzzString() throws Exception {
        fuzzableMessage = aFuzzableTextHttpMessage().build();
        fuzzableMessage.fuzz(null);
    }

    @Test
    public void shouldReplacePositionInRequestBodyWithFuzzString() throws Exception {
        HttpMessage httpMessage = new HttpMessage(new URI("localhost", false));
        httpMessage.setRequestBody("Fuzzing THIS");
        fuzzableMessage = aFuzzableTextHttpMessage().withFuzzLocation(BODY).withHttpMessage(httpMessage).withFuzzingFromToPosition(8, 11).build();

        Message fuzzedMessage = fuzzableMessage.fuzz("TEST");

        assertThat(fuzzedMessage, instanceOf(HttpMessage.class));
        String fuzzedBody = ((HttpMessage) fuzzedMessage).getRequestBody().toString();

        assertThat(fuzzedBody, containsString("TEST"));
        assertThat(fuzzedBody, not(containsString("THIS")));
    }


}
