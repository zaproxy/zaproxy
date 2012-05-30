package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.parosproxy.paros.common.DynamicLoader;

/**
 * Represents a single WebSocket message, consisting out of at least one frame.
 * This class was created with sight on the WebSocketMessage class of the
 * Monsoon project.
 */
public abstract class WebSocketMessage {
	
	private static final Logger logger = Logger.getLogger(WebSocketMessage.class);

	/**
	 * This list will contain all the original bytes for each frame.
	 */
	protected ArrayList<ByteBuffer> frames = new ArrayList<ByteBuffer>();
	
	/**
	 * Indicates if this message already contains all of its frames.
	 */
	protected boolean isFinished;
	
	// WebSocket OpCodes - int's are used instead of an enum for extensibility.

	// non-control frames (0x3 - 0x7 are reserved for further non-control frames)
	public static final int OPCODE_CONTINUATION = 0x0;
	public static final int OPCODE_TEXT = 0x1;
	public static final int OPCODE_BINARY = 0x2;

	// control frames (0xB - 0xF are reserved for further control frames)
	public static final int OPCODE_CLOSE = 0x8;
	public static final int OPCODE_PING = 0x9;
	public static final int OPCODE_PONG = 0xA;
	
	/**
	 * Indicates a normal closure, meaning that the purpose for
	 * which the connection was established has been fulfilled.
	 */
	public static final int STATUS_CODE_OK = 1000;
	
	/**
	 * Indicates that an endpoint is "going away", such as a server
     * going down or a browser having navigated away from a page.
	 */
	public static final int STATUS_CODE_GOING_AWAY = 1001;
	
	/**
	 * Indicates that an endpoint is terminating the connection due
     * to a protocol error.
	 */
	public static final int STATUS_CODE_PROTOCOL_ERROR = 1002;
	
	/**
	 * Indicates that an endpoint is terminating the connection
     * because it has received a type of data it cannot accept (e.g., an
     * endpoint that understands only text data MAY send this if it
     * receives a binary message).
	 */
	public static final int STATUS_CODE_INVALID_DATA_TYPE = 1003;
	
	// 1004 - 1006 are reserved values
	
	/**
	 * Indicates that an endpoint is terminating the connection
     * because it has received data within a message that was not
     * consistent with the type of the message (e.g., non-UTF-8 [RFC3629]
     * data within a text message).
	 */
	public static final int STATUS_CODE_INVALID_DATA = 1007;
	
	/**
	 * Indicates that an endpoint is terminating the connection
     * because it has received a message that violates its policy.  This
     * is a generic status code that can be returned when there is no
     * other more suitable status code (e.g., 1003 or 1009) or if there
     * is a need to hide specific details about the policy.
	 */
	public static final int STATUS_CODE_POLICY_VIOLATION = 1008;
	
	/**
	 * Indicates that an endpoint is terminating the connection
     * because it has received a message that is too big for it to
     * process.
	 */
	public static final int STATUS_CODE_MESSAGE_TOO_LARGE = 1009;
	
	/**
	 * Indicates that an endpoint (client) is terminating the
     * connection because it has expected the server to negotiate one or
     * more extension, but the server didn't return them in the response
     * message of the WebSocket handshake.  The list of extensions that
     * are needed SHOULD appear in the "reason" part of the Close frame.
     * Note that this status code is not used by the server, because it
     * can fail the WebSocket handshake instead.
	 */
	public static final int STATUS_CODE_EXTENSION_NEGOTIATION_FAILED = 1010;
	
	/**
	 * Indicates that a server is terminating the connection because
     * it encountered an unexpected condition that prevented it from
     * fulfilling the request.
	 */
	public static final int STATUS_CODE_SERVER_ERROR = 1011;

	// 1015 is another reserved status code
	
	/**
	 * The byte stream of WebSocket frames must be valid UTF-8. 
	 */
	protected static final Charset UTF8 = Charset.forName("UTF-8");

	/**
	 * Indicates the opcode of this message.
	 * Initially it is set to -1, meaning that its type is unknown.
	 */
	protected int opcode = -1;

	/**
	 * @return the opcode for this message.
	 */
	public final int getOpcode() {
		return opcode;
	}
	
	/**
	 * @see WebSocketMessage#isBinary(int)
	 */
	public final boolean isBinary() {
		return isBinary(opcode);
	}

	/**
	 * @param opcode
	 * @return Returns true if this message contains binary content.
	 */
	public static final boolean isBinary(int opcode) {
		return opcode == OPCODE_BINARY;
	}
	
	/**
	 * @see WebSocketMessage#isText(int)
	 */
	public final boolean isText() {
		return isText(opcode);
	}

	/**
	 * @param opcode
	 * @return Returns true if this message contains textual content.
	 */
	public static final boolean isText(int opcode) {
		return opcode == OPCODE_TEXT;
	}
	
	/**
	 * @see WebSocketMessage#isControl(int)
	 */
	public final boolean isControl() {
		return isControl(opcode);
	}

	/**
	 * @param opcode
	 * @return Returns true if this message represents a control message.
	 */
	public static final boolean isControl(int opcode) {
		if (opcode >= 0x8 && opcode <= 0xF) {
			return true;
		}
		
		return false;
	}

	/**
	 * Write all frames of this message to given channel
	 * if and only if message is finished.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public abstract void forward(OutputStream out) throws IOException;

	/**
	 * Write current frame of this message to given channel.
	 * 
	 * @param out
	 * @throws IOException
	 */
	public abstract void forwardCurrentFrame(OutputStream out) throws IOException;

	/**
	 * Returns true if all frames for this message
	 * were already read.
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * Read further frame for non-control message.
	 * 
	 * @param in
	 * @param frameHeader
	 * @throws IOException
	 */
	public abstract void readContinuation(InputStream in, byte frameHeader) throws IOException;

	/**
	 * Returns a readable representation of the numeric opcode.
	 * 
	 * @param opcode
	 * @return
	 */
	protected static String opcode2string(int opcode) {
		switch (opcode) {
		case OPCODE_BINARY:
			return "BINARY";
			
		case OPCODE_CLOSE:
			return "CLOSE";
			
		case OPCODE_CONTINUATION:
			return "CONTINUATION";
			
		case OPCODE_PING:
			return "PING";
			
		case OPCODE_PONG:
			return "PONG";
			
		case OPCODE_TEXT:
			return "TEXT";
			
		default:
			return "UNKNOWN";
		}
	}

	/**
	 * Helper method to decode payload into UTF-8 string.
	 * 
	 * @param payload
	 * @return
	 */
	protected String decodePayload(byte[] payload) {
		return decodePayload(payload, 0);
	}

	/**
	 * Helper method to decode payload into UTF-8 string.
	 * 
	 * @param payload
	 * @param offset
	 * @return
	 */
	protected String decodePayload(byte[] payload, int offset) {
		try {
			int length = payload.length - offset;
			
			Utf8StringBuilder builder = new Utf8StringBuilder(length);
			builder.append(payload, offset, length);
			return builder.toString();
			
			// could also use CharsetDecoder
//			Charset charset = Charset.forName("UTF-8");
//			CharsetDecoder decoder = charset.newDecoder();
//			decoder.decode();
		} catch (IllegalArgumentException e) {
			if (e.getMessage().equals("!utf8")) {
				logger.error("payload is not UTF-8");
			} else {
				throw e;
			}
		} catch (IllegalStateException e) {
			if (e.getMessage().equals("!utf8")) {
				logger.error("payload is not UTF-8");
			} else {
				throw e;
			}
		}
		
		return "<invalid UTF-8>";
	}
}
