/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.zaproxy.zap.network;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.auth.AuthChallengeParser;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.MalformedChallengeException;

/*
 * The content of this class was copied from org.apache.http.impl.auth.NTLMScheme, HttpComponents Client trunk (revision
 * 1500629).
 * 
 * It was copied because ZAP depends (and uses) Commons HttpClient which is not compatible with, the newer version, 
 * HttpComponents Client.
 * 
 * It's adapted to work with Commons HttpClient.
 * Changes:
 *  - Renamed the class to ZapNTLMScheme to not clash with the name NTLMScheme;
 *  - Moved to "org.zaproxy.zap.network" (instead of keeping it in "org.apache.http.impl.auth");
 *  - Changed to implement org.apache.commons.httpclient.auth.AuthScheme (instead of extending 
 *    org.apache.http.impl.auth.AuthSchemeBase);
 *  - Removed the annotation "@NotThreadSafe";
 *  - Removed the constructor that is not needed, "NTLMScheme(NTLMEngine)", and adapted the default constructor to always use
 *    ZapNTLMEngineImpl;
 *  - Added @Override annotation to methods:
 *     - "public String getSchemeName()";
 *     - "public String getParameter(final String name)";
 *     - "public String getRealm()";
 *     - "public boolean isConnectionBased()";
 *  - Implemented the method "AuthScheme#processChallenge(String challenge)" with the adapted implementation of 
 *    "NTLMScheme#parseChallenge(final CharArrayBuffer buffer,final int beginIndex, final int endIndex)";
 *  - Implemented the method "AuthScheme#authenticate(Credentials credentials, HttpMethod method)" with the adapted 
 *    implementation of "NTLMScheme#authenticate(final Credentials credentials, final HttpRequest request)";
 *  - Changed to use the method "NTCredentials#getHost()" instead of (the replacement) "NTCredentials#getWorkstation()".
 */
/**
 * NTLM is a proprietary authentication scheme developed by Microsoft
 * and optimized for Windows platforms.
 *
 * @since 4.0
 */
public class ZapNTLMScheme implements AuthScheme {

    enum State {
        UNINITIATED,
        CHALLENGE_RECEIVED,
        MSG_TYPE1_GENERATED,
        MSG_TYPE2_RECEVIED,
        MSG_TYPE3_GENERATED,
        FAILED,
    }

    private final ZapNTLMEngineImpl engine;

    private State state;
    private String challenge;

    public ZapNTLMScheme() {
        super();
        this.engine = new ZapNTLMEngineImpl();
        this.state = State.UNINITIATED;
        this.challenge = null;
    }

    @Override
    public String getSchemeName() {
        return "ntlm";
    }

    @Override
    public String getParameter(final String name) {
        // String parameters not supported
        return null;
    }

    @Override
    public String getRealm() {
        // NTLM does not support the concept of an authentication realm
        return null;
    }

    @Override
    public boolean isConnectionBased() {
        return true;
    }

    @Override
    public void processChallenge(String challenge) throws MalformedChallengeException {
        String s = AuthChallengeParser.extractScheme(challenge);
        if (!s.equalsIgnoreCase(getSchemeName())) {
            throw new MalformedChallengeException("Invalid NTLM challenge: " + challenge);
        }
        int i = challenge.indexOf(' ');
        if (i == -1) {
            if (this.state == State.UNINITIATED) {
                this.state = State.CHALLENGE_RECEIVED;
            } else {
                this.state = State.FAILED;
            }
        } else {
            if (this.state.compareTo(State.MSG_TYPE1_GENERATED) < 0) {
                this.state = State.FAILED;
                throw new MalformedChallengeException("Out of sequence NTLM response message");
            } else if (this.state == State.MSG_TYPE1_GENERATED) {
                this.state = State.MSG_TYPE2_RECEVIED;
                this.challenge = challenge.substring(i, challenge.length()).trim();
            }
        }
    }

    @Override
    public String authenticate(Credentials credentials, HttpMethod method) throws AuthenticationException {
        NTCredentials ntcredentials = null;
        try {
            ntcredentials = (NTCredentials) credentials;
        } catch (final ClassCastException e) {
            throw new AuthenticationException(
             "Credentials cannot be used for NTLM authentication: "
              + credentials.getClass().getName());
        }
        String response = null;
        if (this.state == State.FAILED) {
            throw new AuthenticationException("NTLM authentication failed");
        } else if (this.state == State.CHALLENGE_RECEIVED) {
            response = this.engine.generateType1Msg(
                    ntcredentials.getDomain(),
                    ntcredentials.getHost());
            this.state = State.MSG_TYPE1_GENERATED;
        } else if (this.state == State.MSG_TYPE2_RECEVIED) {
            response = this.engine.generateType3Msg(
                    ntcredentials.getUserName(),
                    ntcredentials.getPassword(),
                    ntcredentials.getDomain(),
                    ntcredentials.getHost(),
                    this.challenge);
            this.state = State.MSG_TYPE3_GENERATED;
        } else {
            throw new AuthenticationException("Unexpected state: " + this.state);
        }
        return "NTLM " + response;
    }

    @Override
    public boolean isComplete() {
        return this.state == State.MSG_TYPE3_GENERATED || this.state == State.FAILED;
    }

    @Deprecated
    @Override
    public String getID() {
        return null;
    }

    @Deprecated
    @Override
    public String authenticate(Credentials credentials, String method, String uri) throws AuthenticationException {
        return null;
    }

}