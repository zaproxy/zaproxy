/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
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
package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.sql.Timestamp;

/**
 * Represents a single WebSocket message, consisting of at least one frame.
 * <p>
 * This class was created with sight on the WebSocketMessage class of the <a
 * href="http://code.google.com/p/monsoon/">Monsoon</a> project, although it is
 * not based on Java's NIO features.
 */
public abstract class WebSocketMessage {

	/**
	 * Orientation of WebSocket message.
	 */
	public enum Direction {
		INCOMING, OUTGOING
	}

	/**
	 * A message belongs to one connection.
	 */
	private WebSocketProxy proxy;
	
	/**
	 * Consecutive number identifying a {@link WebSocketMessage}. Unique within
	 * a {@link WebSocketProxy}.
	 */
	private int messageId;
	
	/**
	 * This buffer will contain the whole payload, unmasked
	 */
	protected ByteBuffer payload;

	/**
	 * Determined after first frame is processed.
	 */
	protected Direction direction;
	
	/**
	 * Indicates if this message already contains all of its frames.
	 */
	protected boolean isFinished;

	/**
	 * Indicating when this message was received.
	 */
	protected Timestamp timestamp;
	
	// WebSocket OpCodes - int's are used instead of a enum's for extensibility.

	/**
	 * Either continuation for {@link #OPCODE_TEXT} or {@link #OPCODE_BINARY}.
	 */
	public static final int OPCODE_CONTINUATION = 0x0;
	public static final int OPCODE_TEXT = 0x1;
	public static final int OPCODE_BINARY = 0x2;
	// non-control frames (0x3 - 0x7 are reserved for further non-control frames)

	public static final int OPCODE_CLOSE = 0x8;
	public static final int OPCODE_PING = 0x9;
	public static final int OPCODE_PONG = 0xA;
	// control frames (0xB - 0xF are reserved for further control frames)
	
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
	
	public static final int[] OPCODES = {OPCODE_TEXT, OPCODE_BINARY, OPCODE_CLOSE, OPCODE_PING, OPCODE_PONG };

	/**
	 * Indicates the opcode of this message.
	 * Initially it is set to -1, meaning that its type is unknown.
	 */
	protected int opcode = -1;

	/**
	 * Indicates the status code of this message if it is a close message.
	 * Initially it is set to -1, meaning that its close code is unknown.
	 */
	protected int closeCode = -1;

	/**
	 * One Data Transfer Object is created per {@link WebSocketMessage} instance.
	 * Might be also some subtype.
	 */
	private final WebSocketMessageDTO dto;
	
	public WebSocketMessage(WebSocketProxy proxy, int messageId) {
		this(proxy, messageId, new WebSocketMessageDTO());
	}

	protected WebSocketMessage(WebSocketProxy proxy, int messageId, WebSocketMessageDTO baseDto) {
		this.proxy = proxy;
		this.messageId = messageId;
		this.dto = baseDto;
	}

	/**
	 * @return consecutive number unique within one WebSocket channel
	 */
	public int getMessageId() {
		return messageId;
	}

	/**
	 * Write all frames of this message to given stream.
	 * 
	 * @param out
	 * @return True if successfully forwarded.
	 * @throws IOException
	 */
	public abstract boolean forward(OutputStream out) throws IOException;

	/**
	 * Read further frame for non-control message.
	 * 
	 * @param in
	 * @param frameHeader
	 * @throws IOException
	 */
	public abstract void readContinuation(InputStream in, byte frameHeader) throws IOException;

	/**
	 * Returns the status code if the message's opcode is a
	 * {@link WebSocketMessage#OPCODE_CLOSE}.
	 * One of the possible close codes is
	 * {@link WebSocketMessage#STATUS_CODE_OK}.
	 * 
	 * @return close code or -1
	 */
	public int getCloseCode() {
		return closeCode;
	}
	
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
	 * @return Returns true if given opcode stands for binary content.
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
	 * @return Returns true if given opcode stands for textual content.
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
	 * @return Returns true if given opcode represents a control message.
	 */
	public static final boolean isControl(int opcode) {
		if (opcode >= 0x8 && opcode <= 0xF) {
			return true;
		}
		
		return false;
	}

	/**
	 * Returns true if all frames for this message were already
	 * read (i.e.: if the frame with the FIN flag was read).
	 * 
	 * @return True if all frames were received.
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * @return readable representation of this messages opcode
	 */
	public String getOpcodeString() {
		return opcode2string(opcode);
	}

	/**
	 * @param opcode
	 * @return readable representation of the given numeric opcode
	 */
	public static String opcode2string(int opcode) {
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
	 * Use this helper for concatenating payloads of different WebSocket frames.
	 * Flips the {@link WebSocketMessage#payload} buffer as soon as this message
	 * is finished.
	 * 
	 * @param bytes
	 */
	protected void appendPayload(byte[] bytes) {		
		if (payload == null) {
			// initialize first
			payload = ByteBuffer.allocate(bytes.length);
			payload.put(bytes);
		} else {
			// increase buffer
			payload = reallocate(payload, payload.capacity() + bytes.length);
			payload.put(bytes);
		}
		
		if (isFinished) {
			payload.flip();
		}
	}
	
	/**
     * Resizes a given ByteBuffer to a new size.
     * 
     * @param src ByteBuffer to get resized
     * @param newSize size in bytes for the resized buffer
     */
    protected ByteBuffer reallocate(ByteBuffer src, int newSize) {
        int srcPos = src.position();
        if (srcPos > 0) {
            src.flip();
        }
        
        ByteBuffer dest = ByteBuffer.allocate(newSize);
        dest.put(src);
        dest.position(srcPos);
        
        return dest;
    }

	/**
	 * Returns date of receiving this message. Might also indicate the timestamp
	 * of the last frame received.
	 * 
	 * @return timestamp of message arrival
	 */
	public Timestamp getTimestamp() {
		return timestamp;
	}
	
	/**
	 * @return number of bytes used in the payload
	 */
	public abstract Integer getPayloadLength();
	
	/**
	 * Returns the 'original' payload as found in the WebSocket frame. Returned
	 * bytes array does not back the messages payload buffer (i.e. it is a
	 * copy).
	 * 
	 * @return bytes of payload
	 */
	public abstract byte[] getPayload();

	/**
	 * Modifies the payload to given byte array. Use
	 * {@link WebSocketMessage#setReadablePayload(String)} for setting payloads
	 * of non-binary messages.
	 * 
	 * @param newPayload
	 * @throws WebSocketException
	 */
	public abstract void setPayload(byte[] newPayload) throws WebSocketException;

	/**
	 * Returns the payload from {@link WebSocketMessage#getPayload()} as
	 * readable string (i.e.: converted to UTF-8).
	 * 
	 * @return readable representation of payload
	 */
	public abstract String getReadablePayload();

	/**
	 * Modifies the payload to given UTF-8 string. Converts that into bytes. 
	 * 
	 * @param newReadablePayload
	 * @throws WebSocketException 
	 */
	public abstract void setReadablePayload(String newReadablePayload) throws WebSocketException;
	
	/**
	 * Returns either {@link Direction#INCOMING} if this message originated from
	 * server side, or {@link Direction#OUTGOING} if it came from the browser.
	 * 
	 * @return message flow indicator
	 */
	public abstract Direction getDirection();

	/**
	 * Returns another presentation on this message object, used to decouple the
	 * user interface from this version specific implementation.
	 * 
	 * @return data transfer object
	 */
	public WebSocketMessageDTO getDTO() {
		// build upon base dto attribute set in constructor,
		// such that existing instances are updated with changed values.
		dto.channel = proxy.getDTO();
		
		Timestamp ts = getTimestamp();
		dto.setTime(ts);
		
		dto.opcode = getOpcode();
		dto.readableOpcode = getOpcodeString();

		if (isBinary()) {
			dto.payload = getPayload();
		} else {
			dto.payload = getReadablePayload();
			
			if (dto.payload == null) {
				// prevents NullPointerException
				dto.payload = "";
			}
		}
		
		dto.isOutgoing = (getDirection() == Direction.OUTGOING) ? true : false;
		
		dto.payloadLength = getPayloadLength();
		
		return dto;
	}
	
	@Override
	public String toString() {
		return "WebSocketMessage#" + getMessageId();
	}
}
