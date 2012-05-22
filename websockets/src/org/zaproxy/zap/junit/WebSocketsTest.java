package org.zaproxy.zap.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import org.apache.commons.httpclient.HttpClient;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.ZapGetMethod;
import org.zaproxy.zap.ZapHttpConnectionManager;
import org.zaproxy.zap.extension.websocket.ExtensionWebSocket;

/**
 * This test uses the Echo Server from websockets.org
 * for testing a valid WebSockets connection.
 */
public class WebSocketsTest extends BaseZapProxyTest {

	@BeforeClass
	public static void setup() throws Exception {
		System.getProperties().setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
		BaseZapProxyTest.setup();
		
		// load WebSockets extension
		Control.initSingletonForTesting();
		Control.getSingleton().getExtensionLoader().addExtension(new ExtensionWebSocket());
	}

	@Test
	public void doWebSocketsHandshakeViaClient() throws Exception {    	
		// use HTTP-client with custom connection manager
		// that allows us to expose the SocketChannel
		HttpClient client = new HttpClient(new ZapHttpConnectionManager());
	    client.getHostConfiguration().setProxy(PROXY_HOST, PROXY_PORT);
	    
	    // create minimal HTTP request
	    ZapGetMethod method = new ZapGetMethod("http://echo.websocket.org/?encoding=text");  
		method.addRequestHeader("Connection", "upgrade");
		method.addRequestHeader("Upgrade", "websocket");
		method.addRequestHeader("Sec-WebSocket-Version", "13");
		method.addRequestHeader("Sec-WebSocket-Key", "5d5NazNjJ5hafSgFYJ7SOw==");
		client.executeMethod(method);
		
		int status = method.getStatusCode();
		assertEquals("HTTP status code of WebSockets-handshake response should be 101.", 101, status);

		Socket socket = method.getUpgradedConnection();

		assertWorkingWebSocket(socket);
		
		// send hello message a second time to ensure that socket is not closed after first time
		assertWorkingWebSocket(socket);
		
		properlyCloseWebSocket(socket);
	}

	@Test
	public void doSecureWebSocketsHandshake() throws Exception {
		// use HTTP-client with custom connection manager
		// that allows us to expose the SocketChannel
		HttpClient client = new HttpClient(new ZapHttpConnectionManager());
	    client.getHostConfiguration().setProxy(PROXY_HOST, PROXY_PORT);
	    
	    // create minimal HTTP handshake request
	    ZapGetMethod method = new ZapGetMethod("https://echo.websocket.org/?encoding=text");
		method.addRequestHeader("Connection", "upgrade");
		method.addRequestHeader("Upgrade", "websocket");
		method.addRequestHeader("Sec-WebSocket-Version", "13");
		method.addRequestHeader("Sec-WebSocket-Key", "5d5NazNjJ5hafSgFYJ7SOw==");
		client.executeMethod(method);
		
		assertEquals("HTTP status code of WebSockets-handshake response should be 101.", 101, method.getStatusCode());
		
		Socket socket = method.getUpgradedConnection();
		
		assertWorkingWebSocket(socket);
		
		properlyCloseWebSocket(socket);
	}
	
//	/**
//	 * Cannot use this SOCKS approach, as ZAP does not support SOCKS.
//	 * So I had to use the HttpClient for that purpose. Another try
//	 * to work with UrlConnection failed, as I was not able to set
//   * custom HTTP headers (I was only able to override existing ones).
//   * 
//	 * @throws IOException
//	 */
//	@Test
//	public void doWebSocketsHandshakeViaSocks() throws IOException {
//		Socket socket = new Socket(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(PROXY_HOST, PROXY_PORT)));
//		socket.connect(new InetSocketAddress("echo.websocket.org", 80), 1000);
//		
//		PrintWriter writer = new PrintWriter(socket.getOutputStream ());
//	    BufferedReader reader = new BufferedReader(new InputStreamReader (socket.getInputStream ()));
//
//	    writer.print("GET /?encoding=text HTTP/1.1\r\n");
//	    writer.print("Host: echo.websocket.org\r\n");
//	    writer.print("Connection: keep-alive, Upgrade\r\n");
//	    writer.print("Sec-WebSocket-Version: 13\r\n");
//	    writer.print("Sec-WebSocket-Key: 5d5NazNjJ5hafSgFYJ7SOw==\r\n");
//	    writer.print("Upgrade: websocket\r\n");
//	    writer.print("\r\n");
//	    writer.flush();
//
//	    assertEquals("HTTP/1.1 101 Web Socket Protocol Handshake", reader.readLine());
//	    while(!reader.readLine().isEmpty()) {
//	    	// do something with response
//	    }
//	    
//	    socket.close();
//	    reader.close();
//	    writer.close();	
//	}
	
	private void properlyCloseWebSocket(Socket socket) throws IOException {
		socket.setTcpNoDelay(true);
		assertTrue("Retrieved SocketChannel should not be null.", socket != null);
		
		byte[] maskedClose = {(byte) 0x88, (byte) 0x82, 0x46, 0x59, (byte) 0xdc, 0x4a, 0x45, (byte) 0xb1};
		
		socket.getOutputStream().write(maskedClose);
		socket.close();
	}

	/**
	 * Sends a Hello message into the channel and asserts that
	 * the same message is returned by the Echo-Server.
	 * The outgoing message is masked, while the incoming
	 * contains the message in cleartext.
	 * 
	 * @param socket
	 * @throws IOException
	 */
	private void assertWorkingWebSocket(Socket socket) throws IOException {
		socket.setTcpNoDelay(true);
		assertTrue("Retrieved SocketChannel should not be null.", socket != null);
		
		byte[] maskedHelloMessage = {(byte) 0x81, (byte) 0x85, 0x37, (byte) 0xfa, 0x21, 0x3d, 0x7f, (byte) 0x9f, 0x4d, 0x51, 0x58};
		byte[] unmaskedHelloMessage = {(byte) 0x81, 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f};
		
		socket.getOutputStream().write(maskedHelloMessage);
		
		byte[] dst = new byte[7];
		socket.getInputStream().read(dst);
		
		// use Arrays class to compare two byte arrays
		// returns true if it contains the same elements in same order
		assertTrue("Awaited unmasked hello message from echo server.", Arrays.equals(unmaskedHelloMessage, dst));
	}
}
