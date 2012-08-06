/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.junit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.parosproxy.paros.core.proxy.ProxyServer;
import org.parosproxy.paros.network.SSLConnector;
import org.parosproxy.paros.security.SslCertificateService;
import org.parosproxy.paros.security.SslCertificateServiceImpl;

/**
 * Provides basic infrastructure. See {@link WebSocketsTest} for an example.
 */
public abstract class BaseZapProxyTest {

	protected ProxyServer proxyServer;
	
	protected static String PROXY_HOST = "127.0.0.1";
	protected static int PROXY_PORT = 8087;

	@Before
	public void start() throws Exception {
		proxyServer = new ProxyServer();
		proxyServer.startServer(PROXY_HOST, PROXY_PORT, false);
	}

	@After
	public void stop() {
		proxyServer.stopServer();
	}
	
	@BeforeClass
	public static void setup() throws Exception {
		initializeLogger();
		initializeProxySettings();
		initializeLocalSecurity();
		initializeRootCertificate();
	}
	
	/**
	 * Set up logging levels, to ease debugging of tests.
	 * @throws IOException 
	 */
	protected static void initializeLogger() throws IOException {
//		systemProperties.setProperty("javax.net.debug", "ssl");
		
		Logger rootLogger = Logger.getRootLogger();
		if (!rootLogger.getAllAppenders().hasMoreElements()) {
			rootLogger.setLevel(Level.DEBUG);
			rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-5p [%t]: %m%n")));

			Logger httpClientLogger = rootLogger.getLoggerRepository().getLogger("org.apache.commons.httpclient");
			httpClientLogger.setLevel(Level.DEBUG);

			Logger httpWireLogger = rootLogger.getLoggerRepository().getLogger("org.apache.commons.wire");
//			httpWireLogger.addAppender(new FileAppender(new PatternLayout("%5p [%c] %m%n"), "wire.log"));
			httpWireLogger.setLevel(Level.DEBUG);
		}
	}

	/**
	 * Set system property values for proxy settings.
	 * These settings will be used by Http(s)URLConnection.
	 */
	protected static void initializeProxySettings() {
		Properties systemProperties = System.getProperties();
		systemProperties.setProperty("http.proxyHost", PROXY_HOST);
		systemProperties.setProperty("http.proxyPort", PROXY_PORT+"");
		systemProperties.setProperty("https.proxyHost", PROXY_HOST);
		systemProperties.setProperty("https.proxyPort", PROXY_PORT+"");
	}

	/**
	 * Use custom TrustManager that trusts everything.
	 * Moreover setup custom ProtocolSocketFactory as done in ZAP.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	protected static void initializeLocalSecurity() throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance("SSL");

		// set up a TrustManager that trusts everything
		sslContext.init( null, new TrustManager[]
		    {
		        new X509TrustManager()
		        {
					@Override
					public void checkClientTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						// everything is trusted
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain,
							String authType) throws CertificateException {
						// everything is trusted
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
		        }
		    }, new SecureRandom());

		// this doesn't seem to apply to connections through a proxy
		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

		// setup a hostname verifier that verifies everything
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		
		Protocol.registerProtocol("https", new Protocol("https", (ProtocolSocketFactory) new SSLConnector(), 443));
	}

	/**
	 * Helper method to set the root certificate for this test.
	 * 
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws CertificateException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 */
	protected static void initializeRootCertificate() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException {
		final SslCertificateService service = SslCertificateServiceImpl.getService();
		final byte[] bytes = Base64.decodeBase64("_u3-7QAAAAIAAAABAAAAAQARb3dhc3BfemFwX3Jvb3RfY2EAAAE2LA_eqAAABQIwggT-MA4GCisGAQQBKgIRAQEFAASCBOqboCBB1-ZSxYZqYDwdOLd22xkq1R3-ZJMVlt2ad-SMKGTG52RVHNhAfYYUS14QDwD6LF2TxKuHxKQt-CH2QXGZaRtWNzksTLl50J-irz9l7cDNkS-HI7l3ZLK3Hl8Fr_tx3pi91h1uw9qc5xLl0euLZYDo-bnqtqmrvqzBXS7Th0cDa7IDWWTN1foxoyV-26lc9O_abpcTd8ugJqC5_DoDJ1-yW_eIRT4vqiRl1cxcrZGSrA255O21l1CwBxE3y34HqdQf2eDWpPmVDA5qeLTwUWj_AAeXW574jtjvG42sB1mxk5bhSmp53jGBbQqSh4qgvfsnyS54BicWQFtI5NRy0Bb8KuZe4DmPFkhw9fV6ayMC1ZBE8M1mSvhMaHlxq-NWriH0YAoiTd2g66OXFciPVfQJn6ebu7FLq33LjlxADsVnwdGgHbXeJ85vCprB3VxuRbVDGsch0kfGteunHh5ldYI9rbiurVJOr0r4zx76XyCsf-5BqHS_jey8x7udHEq7orkEzngCQ2lxEY64WKMY4coclHC1FVZqyqJVgXHtRZAc-6UFV1QSCeNBiBaQKtLp9UOS8UbkK4qGvfFOpaGHu14fuM-qcwYnYTrONmNfi6tDdxuLMSPoHGtyMpSwn8yz6g-iHWMBcTccThCmeF40Ns23pjiwDD1weTCEVJwq8oN0v_CUVyysMCqSACtQK9U89R3e_bv-b2losQvVEEzi_R92sXVMe9VctkVAkGJi8gkgl6H1sFyZ4aAA9iUBT7bMCroUmM5V6si8NGJAwlyoNcNGy5SvQBzhmAV2TjukuUoD28ZlWMSqmKbLw0Zb3BFxxEh-sj5Vrquw8YNywAeAHMk0M5esX8Bdh2hmhcRXvYozMjlurs8k0KvmkEDJa0Jw8SfEgHc1sqB0OU3Fg3uKpHOLGCffOk3LiW5UP6hetIvrRl-tFJokHWxXm7E2ORED1LZNPRUU5g_YcSiHpyADRt_LujL30eNbgRPIly_Ra6D6v3Detgd7iqD63GSSxNNBuZ_faYQrdQAXt-UaMCfJS_0e7vaLtrlcV36OcyT7jPVZdtqq9BzfIBeYqNO-TOjaq_pyrtZ6G21F2ttjFzuA2aHi6f3sGdaQ7GxmDvI6s8InMLsvZNmQo7EvLqzv1gVo4594nwGtv9CnRCM7qyZ9GkDjqFySy3OtdkJSOeGLvmZu0c0KHVmeOT2b3J0IwExUwkZ5cKHVyxMUE7BWcZxYIyf9l-Laxp0jdfhPyamRmvAXjXSP6d6Fds7Xntf5WINGrSDwkNTBgqoYB3DpN2ZAtWEhC5riiEuK-AkeSqszc_0Fcs8OM8q2azAgNC1RXYAT5Spbhn_M01IlDXUxVWyxtt92opDnZiLTLSwSBIeb3Bz_xTvkA5AATP5w8p8XF1XF1aaubiiIQItdg1e1IzOhDfbddV-qoCErfvzV_buFLzq-nzdC6dtRRvBPItEKbEyn4I45H2ekRzTUagj7RJqePXvUKVEHfSKcOduM19-sJlMl8dCBWikJfU2yEwys-1VAeVzHjsanL551M6A3X8SWde27wvIvuRL6BEa_3vJo-ggoF6uBFpKZvmqMnFuZyC5sEsEcGhNjnVtt8Cw8VDov9prSf1T5ZmCPlk1Twa0KAjw7quLMEcCVRpPmD91npCnhXKR8TnBexk5MAAAAAQAFWC41MDkAAAPiMIID3jCCAsagAwIBAgIFAMr-ur4wDQYJKoZIhvcNAQEFBQAwgYUxJzAlBgNVBAMMHk9XQVNQIFplZCBBdHRhY2sgUHJveHkgUm9vdCBDQTEZMBcGA1UEBwwQYzhkMmJkODI2ZTZmOWQyMzEWMBQGA1UECgwNT1dBU1AgUm9vdCBDQTEaMBgGA1UECwwRT1dBU1AgWkFQIFJvb3QgQ0ExCzAJBgNVBAYTAnh4MB4XDTEyMDMxOTE3NDQ1OFoXDTEzMDMxOTE3NDQ1OFowgYUxJzAlBgNVBAMMHk9XQVNQIFplZCBBdHRhY2sgUHJveHkgUm9vdCBDQTEZMBcGA1UEBwwQYzhkMmJkODI2ZTZmOWQyMzEWMBQGA1UECgwNT1dBU1AgUm9vdCBDQTEaMBgGA1UECwwRT1dBU1AgWkFQIFJvb3QgQ0ExCzAJBgNVBAYTAnh4MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiUrTsz8fgb0GRo8yikEj7bEg_jCm0BKn_azJIqcJWqCEOhMD38MFDSWOn4LLAZWHLs3YBmwwoyLoNg6aTXj-Cwa6D_NvWSBfMSyqfpFdVFAo3AVfLkWZXoY5cviemr8LRSVAt-2TTYb0JArDY6i23kRFWUSKZcdVMU0hwoq9YAfI_E3FWJ9QH5l_P5EWYK9om349w1Ypl5Y4n1V74yEHeRZo8Fw0BFjk9uLLUTa_NUqEP24q3q7v1MD6-kUZFJJxlyyiGwbtBglxymkMwXKQL1GEPSb0ZOoxjSbDxOUsDjLHW9A8Xh6JQKYL0lMJ6clplrbaNjwtMIM26xJozpRQwQIDAQABo1MwUTAgBgNVHQ4BAf8EFgQUY7QvhPIV67X8kAxEoQTCoyp3N60wDwYDVR0TAQH_BAUwAwEB_zALBgNVHQ8EBAMCAbYwDwYDVR0lBAgwBgYEVR0lADANBgkqhkiG9w0BAQUFAAOCAQEAX5pQ0Pcgy8lEzK3xJaztS3OjUoI1UhJKIqgQeXujXDowMqCLPFHGeN6uVhi4ktjFuun4sfwLBE7CXACl5ZMUpjArD18qdQKQ6glHJ9HYrxKi0lJM2maYt5rkpAhHI8EHnX5IdsDEr2ihoCQUou1h8gGHIe1a6QkeCD_1VrRfKAdwt6UTE6RyaP6nTyr58IZ6Pdw5EQP3FmtA0yXRVapHodQPpudkS24Lj9cxSrpo_gJBSIb-LHpl81TVZg0SAROikcDk69qZDgnbQqLPz6WxsZtWcET1ACOqPHovQIPK6utkyOeOLm-uJL3Iqx84b4yQqnFkG-y-nlDmNWR33DOtE8rTqmBfifcjGibDbx0z26ogkB6I");
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final KeyStore rootca = KeyStore.getInstance(KeyStore.getDefaultType());
		rootca.load(bais, "0w45P.Z4p".toCharArray());
		bais.close();
		service.initializeRootCA(rootca);
	}
}
