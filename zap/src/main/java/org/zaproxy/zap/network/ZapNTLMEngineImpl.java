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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.auth.AuthenticationException;

/*
 * The content of this class was copied from org.apache.http.impl.auth.NTLMEngineImpl, HttpComponents Client branch 4.6.x
 * (commit f8a26dffded21be28e1c1ae73f12bb0f42ad29f2).
 * 
 * It was copied because ZAP depends (and uses) Commons HttpClient which is not compatible with, the newer version, 
 * HttpComponents Client.
 * 
 * It's adapted to work with Commons HttpClient.
 * Changes:
 *  - Renamed the class to ZapNTLMEngineImpl to not clash with the name NTLMEngineImpl;
 *  - Moved to "org.zaproxy.zap.network" (instead of keeping it in "org.apache.http.impl.auth");
 *  - Replaced the exception org.apache.http.impl.auth.NTLMEngineException with 
 *    org.apache.commons.httpclient.auth.AuthenticationException;
 *  - Use StandardCharsets instead of org.apache.http.Consts;
 *  - No longer implements org.apache.http.impl.auth.NTLMEngine (doesn't need to).
 */
/**
 * Provides an implementation for NTLMv1, NTLMv2, and NTLM2 Session forms of the NTLM
 * authentication protocol.
 *
 * @deprecated (2.12.0) Implementation details, do not use.
 */
@Deprecated
final class ZapNTLMEngineImpl {

    /** Unicode encoding */
    private static final Charset UNICODE_LITTLE_UNMARKED = Charset.forName("UnicodeLittleUnmarked");
    /** Character encoding */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.US_ASCII;

    // Flags we use; descriptions according to:
    // http://davenport.sourceforge.net/ntlm.html
    // and
    // http://msdn.microsoft.com/en-us/library/cc236650%28v=prot.20%29.aspx
    // [MS-NLMP] section 2.2.2.5
    static final int FLAG_REQUEST_UNICODE_ENCODING = 0x00000001;      // Unicode string encoding requested
    static final int FLAG_REQUEST_OEM_ENCODING = 0x00000002;      // OEM string encoding requested
    static final int FLAG_REQUEST_TARGET = 0x00000004;                      // Requests target field
    static final int FLAG_REQUEST_SIGN = 0x00000010;  // Requests all messages have a signature attached, in NEGOTIATE message.
    static final int FLAG_REQUEST_SEAL = 0x00000020;  // Request key exchange for message confidentiality in NEGOTIATE message.  MUST be used in conjunction with 56BIT.
    static final int FLAG_REQUEST_LAN_MANAGER_KEY = 0x00000080;    // Request Lan Manager key instead of user session key
    static final int FLAG_REQUEST_NTLMv1 = 0x00000200; // Request NTLMv1 security.  MUST be set in NEGOTIATE and CHALLENGE both
    static final int FLAG_DOMAIN_PRESENT = 0x00001000;        // Domain is present in message
    static final int FLAG_WORKSTATION_PRESENT = 0x00002000;   // Workstation is present in message
    static final int FLAG_REQUEST_ALWAYS_SIGN = 0x00008000;   // Requests a signature block on all messages.  Overridden by REQUEST_SIGN and REQUEST_SEAL.
    static final int FLAG_REQUEST_NTLM2_SESSION = 0x00080000; // From server in challenge, requesting NTLM2 session security
    static final int FLAG_REQUEST_VERSION = 0x02000000;       // Request protocol version
    static final int FLAG_TARGETINFO_PRESENT = 0x00800000;    // From server in challenge message, indicating targetinfo is present
    static final int FLAG_REQUEST_128BIT_KEY_EXCH = 0x20000000; // Request explicit 128-bit key exchange
    static final int FLAG_REQUEST_EXPLICIT_KEY_EXCH = 0x40000000;     // Request explicit key exchange
    static final int FLAG_REQUEST_56BIT_ENCRYPTION = 0x80000000;      // Must be used in conjunction with SEAL

    // Attribute-value identifiers (AvId)
    // according to [MS-NLMP] section 2.2.2.1
    static final int MSV_AV_EOL = 0x0000; // Indicates that this is the last AV_PAIR in the list.
    static final int MSV_AV_NB_COMPUTER_NAME = 0x0001; // The server's NetBIOS computer name.
    static final int MSV_AV_NB_DOMAIN_NAME = 0x0002; // The server's NetBIOS domain name.
    static final int MSV_AV_DNS_COMPUTER_NAME = 0x0003; // The fully qualified domain name (FQDN) of the computer.
    static final int MSV_AV_DNS_DOMAIN_NAME = 0x0004; // The FQDN of the domain.
    static final int MSV_AV_DNS_TREE_NAME = 0x0005; // The FQDN of the forest.
    static final int MSV_AV_FLAGS = 0x0006; // A 32-bit value indicating server or client configuration.
    static final int MSV_AV_TIMESTAMP = 0x0007; // server local time
    static final int MSV_AV_SINGLE_HOST = 0x0008; // A Single_Host_Data structure.
    static final int MSV_AV_TARGET_NAME = 0x0009; // The SPN of the target server.
    static final int MSV_AV_CHANNEL_BINDINGS = 0x000A; // A channel bindings hash.

    static final int MSV_AV_FLAGS_ACCOUNT_AUTH_CONSTAINED = 0x00000001; // Indicates to the client that the account authentication is constrained.
    static final int MSV_AV_FLAGS_MIC = 0x00000002; // Indicates that the client is providing message integrity in the MIC field in the AUTHENTICATE_MESSAGE.
    static final int MSV_AV_FLAGS_UNTRUSTED_TARGET_SPN = 0x00000004; // Indicates that the client is providing a target SPN generated from an untrusted source.

    /** Secure random generator */
    private static final java.security.SecureRandom RND_GEN;
    static {
        java.security.SecureRandom rnd = null;
        try {
            rnd = java.security.SecureRandom.getInstance("SHA1PRNG");
        } catch (final Exception ignore) {
        }
        RND_GEN = rnd;
    }

    /** The signature string as bytes in the default encoding */
    private static final byte[] SIGNATURE = getNullTerminatedAsciiString("NTLMSSP");

    // Key derivation magic strings for the SIGNKEY algorithm defined in
    // [MS-NLMP] section 3.4.5.2
    private static final byte[] SIGN_MAGIC_SERVER = getNullTerminatedAsciiString(
        "session key to server-to-client signing key magic constant");
    private static final byte[] SIGN_MAGIC_CLIENT = getNullTerminatedAsciiString(
        "session key to client-to-server signing key magic constant");
    private static final byte[] SEAL_MAGIC_SERVER = getNullTerminatedAsciiString(
        "session key to server-to-client sealing key magic constant");
    private static final byte[] SEAL_MAGIC_CLIENT = getNullTerminatedAsciiString(
        "session key to client-to-server sealing key magic constant");

    // prefix for GSS API channel binding
    private static final byte[] MAGIC_TLS_SERVER_ENDPOINT = "tls-server-end-point:".getBytes(StandardCharsets.US_ASCII);

    private static byte[] getNullTerminatedAsciiString( final String source )
    {
        final byte[] bytesWithoutNull = source.getBytes(StandardCharsets.US_ASCII);
        final byte[] target = new byte[bytesWithoutNull.length + 1];
        System.arraycopy(bytesWithoutNull, 0, target, 0, bytesWithoutNull.length);
        target[bytesWithoutNull.length] = (byte) 0x00;
        return target;
    }

    private static final String TYPE_1_MESSAGE = new Type1Message().getResponse();

    ZapNTLMEngineImpl() {
    }

    /**
     * Creates the first message (type 1 message) in the NTLM authentication
     * sequence. This message includes the user name, domain and host for the
     * authentication session.
     *
     * @param host
     *            the computer name of the host requesting authentication.
     * @param domain
     *            The domain to authenticate with.
     * @return String the message to add to the HTTP request header.
     */
    static String getType1Message(final String host, final String domain) {
        // For compatibility reason do not include domain and host in type 1 message
        //return new Type1Message(domain, host).getResponse();
        return TYPE_1_MESSAGE;
    }

    /**
     * Creates the type 3 message using the given server nonce. The type 3
     * message includes all the information for authentication, host, domain,
     * username and the result of encrypting the nonce sent by the server using
     * the user's password as the key.
     *
     * @param user
     *            The user name. This should not include the domain name.
     * @param password
     *            The password.
     * @param host
     *            The host that is originating the authentication request.
     * @param domain
     *            The domain to authenticate within.
     * @param nonce
     *            the 8 byte array the server sent.
     * @return The type 3 message.
     * @throws AuthenticationException
     *             If {@encrypt(byte[],byte[])} fails.
     */
    static String getType3Message(final String user, final String password, final String host, final String domain,
            final byte[] nonce, final int type2Flags, final String target, final byte[] targetInformation)
            throws AuthenticationException {
        return new Type3Message(domain, host, user, password, nonce, type2Flags, target,
                targetInformation).getResponse();
    }

    /**
     * Creates the type 3 message using the given server nonce. The type 3
     * message includes all the information for authentication, host, domain,
     * username and the result of encrypting the nonce sent by the server using
     * the user's password as the key.
     *
     * @param user
     *            The user name. This should not include the domain name.
     * @param password
     *            The password.
     * @param host
     *            The host that is originating the authentication request.
     * @param domain
     *            The domain to authenticate within.
     * @param nonce
     *            the 8 byte array the server sent.
     * @return The type 3 message.
     * @throws AuthenticationException
     *             If {@encrypt(byte[],byte[])} fails.
     */
    static String getType3Message(final String user, final String password, final String host, final String domain,
            final byte[] nonce, final int type2Flags, final String target, final byte[] targetInformation,
            final Certificate peerServerCertificate, final byte[] type1Message, final byte[] type2Message)
            throws AuthenticationException {
        return new Type3Message(domain, host, user, password, nonce, type2Flags, target,
                targetInformation, peerServerCertificate, type1Message, type2Message).getResponse();
    }

    private static int readULong(final byte[] src, final int index) throws AuthenticationException {
        if (src.length < index + 4) {
            return 0;
        }
        return (src[index] & 0xff) | ((src[index + 1] & 0xff) << 8)
                | ((src[index + 2] & 0xff) << 16) | ((src[index + 3] & 0xff) << 24);
    }

    private static int readUShort(final byte[] src, final int index) throws AuthenticationException {
        if (src.length < index + 2) {
            return 0;
        }
        return (src[index] & 0xff) | ((src[index + 1] & 0xff) << 8);
    }

    private static byte[] readSecurityBuffer(final byte[] src, final int index) throws AuthenticationException {
        final int length = readUShort(src, index);
        final int offset = readULong(src, index + 4);
        if (src.length < offset + length) {
            return new byte[length];
        }
        final byte[] buffer = new byte[length];
        System.arraycopy(src, offset, buffer, 0, length);
        return buffer;
    }

    /** Calculate a challenge block */
    private static byte[] makeRandomChallenge(final Random random) throws AuthenticationException {
        final byte[] rval = new byte[8];
        synchronized (random) {
            random.nextBytes(rval);
        }
        return rval;
    }

    /** Calculate a 16-byte secondary key */
    private static byte[] makeSecondaryKey(final Random random) throws AuthenticationException {
        final byte[] rval = new byte[16];
        synchronized (random) {
            random.nextBytes(rval);
        }
        return rval;
    }

    protected static class CipherGen {

        protected final Random random;
        protected final long currentTime;

        protected final String domain;
        protected final String user;
        protected final String password;
        protected final byte[] challenge;
        protected final String target;
        protected final byte[] targetInformation;

        // Information we can generate but may be passed in (for testing)
        protected byte[] clientChallenge;
        protected byte[] clientChallenge2;
        protected byte[] secondaryKey;
        protected byte[] timestamp;

        // Stuff we always generate
        protected byte[] lmHash = null;
        protected byte[] lmResponse = null;
        protected byte[] ntlmHash = null;
        protected byte[] ntlmResponse = null;
        protected byte[] ntlmv2Hash = null;
        protected byte[] lmv2Hash = null;
        protected byte[] lmv2Response = null;
        protected byte[] ntlmv2Blob = null;
        protected byte[] ntlmv2Response = null;
        protected byte[] ntlm2SessionResponse = null;
        protected byte[] lm2SessionResponse = null;
        protected byte[] lmUserSessionKey = null;
        protected byte[] ntlmUserSessionKey = null;
        protected byte[] ntlmv2UserSessionKey = null;
        protected byte[] ntlm2SessionResponseUserSessionKey = null;
        protected byte[] lanManagerSessionKey = null;

        @Deprecated
        public CipherGen(final String domain, final String user, final String password,
            final byte[] challenge, final String target, final byte[] targetInformation,
            final byte[] clientChallenge, final byte[] clientChallenge2,
            final byte[] secondaryKey, final byte[] timestamp) {
            this(RND_GEN, System.currentTimeMillis(),
                domain, user, password, challenge, target, targetInformation,
                clientChallenge, clientChallenge2,
                secondaryKey, timestamp);
        }

        public CipherGen(final Random random, final long currentTime,
            final String domain, final String user, final String password,
            final byte[] challenge, final String target, final byte[] targetInformation,
            final byte[] clientChallenge, final byte[] clientChallenge2,
            final byte[] secondaryKey, final byte[] timestamp) {
            this.random = random;
            this.currentTime = currentTime;

            this.domain = domain;
            this.target = target;
            this.user = user;
            this.password = password;
            this.challenge = challenge;
            this.targetInformation = targetInformation;
            this.clientChallenge = clientChallenge;
            this.clientChallenge2 = clientChallenge2;
            this.secondaryKey = secondaryKey;
            this.timestamp = timestamp;
        }

        @Deprecated
        public CipherGen(final String domain,
            final String user,
            final String password,
            final byte[] challenge,
            final String target,
            final byte[] targetInformation) {
            this(RND_GEN, System.currentTimeMillis(), domain, user, password, challenge, target, targetInformation);
        }

        public CipherGen(final Random random, final long currentTime,
            final String domain,
            final String user,
            final String password,
            final byte[] challenge,
            final String target,
            final byte[] targetInformation) {
            this(random, currentTime, domain, user, password, challenge, target, targetInformation, null, null, null, null);
        }

        /** Calculate and return client challenge */
        public byte[] getClientChallenge()
            throws AuthenticationException {
            if (clientChallenge == null) {
                clientChallenge = makeRandomChallenge(random);
            }
            return clientChallenge;
        }

        /** Calculate and return second client challenge */
        public byte[] getClientChallenge2()
            throws AuthenticationException {
            if (clientChallenge2 == null) {
                clientChallenge2 = makeRandomChallenge(random);
            }
            return clientChallenge2;
        }

        /** Calculate and return random secondary key */
        public byte[] getSecondaryKey()
            throws AuthenticationException {
            if (secondaryKey == null) {
                secondaryKey = makeSecondaryKey(random);
            }
            return secondaryKey;
        }

        /** Calculate and return the LMHash */
        public byte[] getLMHash()
            throws AuthenticationException {
            if (lmHash == null) {
                lmHash = lmHash(password);
            }
            return lmHash;
        }

        /** Calculate and return the LMResponse */
        public byte[] getLMResponse()
            throws AuthenticationException {
            if (lmResponse == null) {
                lmResponse = lmResponse(getLMHash(),challenge);
            }
            return lmResponse;
        }

        /** Calculate and return the NTLMHash */
        public byte[] getNTLMHash()
            throws AuthenticationException {
            if (ntlmHash == null) {
                ntlmHash = ntlmHash(password);
            }
            return ntlmHash;
        }

        /** Calculate and return the NTLMResponse */
        public byte[] getNTLMResponse()
            throws AuthenticationException {
            if (ntlmResponse == null) {
                ntlmResponse = lmResponse(getNTLMHash(),challenge);
            }
            return ntlmResponse;
        }

        /** Calculate the LMv2 hash */
        public byte[] getLMv2Hash()
            throws AuthenticationException {
            if (lmv2Hash == null) {
                lmv2Hash = lmv2Hash(domain, user, getNTLMHash());
            }
            return lmv2Hash;
        }

        /** Calculate the NTLMv2 hash */
        public byte[] getNTLMv2Hash()
            throws AuthenticationException {
            if (ntlmv2Hash == null) {
                ntlmv2Hash = ntlmv2Hash(domain, user, getNTLMHash());
            }
            return ntlmv2Hash;
        }

        /** Calculate a timestamp */
        public byte[] getTimestamp() {
            if (timestamp == null) {
                long time = this.currentTime;
                time += 11644473600000l; // milliseconds from January 1, 1601 -> epoch.
                time *= 10000; // tenths of a microsecond.
                // convert to little-endian byte array.
                timestamp = new byte[8];
                for (int i = 0; i < 8; i++) {
                    timestamp[i] = (byte) time;
                    time >>>= 8;
                }
            }
            return timestamp;
        }

        /** Calculate the NTLMv2Blob */
        public byte[] getNTLMv2Blob()
            throws AuthenticationException {
            if (ntlmv2Blob == null) {
                ntlmv2Blob = createBlob(getClientChallenge2(), targetInformation, getTimestamp());
            }
            return ntlmv2Blob;
        }

        /** Calculate the NTLMv2Response */
        public byte[] getNTLMv2Response()
            throws AuthenticationException {
            if (ntlmv2Response == null) {
                ntlmv2Response = lmv2Response(getNTLMv2Hash(),challenge,getNTLMv2Blob());
            }
            return ntlmv2Response;
        }

        /** Calculate the LMv2Response */
        public byte[] getLMv2Response()
            throws AuthenticationException {
            if (lmv2Response == null) {
                lmv2Response = lmv2Response(getLMv2Hash(),challenge,getClientChallenge());
            }
            return lmv2Response;
        }

        /** Get NTLM2SessionResponse */
        public byte[] getNTLM2SessionResponse()
            throws AuthenticationException {
            if (ntlm2SessionResponse == null) {
                ntlm2SessionResponse = ntlm2SessionResponse(getNTLMHash(),challenge,getClientChallenge());
            }
            return ntlm2SessionResponse;
        }

        /** Calculate and return LM2 session response */
        public byte[] getLM2SessionResponse()
            throws AuthenticationException {
            if (lm2SessionResponse == null) {
                final byte[] clntChallenge = getClientChallenge();
                lm2SessionResponse = new byte[24];
                System.arraycopy(clntChallenge, 0, lm2SessionResponse, 0, clntChallenge.length);
                Arrays.fill(lm2SessionResponse, clntChallenge.length, lm2SessionResponse.length, (byte) 0x00);
            }
            return lm2SessionResponse;
        }

        /** Get LMUserSessionKey */
        public byte[] getLMUserSessionKey()
            throws AuthenticationException {
            if (lmUserSessionKey == null) {
                lmUserSessionKey = new byte[16];
                System.arraycopy(getLMHash(), 0, lmUserSessionKey, 0, 8);
                Arrays.fill(lmUserSessionKey, 8, 16, (byte) 0x00);
            }
            return lmUserSessionKey;
        }

        /** Get NTLMUserSessionKey */
        public byte[] getNTLMUserSessionKey()
            throws AuthenticationException {
            if (ntlmUserSessionKey == null) {
                final MD4 md4 = new MD4();
                md4.update(getNTLMHash());
                ntlmUserSessionKey = md4.getOutput();
            }
            return ntlmUserSessionKey;
        }

        /** GetNTLMv2UserSessionKey */
        public byte[] getNTLMv2UserSessionKey()
            throws AuthenticationException {
            if (ntlmv2UserSessionKey == null) {
                final byte[] ntlmv2hash = getNTLMv2Hash();
                final byte[] truncatedResponse = new byte[16];
                System.arraycopy(getNTLMv2Response(), 0, truncatedResponse, 0, 16);
                ntlmv2UserSessionKey = hmacMD5(truncatedResponse, ntlmv2hash);
            }
            return ntlmv2UserSessionKey;
        }

        /** Get NTLM2SessionResponseUserSessionKey */
        public byte[] getNTLM2SessionResponseUserSessionKey()
            throws AuthenticationException {
            if (ntlm2SessionResponseUserSessionKey == null) {
                final byte[] ntlm2SessionResponseNonce = getLM2SessionResponse();
                final byte[] sessionNonce = new byte[challenge.length + ntlm2SessionResponseNonce.length];
                System.arraycopy(challenge, 0, sessionNonce, 0, challenge.length);
                System.arraycopy(ntlm2SessionResponseNonce, 0, sessionNonce, challenge.length, ntlm2SessionResponseNonce.length);
                ntlm2SessionResponseUserSessionKey = hmacMD5(sessionNonce,getNTLMUserSessionKey());
            }
            return ntlm2SessionResponseUserSessionKey;
        }

        /** Get LAN Manager session key */
        public byte[] getLanManagerSessionKey()
            throws AuthenticationException {
            if (lanManagerSessionKey == null) {
                try {
                    final byte[] keyBytes = new byte[14];
                    System.arraycopy(getLMHash(), 0, keyBytes, 0, 8);
                    Arrays.fill(keyBytes, 8, keyBytes.length, (byte)0xbd);
                    final Key lowKey = createDESKey(keyBytes, 0);
                    final Key highKey = createDESKey(keyBytes, 7);
                    final byte[] truncatedResponse = new byte[8];
                    System.arraycopy(getLMResponse(), 0, truncatedResponse, 0, truncatedResponse.length);
                    Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
                    des.init(Cipher.ENCRYPT_MODE, lowKey);
                    final byte[] lowPart = des.doFinal(truncatedResponse);
                    des = Cipher.getInstance("DES/ECB/NoPadding");
                    des.init(Cipher.ENCRYPT_MODE, highKey);
                    final byte[] highPart = des.doFinal(truncatedResponse);
                    lanManagerSessionKey = new byte[16];
                    System.arraycopy(lowPart, 0, lanManagerSessionKey, 0, lowPart.length);
                    System.arraycopy(highPart, 0, lanManagerSessionKey, lowPart.length, highPart.length);
                } catch (final Exception e) {
                    throw new AuthenticationException(e.getMessage(), e);
                }
            }
            return lanManagerSessionKey;
        }
    }

    /** Calculates HMAC-MD5 */
    static byte[] hmacMD5(final byte[] value, final byte[] key)
        throws AuthenticationException {
        final HMACMD5 hmacMD5 = new HMACMD5(key);
        hmacMD5.update(value);
        return hmacMD5.getOutput();
    }

    /** Calculates RC4 */
    static byte[] RC4(final byte[] value, final byte[] key)
        throws AuthenticationException {
        try {
            final Cipher rc4 = Cipher.getInstance("RC4");
            rc4.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "RC4"));
            return rc4.doFinal(value);
        } catch (final Exception e) {
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    /**
     * Calculates the NTLM2 Session Response for the given challenge, using the
     * specified password and client challenge.
     *
     * @return The NTLM2 Session Response. This is placed in the NTLM response
     *         field of the Type 3 message; the LM response field contains the
     *         client challenge, null-padded to 24 bytes.
     */
    static byte[] ntlm2SessionResponse(final byte[] ntlmHash, final byte[] challenge,
            final byte[] clientChallenge) throws AuthenticationException {
        try {
            final MessageDigest md5 = getMD5();
            md5.update(challenge);
            md5.update(clientChallenge);
            final byte[] digest = md5.digest();

            final byte[] sessionHash = new byte[8];
            System.arraycopy(digest, 0, sessionHash, 0, 8);
            return lmResponse(ntlmHash, sessionHash);
        } catch (final Exception e) {
            if (e instanceof AuthenticationException) {
                throw (AuthenticationException) e;
            }
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    /**
     * Creates the LM Hash of the user's password.
     *
     * @param password
     *            The password.
     *
     * @return The LM Hash of the given password, used in the calculation of the
     *         LM Response.
     */
    private static byte[] lmHash(final String password) throws AuthenticationException {
        try {
            final byte[] oemPassword = password.toUpperCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
            final int length = Math.min(oemPassword.length, 14);
            final byte[] keyBytes = new byte[14];
            System.arraycopy(oemPassword, 0, keyBytes, 0, length);
            final Key lowKey = createDESKey(keyBytes, 0);
            final Key highKey = createDESKey(keyBytes, 7);
            final byte[] magicConstant = "KGS!@#$%".getBytes(StandardCharsets.US_ASCII);
            final Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
            des.init(Cipher.ENCRYPT_MODE, lowKey);
            final byte[] lowHash = des.doFinal(magicConstant);
            des.init(Cipher.ENCRYPT_MODE, highKey);
            final byte[] highHash = des.doFinal(magicConstant);
            final byte[] lmHash = new byte[16];
            System.arraycopy(lowHash, 0, lmHash, 0, 8);
            System.arraycopy(highHash, 0, lmHash, 8, 8);
            return lmHash;
        } catch (final Exception e) {
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    /**
     * Creates the NTLM Hash of the user's password.
     *
     * @param password
     *            The password.
     *
     * @return The NTLM Hash of the given password, used in the calculation of
     *         the NTLM Response and the NTLMv2 and LMv2 Hashes.
     */
    private static byte[] ntlmHash(final String password) throws AuthenticationException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new AuthenticationException("Unicode not supported");
        }
        final byte[] unicodePassword = password.getBytes(UNICODE_LITTLE_UNMARKED);
        final MD4 md4 = new MD4();
        md4.update(unicodePassword);
        return md4.getOutput();
    }

    /**
     * Creates the LMv2 Hash of the user's password.
     *
     * @return The LMv2 Hash, used in the calculation of the NTLMv2 and LMv2
     *         Responses.
     */
    private static byte[] lmv2Hash(final String domain, final String user, final byte[] ntlmHash)
            throws AuthenticationException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new AuthenticationException("Unicode not supported");
        }
        final HMACMD5 hmacMD5 = new HMACMD5(ntlmHash);
        // Upper case username, upper case domain!
        hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        if (domain != null) {
            hmacMD5.update(domain.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        }
        return hmacMD5.getOutput();
    }

    /**
     * Creates the NTLMv2 Hash of the user's password.
     *
     * @return The NTLMv2 Hash, used in the calculation of the NTLMv2 and LMv2
     *         Responses.
     */
    private static byte[] ntlmv2Hash(final String domain, final String user, final byte[] ntlmHash)
            throws AuthenticationException {
        if (UNICODE_LITTLE_UNMARKED == null) {
            throw new AuthenticationException("Unicode not supported");
        }
        final HMACMD5 hmacMD5 = new HMACMD5(ntlmHash);
        // Upper case username, mixed case target!!
        hmacMD5.update(user.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED));
        if (domain != null) {
            hmacMD5.update(domain.getBytes(UNICODE_LITTLE_UNMARKED));
        }
        return hmacMD5.getOutput();
    }

    /**
     * Creates the LM Response from the given hash and Type 2 challenge.
     *
     * @param hash
     *            The LM or NTLM Hash.
     * @param challenge
     *            The server challenge from the Type 2 message.
     *
     * @return The response (either LM or NTLM, depending on the provided hash).
     */
    private static byte[] lmResponse(final byte[] hash, final byte[] challenge) throws AuthenticationException {
        try {
            final byte[] keyBytes = new byte[21];
            System.arraycopy(hash, 0, keyBytes, 0, 16);
            final Key lowKey = createDESKey(keyBytes, 0);
            final Key middleKey = createDESKey(keyBytes, 7);
            final Key highKey = createDESKey(keyBytes, 14);
            final Cipher des = Cipher.getInstance("DES/ECB/NoPadding");
            des.init(Cipher.ENCRYPT_MODE, lowKey);
            final byte[] lowResponse = des.doFinal(challenge);
            des.init(Cipher.ENCRYPT_MODE, middleKey);
            final byte[] middleResponse = des.doFinal(challenge);
            des.init(Cipher.ENCRYPT_MODE, highKey);
            final byte[] highResponse = des.doFinal(challenge);
            final byte[] lmResponse = new byte[24];
            System.arraycopy(lowResponse, 0, lmResponse, 0, 8);
            System.arraycopy(middleResponse, 0, lmResponse, 8, 8);
            System.arraycopy(highResponse, 0, lmResponse, 16, 8);
            return lmResponse;
        } catch (final Exception e) {
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    /**
     * Creates the LMv2 Response from the given hash, client data, and Type 2
     * challenge.
     *
     * @param hash
     *            The NTLMv2 Hash.
     * @param clientData
     *            The client data (blob or client challenge).
     * @param challenge
     *            The server challenge from the Type 2 message.
     *
     * @return The response (either NTLMv2 or LMv2, depending on the client
     *         data).
     */
    private static byte[] lmv2Response(final byte[] hash, final byte[] challenge, final byte[] clientData)
            throws AuthenticationException {
        final HMACMD5 hmacMD5 = new HMACMD5(hash);
        hmacMD5.update(challenge);
        hmacMD5.update(clientData);
        final byte[] mac = hmacMD5.getOutput();
        final byte[] lmv2Response = new byte[mac.length + clientData.length];
        System.arraycopy(mac, 0, lmv2Response, 0, mac.length);
        System.arraycopy(clientData, 0, lmv2Response, mac.length, clientData.length);
        return lmv2Response;
    }

    static enum Mode
    {
        CLIENT, SERVER;
    }

    static class Handle
    {
        final private byte[] exportedSessionKey;
        private byte[] signingKey;
        private byte[] sealingKey;
        private final Cipher rc4;
        final Mode mode;
        final private boolean isConnection;
        int sequenceNumber = 0;


        Handle( final byte[] exportedSessionKey, final Mode mode, final boolean isConnection )
            throws AuthenticationException
        {
            this.exportedSessionKey = exportedSessionKey;
            this.isConnection = isConnection;
            this.mode = mode;
            try
            {
                final MessageDigest signMd5 = getMD5();
                final MessageDigest sealMd5 = getMD5();
                signMd5.update( exportedSessionKey );
                sealMd5.update( exportedSessionKey );
                if ( mode == Mode.CLIENT )
                {
                    signMd5.update( SIGN_MAGIC_CLIENT );
                    sealMd5.update( SEAL_MAGIC_CLIENT );
                }
                else
                {
                    signMd5.update( SIGN_MAGIC_SERVER );
                    sealMd5.update( SEAL_MAGIC_SERVER );
                }
                signingKey = signMd5.digest();
                sealingKey = sealMd5.digest();
            }
            catch ( final Exception e )
            {
                throw new AuthenticationException( e.getMessage(), e );
            }
            rc4 = initCipher();
        }

        public byte[] getSigningKey()
        {
            return signingKey;
        }


        public byte[] getSealingKey()
        {
            return sealingKey;
        }

        private Cipher initCipher() throws AuthenticationException
        {
            final Cipher cipher;
            try
            {
                cipher = Cipher.getInstance( "RC4" );
                if ( mode == Mode.CLIENT )
                {
                    cipher.init( Cipher.ENCRYPT_MODE, new SecretKeySpec( sealingKey, "RC4" ) );
                }
                else
                {
                    cipher.init( Cipher.DECRYPT_MODE, new SecretKeySpec( sealingKey, "RC4" ) );
                }
            }
            catch ( final Exception e )
            {
                throw new AuthenticationException( e.getMessage(), e );
            }
            return cipher;
        }


        private void advanceMessageSequence() throws AuthenticationException
        {
            if ( !isConnection )
            {
                final MessageDigest sealMd5 = getMD5();
                sealMd5.update( sealingKey );
                final byte[] seqNumBytes = new byte[4];
                writeULong( seqNumBytes, sequenceNumber, 0 );
                sealMd5.update( seqNumBytes );
                sealingKey = sealMd5.digest();
                initCipher();
            }
            sequenceNumber++;
        }

        private byte[] encrypt( final byte[] data ) throws AuthenticationException
        {
            return rc4.update( data );
        }

        private byte[] decrypt( final byte[] data ) throws AuthenticationException
        {
            return rc4.update( data );
        }

        private byte[] computeSignature( final byte[] message ) throws AuthenticationException
        {
            final byte[] sig = new byte[16];

            // version
            sig[0] = 0x01;
            sig[1] = 0x00;
            sig[2] = 0x00;
            sig[3] = 0x00;

            // HMAC (first 8 bytes)
            final HMACMD5 hmacMD5 = new HMACMD5( signingKey );
            hmacMD5.update( encodeLong( sequenceNumber ) );
            hmacMD5.update( message );
            final byte[] hmac = hmacMD5.getOutput();
            final byte[] trimmedHmac = new byte[8];
            System.arraycopy( hmac, 0, trimmedHmac, 0, 8 );
            final byte[] encryptedHmac = encrypt( trimmedHmac );
            System.arraycopy( encryptedHmac, 0, sig, 4, 8 );

            // sequence number
            encodeLong( sig, 12, sequenceNumber );

            return sig;
        }

        private boolean validateSignature( final byte[] signature, final byte message[] ) throws AuthenticationException
        {
            final byte[] computedSignature = computeSignature( message );
            //            log.info( "SSSSS validateSignature("+seqNumber+")\n"
            //                + "  received: " + DebugUtil.dump( signature ) + "\n"
            //                + "  computed: " + DebugUtil.dump( computedSignature ) );
            return Arrays.equals( signature, computedSignature );
        }

        public byte[] signAndEncryptMessage( final byte[] cleartextMessage ) throws AuthenticationException
        {
            final byte[] encryptedMessage = encrypt( cleartextMessage );
            final byte[] signature = computeSignature( cleartextMessage );
            final byte[] outMessage = new byte[signature.length + encryptedMessage.length];
            System.arraycopy( signature, 0, outMessage, 0, signature.length );
            System.arraycopy( encryptedMessage, 0, outMessage, signature.length, encryptedMessage.length );
            advanceMessageSequence();
            return outMessage;
        }

        public byte[] decryptAndVerifySignedMessage( final byte[] inMessage ) throws AuthenticationException
        {
            final byte[] signature = new byte[16];
            System.arraycopy( inMessage, 0, signature, 0, signature.length );
            final byte[] encryptedMessage = new byte[inMessage.length - 16];
            System.arraycopy( inMessage, 16, encryptedMessage, 0, encryptedMessage.length );
            final byte[] cleartextMessage = decrypt( encryptedMessage );
            if ( !validateSignature( signature, cleartextMessage ) )
            {
                throw new AuthenticationException( "Wrong signature" );
            }
            advanceMessageSequence();
            return cleartextMessage;
        }

    }

    private static byte[] encodeLong( final int value )
    {
        final byte[] enc = new byte[4];
        encodeLong( enc, 0, value );
        return enc;
    }

    private static void encodeLong( final byte[] buf, final int offset, final int value )
    {
        buf[offset + 0] = ( byte ) ( value & 0xff );
        buf[offset + 1] = ( byte ) ( value >> 8 & 0xff );
        buf[offset + 2] = ( byte ) ( value >> 16 & 0xff );
        buf[offset + 3] = ( byte ) ( value >> 24 & 0xff );
    }

    /**
     * Creates the NTLMv2 blob from the given target information block and
     * client challenge.
     *
     * @param targetInformation
     *            The target information block from the Type 2 message.
     * @param clientChallenge
     *            The random 8-byte client challenge.
     *
     * @return The blob, used in the calculation of the NTLMv2 Response.
     */
    private static byte[] createBlob(final byte[] clientChallenge, final byte[] targetInformation, final byte[] timestamp) {
        final byte[] blobSignature = new byte[] { (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0x00 };
        final byte[] reserved = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        final byte[] unknown1 = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        final byte[] unknown2 = new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
        final byte[] blob = new byte[blobSignature.length + reserved.length + timestamp.length + 8
                + unknown1.length + targetInformation.length + unknown2.length];
        int offset = 0;
        System.arraycopy(blobSignature, 0, blob, offset, blobSignature.length);
        offset += blobSignature.length;
        System.arraycopy(reserved, 0, blob, offset, reserved.length);
        offset += reserved.length;
        System.arraycopy(timestamp, 0, blob, offset, timestamp.length);
        offset += timestamp.length;
        System.arraycopy(clientChallenge, 0, blob, offset, 8);
        offset += 8;
        System.arraycopy(unknown1, 0, blob, offset, unknown1.length);
        offset += unknown1.length;
        System.arraycopy(targetInformation, 0, blob, offset, targetInformation.length);
        offset += targetInformation.length;
        System.arraycopy(unknown2, 0, blob, offset, unknown2.length);
        offset += unknown2.length;
        return blob;
    }

    /**
     * Creates a DES encryption key from the given key material.
     *
     * @param bytes
     *            A byte array containing the DES key material.
     * @param offset
     *            The offset in the given byte array at which the 7-byte key
     *            material starts.
     *
     * @return A DES encryption key created from the key material starting at
     *         the specified offset in the given byte array.
     */
    private static Key createDESKey(final byte[] bytes, final int offset) {
        final byte[] keyBytes = new byte[7];
        System.arraycopy(bytes, offset, keyBytes, 0, 7);
        final byte[] material = new byte[8];
        material[0] = keyBytes[0];
        material[1] = (byte) (keyBytes[0] << 7 | (keyBytes[1] & 0xff) >>> 1);
        material[2] = (byte) (keyBytes[1] << 6 | (keyBytes[2] & 0xff) >>> 2);
        material[3] = (byte) (keyBytes[2] << 5 | (keyBytes[3] & 0xff) >>> 3);
        material[4] = (byte) (keyBytes[3] << 4 | (keyBytes[4] & 0xff) >>> 4);
        material[5] = (byte) (keyBytes[4] << 3 | (keyBytes[5] & 0xff) >>> 5);
        material[6] = (byte) (keyBytes[5] << 2 | (keyBytes[6] & 0xff) >>> 6);
        material[7] = (byte) (keyBytes[6] << 1);
        oddParity(material);
        return new SecretKeySpec(material, "DES");
    }

    /**
     * Applies odd parity to the given byte array.
     *
     * @param bytes
     *            The data whose parity bits are to be adjusted for odd parity.
     */
    private static void oddParity(final byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            final byte b = bytes[i];
            final boolean needsParity = (((b >>> 7) ^ (b >>> 6) ^ (b >>> 5) ^ (b >>> 4) ^ (b >>> 3)
                    ^ (b >>> 2) ^ (b >>> 1)) & 0x01) == 0;
            if (needsParity) {
                bytes[i] |= (byte) 0x01;
            } else {
                bytes[i] &= (byte) 0xfe;
            }
        }
    }

    /**
     * Find the character set based on the flags.
     * @param flags is the flags.
     * @return the character set.
     */
    private static Charset getCharset(final int flags) throws AuthenticationException
    {
        if ((flags & FLAG_REQUEST_UNICODE_ENCODING) == 0) {
            return DEFAULT_CHARSET;
        } else {
            if (UNICODE_LITTLE_UNMARKED == null) {
                throw new AuthenticationException( "Unicode not supported" );
            }
            return UNICODE_LITTLE_UNMARKED;
        }
    }

    /** Strip dot suffix from a name */
    private static String stripDotSuffix(final String value) {
        if (value == null) {
            return null;
        }
        final int index = value.indexOf(".");
        if (index != -1) {
            return value.substring(0, index);
        }
        return value;
    }

    /** Convert host to standard form */
    private static String convertHost(final String host) {
        return stripDotSuffix(host);
    }

    /** Convert domain to standard form */
    private static String convertDomain(final String domain) {
        return stripDotSuffix(domain);
    }

    /** NTLM message generation, base class */
    static class NTLMMessage {
        /** The current response */
        protected byte[] messageContents = null;

        /** The current output position */
        protected int currentOutputPosition = 0;

        /** Constructor to use when message contents are not yet known */
        NTLMMessage() {
        }

        /** Constructor taking a string */
        NTLMMessage(final String messageBody, final int expectedType) throws AuthenticationException {
            this(Base64.decodeBase64(messageBody.getBytes(DEFAULT_CHARSET)), expectedType);
        }

        /** Constructor to use when message bytes are known */
        NTLMMessage(final byte[] message, final int expectedType) throws AuthenticationException {
            messageContents = message;
            // Look for NTLM message
            if (messageContents.length < SIGNATURE.length) {
                throw new AuthenticationException("NTLM message decoding error - packet too short");
            }
            int i = 0;
            while (i < SIGNATURE.length) {
                if (messageContents[i] != SIGNATURE[i]) {
                    throw new AuthenticationException(
                            "NTLM message expected - instead got unrecognized bytes");
                }
                i++;
            }

            // Check to be sure there's a type 2 message indicator next
            final int type = readULong(SIGNATURE.length);
            if (type != expectedType) {
                throw new AuthenticationException("NTLM type " + Integer.toString(expectedType)
                        + " message expected - instead got type " + Integer.toString(type));
            }

            currentOutputPosition = messageContents.length;
        }

        /**
         * Get the length of the signature and flags, so calculations can adjust
         * offsets accordingly.
         */
        protected int getPreambleLength() {
            return SIGNATURE.length + 4;
        }

        /** Get the message length */
        protected int getMessageLength() {
            return currentOutputPosition;
        }

        /** Read a byte from a position within the message buffer */
        protected byte readByte(final int position) throws AuthenticationException {
            if (messageContents.length < position + 1) {
                throw new AuthenticationException("NTLM: Message too short");
            }
            return messageContents[position];
        }

        /** Read a bunch of bytes from a position in the message buffer */
        protected void readBytes(final byte[] buffer, final int position) throws AuthenticationException {
            if (messageContents.length < position + buffer.length) {
                throw new AuthenticationException("NTLM: Message too short");
            }
            System.arraycopy(messageContents, position, buffer, 0, buffer.length);
        }

        /** Read a ushort from a position within the message buffer */
        protected int readUShort(final int position) throws AuthenticationException {
            return ZapNTLMEngineImpl.readUShort(messageContents, position);
        }

        /** Read a ulong from a position within the message buffer */
        protected int readULong(final int position) throws AuthenticationException {
            return ZapNTLMEngineImpl.readULong(messageContents, position);
        }

        /** Read a security buffer from a position within the message buffer */
        protected byte[] readSecurityBuffer(final int position) throws AuthenticationException {
            return ZapNTLMEngineImpl.readSecurityBuffer(messageContents, position);
        }

        /**
         * Prepares the object to create a response of the given length.
         *
         * @param maxlength
         *            the maximum length of the response to prepare,
         *            including the type and the signature (which this method
         *            adds).
         */
        protected void prepareResponse(final int maxlength, final int messageType) {
            messageContents = new byte[maxlength];
            currentOutputPosition = 0;
            addBytes(SIGNATURE);
            addULong(messageType);
        }

        /**
         * Adds the given byte to the response.
         *
         * @param b
         *            the byte to add.
         */
        protected void addByte(final byte b) {
            messageContents[currentOutputPosition] = b;
            currentOutputPosition++;
        }

        /**
         * Adds the given bytes to the response.
         *
         * @param bytes
         *            the bytes to add.
         */
        protected void addBytes(final byte[] bytes) {
            if (bytes == null) {
                return;
            }
            for (final byte b : bytes) {
                messageContents[currentOutputPosition] = b;
                currentOutputPosition++;
            }
        }

        /** Adds a USHORT to the response */
        protected void addUShort(final int value) {
            addByte((byte) (value & 0xff));
            addByte((byte) (value >> 8 & 0xff));
        }

        /** Adds a ULong to the response */
        protected void addULong(final int value) {
            addByte((byte) (value & 0xff));
            addByte((byte) (value >> 8 & 0xff));
            addByte((byte) (value >> 16 & 0xff));
            addByte((byte) (value >> 24 & 0xff));
        }

        /**
         * Returns the response that has been generated after shrinking the
         * array if required and base64 encodes the response.
         *
         * @return The response as above.
         */
        public String getResponse() {
            return new String(Base64.encodeBase64(getBytes()), StandardCharsets.US_ASCII);
        }

        public byte[] getBytes() {
            if (messageContents == null) {
                buildMessage();
            }
            final byte[] resp;
            if ( messageContents.length > currentOutputPosition ) {
                final byte[] tmp = new byte[currentOutputPosition];
                System.arraycopy( messageContents, 0, tmp, 0, currentOutputPosition );
                messageContents = tmp;
            }
            return messageContents;
        }

        protected void buildMessage() {
            throw new RuntimeException("Message builder not implemented for "+getClass().getName());
        }
    }

    /** Type 1 message assembly class */
    static class Type1Message extends NTLMMessage {

        private final byte[] hostBytes;
        private final byte[] domainBytes;
        private final int flags;

        Type1Message(final String domain, final String host) throws AuthenticationException {
            this(domain, host, null);
        }

        Type1Message(final String domain, final String host, final Integer flags) throws AuthenticationException {
            super();
            this.flags = ((flags == null)?getDefaultFlags():flags);

            // Strip off domain name from the host!
            final String unqualifiedHost = convertHost(host);
            // Use only the base domain name!
            final String unqualifiedDomain = convertDomain(domain);

            hostBytes = unqualifiedHost != null ?
                    unqualifiedHost.getBytes(UNICODE_LITTLE_UNMARKED) : null;
            domainBytes = unqualifiedDomain != null ?
                    unqualifiedDomain.toUpperCase(Locale.ROOT).getBytes(UNICODE_LITTLE_UNMARKED) : null;
        }

        Type1Message() {
            super();
            hostBytes = null;
            domainBytes = null;
            flags = getDefaultFlags();
        }

        private int getDefaultFlags() {
            return
                //FLAG_WORKSTATION_PRESENT |
                //FLAG_DOMAIN_PRESENT |

                // Required flags
                //FLAG_REQUEST_LAN_MANAGER_KEY |
                FLAG_REQUEST_NTLMv1 |
                FLAG_REQUEST_NTLM2_SESSION |

                // Protocol version request
                FLAG_REQUEST_VERSION |

                // Recommended privacy settings
                FLAG_REQUEST_ALWAYS_SIGN |
                //FLAG_REQUEST_SEAL |
                //FLAG_REQUEST_SIGN |

                // These must be set according to documentation, based on use of SEAL above
                FLAG_REQUEST_128BIT_KEY_EXCH |
                FLAG_REQUEST_56BIT_ENCRYPTION |
                //FLAG_REQUEST_EXPLICIT_KEY_EXCH |

                FLAG_REQUEST_UNICODE_ENCODING;

        }

        /**
         * Getting the response involves building the message before returning
         * it
         */
        @Override
        protected void buildMessage() {
            int domainBytesLength = 0;
            if ( domainBytes != null ) {
                domainBytesLength = domainBytes.length;
            }
            int hostBytesLength = 0;
            if ( hostBytes != null ) {
                hostBytesLength = hostBytes.length;
            }

            // Now, build the message. Calculate its length first, including
            // signature or type.
            final int finalLength = 32 + 8 + hostBytesLength + domainBytesLength;

            // Set up the response. This will initialize the signature, message
            // type, and flags.
            prepareResponse(finalLength, 1);

            // Flags. These are the complete set of flags we support.
            addULong(flags);

            // Domain length (two times).
            addUShort(domainBytesLength);
            addUShort(domainBytesLength);

            // Domain offset.
            addULong(hostBytesLength + 32 + 8);

            // Host length (two times).
            addUShort(hostBytesLength);
            addUShort(hostBytesLength);

            // Host offset (always 32 + 8).
            addULong(32 + 8);

            // Version
            addUShort(0x0105);
            // Build
            addULong(2600);
            // NTLM revision
            addUShort(0x0f00);

            // Host (workstation) String.
            if (hostBytes != null) {
                addBytes(hostBytes);
            }
            // Domain String.
            if (domainBytes != null) {
                addBytes(domainBytes);
            }
        }

    }

    /** Type 2 message class */
    static class Type2Message extends NTLMMessage {
        protected final byte[] challenge;
        protected String target;
        protected byte[] targetInfo;
        protected final int flags;

        Type2Message(final String messageBody) throws AuthenticationException {
            this(Base64.decodeBase64(messageBody.getBytes(DEFAULT_CHARSET)));
        }

        Type2Message(final byte[] message) throws AuthenticationException {
            super(message, 2);

            // Type 2 message is laid out as follows:
            // First 8 bytes: NTLMSSP[0]
            // Next 4 bytes: Ulong, value 2
            // Next 8 bytes, starting at offset 12: target field (2 ushort lengths, 1 ulong offset)
            // Next 4 bytes, starting at offset 20: Flags, e.g. 0x22890235
            // Next 8 bytes, starting at offset 24: Challenge
            // Next 8 bytes, starting at offset 32: ??? (8 bytes of zeros)
            // Next 8 bytes, starting at offset 40: targetinfo field (2 ushort lengths, 1 ulong offset)
            // Next 2 bytes, major/minor version number (e.g. 0x05 0x02)
            // Next 8 bytes, build number
            // Next 2 bytes, protocol version number (e.g. 0x00 0x0f)
            // Next, various text fields, and a ushort of value 0 at the end

            // Parse out the rest of the info we need from the message
            // The nonce is the 8 bytes starting from the byte in position 24.
            challenge = new byte[8];
            readBytes(challenge, 24);

            flags = readULong(20);

            // Do the target!
            target = null;
            // The TARGET_DESIRED flag is said to not have understood semantics
            // in Type2 messages, so use the length of the packet to decide
            // how to proceed instead
            if (getMessageLength() >= 12 + 8) {
                final byte[] bytes = readSecurityBuffer(12);
                if (bytes.length != 0) {
                    target = new String(bytes, getCharset(flags));
                }
            }

            // Do the target info!
            targetInfo = null;
            // TARGET_DESIRED flag cannot be relied on, so use packet length
            if (getMessageLength() >= 40 + 8) {
                final byte[] bytes = readSecurityBuffer(40);
                if (bytes.length != 0) {
                    targetInfo = bytes;
                }
            }
        }

        /** Retrieve the challenge */
        byte[] getChallenge() {
            return challenge;
        }

        /** Retrieve the target */
        String getTarget() {
            return target;
        }

        /** Retrieve the target info */
        byte[] getTargetInfo() {
            return targetInfo;
        }

        /** Retrieve the response flags */
        int getFlags() {
            return flags;
        }

    }

    /** Type 3 message assembly class */
    static class Type3Message extends NTLMMessage {
        // For mic computation
        protected final byte[] type1Message;
        protected final byte[] type2Message;
        // Response flags from the type2 message
        protected final int type2Flags;

        protected final byte[] domainBytes;
        protected final byte[] hostBytes;
        protected final byte[] userBytes;

        protected byte[] lmResp;
        protected byte[] ntResp;
        protected final byte[] sessionKey;
        protected final byte[] exportedSessionKey;

        protected final boolean computeMic;

        /** More primitive constructor: don't include cert or previous messages.
        */
        Type3Message(final String domain,
            final String host,
            final String user,
            final String password,
            final byte[] nonce,
            final int type2Flags,
            final String target,
            final byte[] targetInformation)
            throws AuthenticationException {
            this(domain, host, user, password, nonce, type2Flags, target, targetInformation, null, null, null);
        }

        /** More primitive constructor: don't include cert or previous messages.
        */
        Type3Message(final Random random, final long currentTime,
            final String domain,
            final String host,
            final String user,
            final String password,
            final byte[] nonce,
            final int type2Flags,
            final String target,
            final byte[] targetInformation)
            throws AuthenticationException {
            this(random, currentTime, domain, host, user, password, nonce, type2Flags, target, targetInformation, null, null, null);
        }

        /** Constructor. Pass the arguments we will need */
        Type3Message(final String domain,
            final String host,
            final String user,
            final String password,
            final byte[] nonce,
            final int type2Flags,
            final String target,
            final byte[] targetInformation,
            final Certificate peerServerCertificate,
            final byte[] type1Message,
            final byte[] type2Message)
            throws AuthenticationException {
            this(RND_GEN, System.currentTimeMillis(), domain, host, user, password, nonce, type2Flags, target, targetInformation, peerServerCertificate, type1Message, type2Message);
        }

        /** Constructor. Pass the arguments we will need */
        Type3Message(final Random random, final long currentTime,
            final String domain,
            final String host,
            final String user,
            final String password,
            final byte[] nonce,
            final int type2Flags,
            final String target,
            final byte[] targetInformation,
            final Certificate peerServerCertificate,
            final byte[] type1Message,
            final byte[] type2Message)
            throws AuthenticationException {

            if (random == null) {
                throw new AuthenticationException("Random generator not available");
            }

            // Save the flags
            this.type2Flags = type2Flags;
            this.type1Message = type1Message;
            this.type2Message = type2Message;

            // Strip off domain name from the host!
            final String unqualifiedHost = convertHost(host);
            // Use only the base domain name!
            final String unqualifiedDomain = convertDomain(domain);

            byte[] responseTargetInformation = targetInformation;
            if (peerServerCertificate != null) {
                responseTargetInformation = addGssMicAvsToTargetInfo(targetInformation, peerServerCertificate);
                computeMic = true;
            } else {
                computeMic = false;
            }

             // Create a cipher generator class.  Use domain BEFORE it gets modified!
            final CipherGen gen = new CipherGen(random, currentTime,
                unqualifiedDomain,
                user,
                password,
                nonce,
                target,
                responseTargetInformation);

            // Use the new code to calculate the responses, including v2 if that
            // seems warranted.
            byte[] userSessionKey;
            try {
                // This conditional may not work on Windows Server 2008 R2 and above, where it has not yet
                // been tested
                if (((type2Flags & FLAG_TARGETINFO_PRESENT) != 0) &&
                    targetInformation != null && target != null) {
                    // NTLMv2
                    ntResp = gen.getNTLMv2Response();
                    lmResp = gen.getLMv2Response();
                    if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                        userSessionKey = gen.getLanManagerSessionKey();
                    } else {
                        userSessionKey = gen.getNTLMv2UserSessionKey();
                    }
                } else {
                    // NTLMv1
                    if ((type2Flags & FLAG_REQUEST_NTLM2_SESSION) != 0) {
                        // NTLM2 session stuff is requested
                        ntResp = gen.getNTLM2SessionResponse();
                        lmResp = gen.getLM2SessionResponse();
                        if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                            userSessionKey = gen.getLanManagerSessionKey();
                        } else {
                            userSessionKey = gen.getNTLM2SessionResponseUserSessionKey();
                        }
                    } else {
                        ntResp = gen.getNTLMResponse();
                        lmResp = gen.getLMResponse();
                        if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                            userSessionKey = gen.getLanManagerSessionKey();
                        } else {
                            userSessionKey = gen.getNTLMUserSessionKey();
                        }
                    }
                }
            } catch (final AuthenticationException e) {
                // This likely means we couldn't find the MD4 hash algorithm -
                // fail back to just using LM
                ntResp = new byte[0];
                lmResp = gen.getLMResponse();
                if ((type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) != 0) {
                    userSessionKey = gen.getLanManagerSessionKey();
                } else {
                    userSessionKey = gen.getLMUserSessionKey();
                }
            }

            if ((type2Flags & FLAG_REQUEST_SIGN) != 0) {
                if ((type2Flags & FLAG_REQUEST_EXPLICIT_KEY_EXCH) != 0) {
                    exportedSessionKey = gen.getSecondaryKey();
                    sessionKey = RC4(exportedSessionKey, userSessionKey);
                } else {
                    sessionKey = userSessionKey;
                    exportedSessionKey = sessionKey;
                }
            } else {
                if (computeMic) {
                    throw new AuthenticationException("Cannot sign/seal: no exported session key");
                }
                sessionKey = null;
                exportedSessionKey = null;
            }
            final Charset charset = getCharset(type2Flags);
            hostBytes = unqualifiedHost != null ? unqualifiedHost.getBytes(charset) : null;
             domainBytes = unqualifiedDomain != null ? unqualifiedDomain
                .toUpperCase(Locale.ROOT).getBytes(charset) : null;
            userBytes = user.getBytes(charset);
        }

        public byte[] getEncryptedRandomSessionKey() {
            return sessionKey;
        }

        public byte[] getExportedSessionKey() {
            return exportedSessionKey;
        }

        /** Assemble the response */
        @Override
        protected void buildMessage() {
            final int ntRespLen = ntResp.length;
            final int lmRespLen = lmResp.length;

            final int domainLen = domainBytes != null ? domainBytes.length : 0;
            final int hostLen = hostBytes != null ? hostBytes.length: 0;
            final int userLen = userBytes.length;
            final int sessionKeyLen;
            if (sessionKey != null) {
                sessionKeyLen = sessionKey.length;
            } else {
                sessionKeyLen = 0;
            }

            // Calculate the layout within the packet
            final int lmRespOffset = 72 + // allocate space for the version
                ( computeMic ? 16 : 0 ); // and MIC
            final int ntRespOffset = lmRespOffset + lmRespLen;
            final int domainOffset = ntRespOffset + ntRespLen;
            final int userOffset = domainOffset + domainLen;
            final int hostOffset = userOffset + userLen;
            final int sessionKeyOffset = hostOffset + hostLen;
            final int finalLength = sessionKeyOffset + sessionKeyLen;

            // Start the response. Length includes signature and type
            prepareResponse(finalLength, 3);

            // LM Resp Length (twice)
            addUShort(lmRespLen);
            addUShort(lmRespLen);

            // LM Resp Offset
            addULong(lmRespOffset);

            // NT Resp Length (twice)
            addUShort(ntRespLen);
            addUShort(ntRespLen);

            // NT Resp Offset
            addULong(ntRespOffset);

            // Domain length (twice)
            addUShort(domainLen);
            addUShort(domainLen);

            // Domain offset.
            addULong(domainOffset);

            // User Length (twice)
            addUShort(userLen);
            addUShort(userLen);

            // User offset
            addULong(userOffset);

            // Host length (twice)
            addUShort(hostLen);
            addUShort(hostLen);

            // Host offset
            addULong(hostOffset);

            // Session key length (twice)
            addUShort(sessionKeyLen);
            addUShort(sessionKeyLen);

            // Session key offset
            addULong(sessionKeyOffset);

            // Flags.
            addULong(
                    /*
                    //FLAG_WORKSTATION_PRESENT |
                    //FLAG_DOMAIN_PRESENT |

                    // Required flags
                    (type2Flags & FLAG_REQUEST_LAN_MANAGER_KEY) |
                    (type2Flags & FLAG_REQUEST_NTLMv1) |
                    (type2Flags & FLAG_REQUEST_NTLM2_SESSION) |

                    // Protocol version request
                    FLAG_REQUEST_VERSION |

                    // Recommended privacy settings
                    (type2Flags & FLAG_REQUEST_ALWAYS_SIGN) |
                    (type2Flags & FLAG_REQUEST_SEAL) |
                    (type2Flags & FLAG_REQUEST_SIGN) |

                    // These must be set according to documentation, based on use of SEAL above
                    (type2Flags & FLAG_REQUEST_128BIT_KEY_EXCH) |
                    (type2Flags & FLAG_REQUEST_56BIT_ENCRYPTION) |
                    (type2Flags & FLAG_REQUEST_EXPLICIT_KEY_EXCH) |

                    (type2Flags & FLAG_TARGETINFO_PRESENT) |
                    (type2Flags & FLAG_REQUEST_UNICODE_ENCODING) |
                    (type2Flags & FLAG_REQUEST_TARGET)
                        */
                type2Flags
            );

            // Version
            addUShort(0x0105);
            // Build
            addULong(2600);
            // NTLM revision
            addUShort(0x0f00);

            int micPosition = -1;
            if ( computeMic ) {
                micPosition = currentOutputPosition;
                currentOutputPosition += 16;
            }

            // Add the actual data
            addBytes(lmResp);
            addBytes(ntResp);
            addBytes(domainBytes);
            addBytes(userBytes);
            addBytes(hostBytes);
            if (sessionKey != null) {
                addBytes(sessionKey);
            }

            // Write the mic back into its slot in the message

            if (computeMic) {
                // Computation of message integrity code (MIC) as specified in [MS-NLMP] section 3.2.5.1.2.
                final HMACMD5 hmacMD5 = new HMACMD5( exportedSessionKey );
                hmacMD5.update( type1Message );
                hmacMD5.update( type2Message );
                hmacMD5.update( messageContents );
                final byte[] mic = hmacMD5.getOutput();
                System.arraycopy( mic, 0, messageContents, micPosition, mic.length );
            }
        }

        /**
         * Add GSS channel binding hash and MIC flag to the targetInfo.
         * Looks like this is needed if we want to use exported session key for GSS wrapping.
         */
        private byte[] addGssMicAvsToTargetInfo( final byte[] originalTargetInfo,
            final Certificate peerServerCertificate ) throws AuthenticationException
        {
            final byte[] newTargetInfo = new byte[originalTargetInfo.length + 8 + 20];
            final int appendLength = originalTargetInfo.length - 4; // last tag is MSV_AV_EOL, do not copy that
            System.arraycopy( originalTargetInfo, 0, newTargetInfo, 0, appendLength );
            writeUShort( newTargetInfo, MSV_AV_FLAGS, appendLength );
            writeUShort( newTargetInfo, 4, appendLength + 2 );
            writeULong( newTargetInfo, MSV_AV_FLAGS_MIC, appendLength + 4 );
            writeUShort( newTargetInfo, MSV_AV_CHANNEL_BINDINGS, appendLength + 8 );
            writeUShort( newTargetInfo, 16, appendLength + 10 );

            final byte[] channelBindingsHash;
            try
            {
                final byte[] certBytes = peerServerCertificate.getEncoded();
                final MessageDigest sha256 = MessageDigest.getInstance( "SHA-256" );
                final byte[] certHashBytes = sha256.digest( certBytes );
                final byte[] channelBindingStruct = new byte[16 + 4 + MAGIC_TLS_SERVER_ENDPOINT.length
                    + certHashBytes.length];
                writeULong( channelBindingStruct, 0x00000035, 16 );
                System.arraycopy( MAGIC_TLS_SERVER_ENDPOINT, 0, channelBindingStruct, 20,
                    MAGIC_TLS_SERVER_ENDPOINT.length );
                System.arraycopy( certHashBytes, 0, channelBindingStruct, 20 + MAGIC_TLS_SERVER_ENDPOINT.length,
                    certHashBytes.length );
                final MessageDigest md5 = getMD5();
                channelBindingsHash = md5.digest( channelBindingStruct );
            }
            catch ( final CertificateEncodingException e )
            {
                throw new AuthenticationException( e.getMessage(), e );
            }
            catch ( final NoSuchAlgorithmException e )
            {
                throw new AuthenticationException( e.getMessage(), e );
            }

            System.arraycopy( channelBindingsHash, 0, newTargetInfo, appendLength + 12, 16 );
            return newTargetInfo;
         }

    }

    static void writeUShort(final byte[] buffer, final int value, final int offset) {
        buffer[offset] = ( byte ) ( value & 0xff );
        buffer[offset + 1] = ( byte ) ( value >> 8 & 0xff );
    }

    static void writeULong(final byte[] buffer, final int value, final int offset) {
        buffer[offset] = (byte) (value & 0xff);
        buffer[offset + 1] = (byte) (value >> 8 & 0xff);
        buffer[offset + 2] = (byte) (value >> 16 & 0xff);
        buffer[offset + 3] = (byte) (value >> 24 & 0xff);
    }

    static int F(final int x, final int y, final int z) {
        return ((x & y) | (~x & z));
    }

    static int G(final int x, final int y, final int z) {
        return ((x & y) | (x & z) | (y & z));
    }

    static int H(final int x, final int y, final int z) {
        return (x ^ y ^ z);
    }

    static int rotintlft(final int val, final int numbits) {
        return ((val << numbits) | (val >>> (32 - numbits)));
    }

    static MessageDigest getMD5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (final NoSuchAlgorithmException ex) {
            throw new RuntimeException("MD5 message digest doesn't seem to exist - fatal error: "+ex.getMessage(), ex);
        }
    }

    /**
     * Cryptography support - MD4. The following class was based loosely on the
     * RFC and on code found at http://www.cs.umd.edu/~harry/jotp/src/md.java.
     * Code correctness was verified by looking at MD4.java from the jcifs
     * library (http://jcifs.samba.org). It was massaged extensively to the
     * final form found here by Karl Wright (kwright@metacarta.com).
     */
    static class MD4 {
        protected int A = 0x67452301;
        protected int B = 0xefcdab89;
        protected int C = 0x98badcfe;
        protected int D = 0x10325476;
        protected long count = 0L;
        protected final byte[] dataBuffer = new byte[64];

        MD4() {
        }

        void update(final byte[] input) {
            // We always deal with 512 bits at a time. Correspondingly, there is
            // a buffer 64 bytes long that we write data into until it gets
            // full.
            int curBufferPos = (int) (count & 63L);
            int inputIndex = 0;
            while (input.length - inputIndex + curBufferPos >= dataBuffer.length) {
                // We have enough data to do the next step. Do a partial copy
                // and a transform, updating inputIndex and curBufferPos
                // accordingly
                final int transferAmt = dataBuffer.length - curBufferPos;
                System.arraycopy(input, inputIndex, dataBuffer, curBufferPos, transferAmt);
                count += transferAmt;
                curBufferPos = 0;
                inputIndex += transferAmt;
                processBuffer();
            }

            // If there's anything left, copy it into the buffer and leave it.
            // We know there's not enough left to process.
            if (inputIndex < input.length) {
                final int transferAmt = input.length - inputIndex;
                System.arraycopy(input, inputIndex, dataBuffer, curBufferPos, transferAmt);
                count += transferAmt;
                curBufferPos += transferAmt;
            }
        }

        byte[] getOutput() {
            // Feed pad/length data into engine. This must round out the input
            // to a multiple of 512 bits.
            final int bufferIndex = (int) (count & 63L);
            final int padLen = (bufferIndex < 56) ? (56 - bufferIndex) : (120 - bufferIndex);
            final byte[] postBytes = new byte[padLen + 8];
            // Leading 0x80, specified amount of zero padding, then length in
            // bits.
            postBytes[0] = (byte) 0x80;
            // Fill out the last 8 bytes with the length
            for (int i = 0; i < 8; i++) {
                postBytes[padLen + i] = (byte) ((count * 8) >>> (8 * i));
            }

            // Update the engine
            update(postBytes);

            // Calculate final result
            final byte[] result = new byte[16];
            writeULong(result, A, 0);
            writeULong(result, B, 4);
            writeULong(result, C, 8);
            writeULong(result, D, 12);
            return result;
        }

        protected void processBuffer() {
            // Convert current buffer to 16 ulongs
            final int[] d = new int[16];

            for (int i = 0; i < 16; i++) {
                d[i] = (dataBuffer[i * 4] & 0xff) + ((dataBuffer[i * 4 + 1] & 0xff) << 8)
                        + ((dataBuffer[i * 4 + 2] & 0xff) << 16)
                        + ((dataBuffer[i * 4 + 3] & 0xff) << 24);
            }

            // Do a round of processing
            final int AA = A;
            final int BB = B;
            final int CC = C;
            final int DD = D;
            round1(d);
            round2(d);
            round3(d);
            A += AA;
            B += BB;
            C += CC;
            D += DD;

        }

        protected void round1(final int[] d) {
            A = rotintlft((A + F(B, C, D) + d[0]), 3);
            D = rotintlft((D + F(A, B, C) + d[1]), 7);
            C = rotintlft((C + F(D, A, B) + d[2]), 11);
            B = rotintlft((B + F(C, D, A) + d[3]), 19);

            A = rotintlft((A + F(B, C, D) + d[4]), 3);
            D = rotintlft((D + F(A, B, C) + d[5]), 7);
            C = rotintlft((C + F(D, A, B) + d[6]), 11);
            B = rotintlft((B + F(C, D, A) + d[7]), 19);

            A = rotintlft((A + F(B, C, D) + d[8]), 3);
            D = rotintlft((D + F(A, B, C) + d[9]), 7);
            C = rotintlft((C + F(D, A, B) + d[10]), 11);
            B = rotintlft((B + F(C, D, A) + d[11]), 19);

            A = rotintlft((A + F(B, C, D) + d[12]), 3);
            D = rotintlft((D + F(A, B, C) + d[13]), 7);
            C = rotintlft((C + F(D, A, B) + d[14]), 11);
            B = rotintlft((B + F(C, D, A) + d[15]), 19);
        }

        protected void round2(final int[] d) {
            A = rotintlft((A + G(B, C, D) + d[0] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[4] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[8] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[12] + 0x5a827999), 13);

            A = rotintlft((A + G(B, C, D) + d[1] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[5] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[9] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[13] + 0x5a827999), 13);

            A = rotintlft((A + G(B, C, D) + d[2] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[6] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[10] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[14] + 0x5a827999), 13);

            A = rotintlft((A + G(B, C, D) + d[3] + 0x5a827999), 3);
            D = rotintlft((D + G(A, B, C) + d[7] + 0x5a827999), 5);
            C = rotintlft((C + G(D, A, B) + d[11] + 0x5a827999), 9);
            B = rotintlft((B + G(C, D, A) + d[15] + 0x5a827999), 13);

        }

        protected void round3(final int[] d) {
            A = rotintlft((A + H(B, C, D) + d[0] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[8] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[4] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[12] + 0x6ed9eba1), 15);

            A = rotintlft((A + H(B, C, D) + d[2] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[10] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[6] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[14] + 0x6ed9eba1), 15);

            A = rotintlft((A + H(B, C, D) + d[1] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[9] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[5] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[13] + 0x6ed9eba1), 15);

            A = rotintlft((A + H(B, C, D) + d[3] + 0x6ed9eba1), 3);
            D = rotintlft((D + H(A, B, C) + d[11] + 0x6ed9eba1), 9);
            C = rotintlft((C + H(D, A, B) + d[7] + 0x6ed9eba1), 11);
            B = rotintlft((B + H(C, D, A) + d[15] + 0x6ed9eba1), 15);

        }

    }

    /**
     * Cryptography support - HMACMD5 - algorithmically based on various web
     * resources by Karl Wright
     */
    static class HMACMD5 {
        protected final byte[] ipad;
        protected final byte[] opad;
        protected final MessageDigest md5;

        HMACMD5(final byte[] input) {
            byte[] key = input;
            md5 = getMD5();

            // Initialize the pad buffers with the key
            ipad = new byte[64];
            opad = new byte[64];

            int keyLength = key.length;
            if (keyLength > 64) {
                // Use MD5 of the key instead, as described in RFC 2104
                md5.update(key);
                key = md5.digest();
                keyLength = key.length;
            }
            int i = 0;
            while (i < keyLength) {
                ipad[i] = (byte) (key[i] ^ (byte) 0x36);
                opad[i] = (byte) (key[i] ^ (byte) 0x5c);
                i++;
            }
            while (i < 64) {
                ipad[i] = (byte) 0x36;
                opad[i] = (byte) 0x5c;
                i++;
            }

            // Very important: processChallenge the digest with the ipad buffer
            md5.reset();
            md5.update(ipad);

        }

        /** Grab the current digest. This is the "answer". */
        byte[] getOutput() {
            final byte[] digest = md5.digest();
            md5.update(opad);
            return md5.digest(digest);
        }

        /** Update by adding a complete array */
        void update(final byte[] input) {
            md5.update(input);
        }

        /** Update the algorithm */
        void update(final byte[] input, final int offset, final int length) {
            md5.update(input, offset, length);
        }

    }

    public String generateType1Msg(
            final String domain,
            final String workstation) throws AuthenticationException {
        return getType1Message(workstation, domain);
    }

    public String generateType3Msg(
            final String username,
            final String password,
            final String domain,
            final String workstation,
            final String challenge) throws AuthenticationException {
        final Type2Message t2m = new Type2Message(challenge);
        return getType3Message(
                username,
                password,
                workstation,
                domain,
                t2m.getChallenge(),
                t2m.getFlags(),
                t2m.getTarget(),
                t2m.getTargetInfo());
    }

}
