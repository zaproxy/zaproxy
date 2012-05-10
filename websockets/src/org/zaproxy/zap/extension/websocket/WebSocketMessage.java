package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Represents a single WebSocket message, consisting out of at least one frame.
 * This class was created with sight on the WebSocketMessage class of the
 * Monsoon project.
 */
public abstract class WebSocketMessage {

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
	 * @param opcode
	 * @return Returns true if this message contains binary content.
	 */
	public static final boolean isBinary(int opcode) {
		return opcode == OPCODE_BINARY;
	}

	/**
	 * @param opcode
	 * @return Returns true if this message contains textual content.
	 */
	public static final boolean isText(int opcode) {
		return opcode == OPCODE_TEXT;
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
	 * Write all frames of current message to given channel.
	 * 
	 * @param channel
	 * @throws IOException
	 */
	public abstract void forward(SocketChannel channel) throws IOException;

	/**
	 * Returns true if all frames for this message
	 * were already read.
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return isFinished;
	}

	public abstract void readContinuation(byte flagsByte, SocketChannel channel) throws IOException;
}
