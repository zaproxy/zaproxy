/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2020 The ZAP Development Team
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
package org.zaproxy.zap.extension.pscan;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;
import java.util.List;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.network.HttpMessage;
import org.parosproxy.paros.network.HttpStatusCode;
import org.zaproxy.zap.WithConfigsTest;
import org.zaproxy.zap.extension.custompages.CustomPage;
import org.zaproxy.zap.extension.users.ContextUserAuthManager;
import org.zaproxy.zap.extension.users.ExtensionUserManagement;
import org.zaproxy.zap.model.Context;
import org.zaproxy.zap.model.Tech;
import org.zaproxy.zap.model.TechSet;
import org.zaproxy.zap.users.User;
import org.zaproxy.zap.utils.I18N;

class PassiveScanDataUnitTest extends WithConfigsTest {

    private Session session;
    private Context context;

    @BeforeEach
    void setup() {
        Constant.messages = mock(I18N.class);
        session = mock(Session.class);
        doReturn(session).when(model).getSession();
        context = mock(Context.class);
        PassiveScanData.setExtUserMgmt(null);
    }

    @Test
    void shouldHaveAllTechSetByDefault() {
        // Given / When
        HttpMessage msg = createMessage();
        PassiveScanData psd = new PassiveScanData(msg);
        // Then
        assertThat(psd.getTechSet(), is(equalTo(TechSet.getAllTech())));
    }

    @Test
    void shouldHaveEmptyUserListByDefault() {
        // Given / When
        HttpMessage msg = createMessage();
        PassiveScanData psd = new PassiveScanData(msg);
        // Then
        assertThat(psd.getUsers(), is(equalTo(Collections.emptyList())));
    }

    @Test
    void shouldNotHaveContextByDefault() {
        // Given / When
        HttpMessage msg = createMessage();
        PassiveScanData psd = new PassiveScanData(msg);
        // Then
        assertNull(psd.getContext());
        assertThat(psd.hasContext(), is(equalTo(false)));
    }

    @Test
    void shouldHaveContextIfMessageIncludedInOne() {
        // Given
        HttpMessage msg = createMessage();
        Context context = mock(Context.class);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        // When
        PassiveScanData psd = new PassiveScanData(msg);
        // Then
        assertThat(psd.getContext(), is(equalTo(context)));
        assertThat(psd.hasContext(), is(equalTo(true)));
    }

    @Test
    void shouldUseFirstContextIfMessageApplicableToMultiple() {
        // Given
        HttpMessage msg = createMessage();
        Context matchCtxOne = mock(Context.class);
        Context matchCtxTwo = mock(Context.class);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(matchCtxOne, matchCtxTwo));
        // When
        PassiveScanData psd = new PassiveScanData(msg);
        // Then
        assertThat(psd.getContext(), is(equalTo(matchCtxOne)));
        assertThat(psd.hasContext(), is(equalTo(true)));
    }

    @Test
    void shouldUseTechSetOfFirstMatchedContextIfMessageApplicableToMultiple() {
        // Given
        HttpMessage msg = createMessage();
        Context matchCtxOne = mock(Context.class);
        TechSet expectedTechSet = new TechSet(Tech.Db);
        given(matchCtxOne.getTechSet()).willReturn(expectedTechSet);
        Context matchCtxTwo = mock(Context.class);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(matchCtxOne, matchCtxTwo));
        // When
        PassiveScanData psd = new PassiveScanData(msg);

        // Then
        assertThat(psd.getTechSet(), is(equalTo(expectedTechSet)));
    }

    @Test
    void shouldUseUsersOfFirstMatchedContextIfMessageApplicableToMultiple() {
        // Given
        HttpMessage msg = createMessage();
        int contextId = 3;
        Context matchCtxOne = mock(Context.class);
        given(matchCtxOne.getId()).willReturn(contextId);
        ExtensionUserManagement extUserMgmt = mock(ExtensionUserManagement.class);
        List<User> expectedUsers = asList(mock(User.class), mock(User.class));
        ContextUserAuthManager contextUserAuthManager = mock(ContextUserAuthManager.class);
        given(extUserMgmt.getContextUserAuthManager(contextId)).willReturn(contextUserAuthManager);
        given(contextUserAuthManager.getUsers()).willReturn(expectedUsers);
        PassiveScanData.setExtUserMgmt(extUserMgmt);
        Context matchCtxTwo = mock(Context.class);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(matchCtxOne, matchCtxTwo));
        // When
        PassiveScanData psd = new PassiveScanData(msg);
        // Then
        assertThat(psd.getUsers(), is(equalTo(expectedUsers)));
    }

    @Test
    void shouldCheckPage200WithContext() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage msg = createMessage();
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isPage200(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500);
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isPage200ShouldReturnFalseIfCustomPage404Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage message = createMessage();
        given(context.isCustomPageWithFallback(message, type)).willReturn(true);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage200(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, type);
    }

    @Test
    void isPage200ShouldReturnFalseIfCustomPage500Matches() {
        // Given
        HttpMessage message = createMessage();
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(true);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage200(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
    }

    @Test
    void isPage200ShouldReturnFalseIfNeitherCustomPage200NorStatusCodeMatch() {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage200(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void isPage200ShouldReturnTrueIfCustomPage200DoesNotMatchButStatusCodeDoes() {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(200);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage200(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void shouldCheckPage404WithContext() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage msg = createMessage();
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isPage404(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.OK_200);
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isPage404ShouldReturnFalseIfCustomPage200Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage message = createMessage();
        given(context.isCustomPageWithFallback(message, type)).willReturn(true);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage404(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, type);
    }

    @Test
    void isPage404houldReturnFalseIfCustomPage500Matches() {
        // Given
        HttpMessage message = createMessage();
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(true);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage404(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
    }

    @Test
    void isPage404ShouldReturnFalseIfNeitherCustomPage404NorStatusCodeMatch() {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage404(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void isPage404ShouldReturnTrueIfCustomPage404DoesNotMatchButStatusCodeDoes() {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(404);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage404(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void shouldCheckPage500WithContext() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage msg = createMessage();
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isPage500(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.OK_200);
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isPage500ShouldReturnFalseIfCustomPage200Matches() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage message = createMessage();
        given(context.isCustomPageWithFallback(message, type)).willReturn(true);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage500(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, type);
    }

    @Test
    void isPage500houldReturnFalseIfCustomPage404Matches() {
        // Given
        HttpMessage message = createMessage();
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(true);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage500(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
    }

    @Test
    void isPage500ShouldReturnFalseIfNeitherCustomPage500NorStatusCodeMatch() {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage500(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void isPage500ShouldReturnTrueIfCustomPage500DoesNotMatchButStatusCodeDoes() {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(500);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.ERROR_500))
                .willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPage500(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.NOTFOUND_404);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.ERROR_500);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void shouldCheckPageOtherWithContext() {
        // Given
        CustomPage.Type type = CustomPage.Type.OTHER;
        HttpMessage msg = createMessage();
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isPageOther(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void shouldCheckPageAuthIssueWithContext() {
        // Given
        CustomPage.Type type = CustomPage.Type.AUTH_4XX;
        HttpMessage msg = createMessage();
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isPageAuthIssue(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.OK_200);
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isPageAuthIssueShouldReturnFalseIfCustomPage200Matches() {
        // Given
        HttpMessage message = createMessage();
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(true);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPageAuthIssue(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void isPageAuthIssueShouldReturnFalseIfNeitherCustomPageAuthIssueNorStatusCodeMatch() {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.AUTH_4XX))
                .willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPageAuthIssue(message);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.AUTH_4XX);
    }

    @ParameterizedTest
    @ValueSource(ints = {HttpStatusCode.UNAUTHORIZED, HttpStatusCode.FORBIDDEN})
    void isPageAuthIssueShouldReturnTrueIfCustomPageAuthIssueDoesNotMatchButStatusCodeDoes(
            int statusCode) {
        // Given
        HttpMessage message = createMessage();
        message.getResponseHeader().setStatusCode(statusCode);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(message, CustomPage.Type.AUTH_4XX))
                .willReturn(false);
        given(session.getContextsForUrl(message.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(message);
        // When
        boolean result = psd.isPageAuthIssue(message);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.AUTH_4XX);
        verify(context).isCustomPageWithFallback(message, CustomPage.Type.OK_200);
    }

    @Test
    void isSuccessShouldReturnTrueIfStatusCodeMatches() {
        // Given
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(200);
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isSuccess(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verifyNoInteractions(context);
    }

    @Test
    void isSuccessShouldReturnTrueIfCustomPage200ButStatusCodeDoesNotMatch() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(403);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isSuccess(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isSuccessShouldReturnFalseIfNeitherCustomPage200NorStatusCodeMatch() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(403);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isSuccess(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isSuccessShouldReturnFalseIfNeitherCustomPage200NorStatusCodeMatchButCustomPage404Does() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isSuccess(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isSuccessShouldReturnFalseIfNeitherCustomPage200NorStatusCodeMatchButCustomPage500Does() {
        // Given
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isSuccess(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500);
    }

    @Test
    void isClientErrorShouldReturnTrueIfStatusCodeMatches() {
        // Given
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(403);
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isClientError(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verifyNoInteractions(context);
    }

    @Test
    void isClientErrorShouldReturnTrueIfCustomPage404ButStatusCodeDoesNotMatch() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(200);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isClientError(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isClientErrorShouldReturnFalseIfNeitherCustomPage404NorStatusCodeMatch() {
        // Given
        CustomPage.Type type = CustomPage.Type.NOTFOUND_404;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(200);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isClientError(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void
            isClientErrorShouldReturnFalseIfNeitherCustomPage404NorStatusCodeMatchButCustomPage200Does() {
        // Given
        CustomPage.Type type = CustomPage.Type.OK_200;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isClientError(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void
            isClientErrorShouldReturnFalseIfNeitherCustomPage404NorStatusCodeMatchButCustomPage500Does() {
        // Given
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isClientError(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.ERROR_500);
    }

    @Test
    void isServerErrorShouldReturnTrueIfStatusCodeMatches() {
        // Given
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(503);
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isServerError(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verifyNoInteractions(context);
    }

    @Test
    void isServerErrorShouldReturnTrueIfCustomPage500MatchesButStatusCodeDoesNotMatch() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(200);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isServerError(msg);
        // Then
        assertThat(result, is(equalTo(true)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void isServerErrorShouldReturnFalseIfNeitherCustomPage500ButNorStatusCodeMatch() {
        // Given
        CustomPage.Type type = CustomPage.Type.ERROR_500;
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(200);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404))
                .willReturn(false);
        given(context.isCustomPageWithFallback(msg, type)).willReturn(false);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isServerError(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, type);
    }

    @Test
    void
            isServerErrorShouldReturnFalseIfNeitherCustomPage500ButNorStatusCodeMatchButCustomPage200Does() {
        // Given
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isServerError(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.OK_200);
    }

    @Test
    void
            isServerErrorShouldReturnFalseIfNeitherCustomPage500NorStatusCodeMatchButCustomPage404Does() {
        // Given
        HttpMessage msg = createMessage();
        msg.getResponseHeader().setStatusCode(302);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.OK_200)).willReturn(false);
        given(context.isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404)).willReturn(true);
        given(session.getContextsForUrl(msg.getRequestHeader().getURI().toString()))
                .willReturn(asList(context));
        PassiveScanData psd = new PassiveScanData(msg);
        // When
        boolean result = psd.isServerError(msg);
        // Then
        assertThat(result, is(equalTo(false)));
        verify(context).isCustomPageWithFallback(msg, CustomPage.Type.NOTFOUND_404);
    }

    private static HttpMessage createMessage() {
        HttpMessage message = new HttpMessage();
        try {
            message.getRequestHeader().setURI(new URI("https://example.com", false));
        } catch (URIException | NullPointerException e) {
            // Ignore
        }
        return message;
    }
}
