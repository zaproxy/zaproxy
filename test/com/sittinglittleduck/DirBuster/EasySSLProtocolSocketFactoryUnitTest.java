package com.sittinglittleduck.DirBuster;

import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.junit.*;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

import java.net.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class EasySSLProtocolSocketFactoryUnitTest {

	private EasySSLProtocolSocketFactory socketFactory;

	private static Connection hostConnection;

	@BeforeClass
	public static void startEmbeddedHttpServers() throws Exception {
		hostConnection = new SocketConnection(new ContainerServer(null));
		hostConnection.connect(new InetSocketAddress(18080));
	}

	@AfterClass
	public static void stopEmbeddedHttpServers() throws Exception {
		hostConnection.close();
	}

	@Before
	public void resetSocketFactory() throws Exception {
		socketFactory = new EasySSLProtocolSocketFactory();
	}

	@Test
	public void shouldCreateSocketForGivenHostAndPort() throws Exception {
		// Given
		String host = "localhost";
		int port = 18080;
		// When
		Socket sslSocket = socketFactory.createSocket(host, port);
		// Then
		assertThat(sslSocket.getInetAddress().getHostName(), is(equalTo(host)));
		assertThat(sslSocket.getPort(), is(equalTo(port)));
	}

	// Note that on some platforms this gives a ConnectionException while on others it give a UnknownHostException 
	@Test(expected = java.io.IOException.class)
	public void shouldFailCreatingSocketForUnknownHost() throws Exception {
		// Given
		String unknownHost = "localhorst";
		InetAddress localAddress = InetAddress.getLoopbackAddress();
		int localPort = 28080;
		HttpConnectionParams params = new HttpConnectionParams();
		params.setConnectionTimeout(60000);
		// When
		socketFactory.createSocket(unknownHost, 18080, localAddress, localPort, params);
		// Then = IOException
	}

	@Test(expected = ConnectException.class)
	public void shouldFailCreatingSocketForUnknownPort() throws Exception {
		// Given
		int unknownPort = 12345;
		// When
		socketFactory.createSocket("localhost", unknownPort);
		// Then = ConnectException
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldFailCreatingSocketForMissingParameters() throws Exception {
		// Given
		HttpConnectionParams nullParams = null;
		// When
		socketFactory.createSocket("localhost", 18080, InetAddress.getLoopbackAddress(), 12345, nullParams);
		// Then = IllegalArgumentException
	}

	@Test
	public void shouldCreateSocketWithGivenLocalAddressAndPort() throws Exception {
		// Given
		InetAddress localAddress = InetAddress.getLoopbackAddress();
		int localPort = 28080;
		// When
		Socket sslSocket = socketFactory.createSocket("localhost", 18080, localAddress, localPort, new HttpConnectionParams());
		// Then
		assertThat(sslSocket.getLocalAddress(), is(equalTo(localAddress)));
		assertThat(sslSocket.getLocalPort(), is(equalTo(localPort)));
	}

	@Test(expected = SocketTimeoutException.class)
	@Ignore // TODO Won't work unless we figure out a way to slow down connect process artificially
	public void shouldFailCreatingSocketWithInstantTimeout() throws Exception {
		// Given
		HttpConnectionParams params = new HttpConnectionParams();
		params.setConnectionTimeout(1);
		// When
		socketFactory.createSocket("localhost", 18080, InetAddress.getLoopbackAddress(), 38080, params);
		// Then = SocketTimeoutException
	}

	@Test
	public void shouldSucceedCreatingSocketWithReasonableTimeout() throws Exception {
		// Given
		HttpConnectionParams params = new HttpConnectionParams();
		params.setConnectionTimeout(1000);
		// When
		Socket sslSocket = socketFactory.createSocket("localhost", 18080, InetAddress.getLoopbackAddress(), 48080, params);
		// Then
		assertThat(sslSocket, is(notNullValue()));
	}

}
