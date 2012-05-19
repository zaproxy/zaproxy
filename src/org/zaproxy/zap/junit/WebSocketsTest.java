package org.zaproxy.zap.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.parosproxy.paros.control.Control;
import org.zaproxy.zap.ZapDefaultProtocolSocketFactory;
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
		// set up custom socket factory that creates sockets
		// via SocketChannel.open().socket()
		// allows to retrieve channel later
		ProtocolSocketFactory defaultSocketFactory = new ZapDefaultProtocolSocketFactory();
    	Protocol.registerProtocol("http", new Protocol("http", defaultSocketFactory, 80));
    	
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
		
		assertWorkingWebSocket(method);
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
	
	/**
	 * Sends a Hello message into the channel and asserts that
	 * the same message is returned by the Echo-Server.
	 * The outgoing message is masked, while the incoming
	 * contains the message in cleartext.
	 * 
	 * @param method
	 * @throws IOException
	 */
	private void assertWorkingWebSocket(ZapGetMethod method) throws IOException {
		SocketChannel channel = method.getUpgradedChannel();
		assertTrue("Retrieved SocketChannel should not be null.", channel != null);
		
		byte[] maskedHelloMessage = {(byte) 0x81, (byte) 0x85, 0x37, (byte) 0xfa, 0x21, 0x3d, 0x7f, (byte) 0x9f, 0x4d, 0x51, 0x58};
		byte[] unmaskedHelloMessage = {(byte) 0x81, 0x05, 0x48, 0x65, 0x6c, 0x6c, 0x6f};
		
		channel.write(ByteBuffer.wrap(maskedHelloMessage));
		
		ByteBuffer dst = ByteBuffer.allocate(10);
		channel.read(dst);
		dst.flip();
		
		assertEquals("Awaited unmasked hello message from echo server.", ByteBuffer.wrap(unmaskedHelloMessage), dst);
	}

	@Test
	public void doSecureWebSocketsHandshake() throws Exception {
		// set up custom socket factory that creates sockets
		// via SocketChannel.open().socket()
		// allows to retrieve channel later
		ProtocolSocketFactory defaultSocketFactory = new ZapDefaultProtocolSocketFactory();
    	Protocol.registerProtocol("http", new Protocol("http", defaultSocketFactory, 80));
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
		assertTrue("SocketChannel must not be null", method.getUpgradedChannel() != null);
		
		assertWorkingWebSocket(method);
	}
}
