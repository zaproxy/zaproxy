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
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.websocket.utility.InvalidUtf8Exception;
import org.zaproxy.zap.extension.websocket.utility.Utf8Util;

/**
 * This proxy implements the WebSocket protocol version 13 as specified in <a
 * href="http://tools.ietf.org/html/rfc6455">RFC6455</a>. Code was inspired by
 * the <a href="http://code.google.com/p/monsoon/">Monsoon</a> project.
 */
public class WebSocketProxyV13 extends WebSocketProxy {

	private static final Logger logger = Logger.getLogger(WebSocketProxyV13.class);
	
	/**
	 * The payload length is determined by 63 bits -> at maximum (2^63 - 1),
	 * which can be represented in Java by
	 * <code>new BigInteger("9223372036854775806")</code>. But for this
	 * implementation I choose a smaller maximum frame length.
	 */
	private static final int PAYLOAD_MAX_FRAME_LENGTH = Integer.MAX_VALUE;

	/**
	 * @see WebSocketProxy#WebSocketProxy(Socket, Socket)
	 */
	public WebSocketProxyV13(Socket localSocket, Socket remoteSocket) throws WebSocketException {
		super(localSocket, remoteSocket);
	}

	/**
	 * @see WebSocketProxy#createWebSocketMessage(InputStream, byte)
	 */
	@Override
	protected WebSocketMessage createWebSocketMessage(InputStream in, byte frameHeader) throws IOException {
		return new WebSocketMessageV13(this, in, frameHeader);
	}

	@Override
	protected WebSocketMessage createWebSocketMessage(WebSocketMessageDTO message) throws WebSocketException {
		return new WebSocketMessageV13(this, message);
	}

	/**
	 * Version 13 specific WebSockets message.
	 */
	protected class WebSocketMessageV13 extends WebSocketMessage {
		
		/**
		 * One message can consist of several frames.
		 */
		private class WebSocketFrameV13 {
		    private final Random randomizer = new Random();
			private ByteBuffer buffer;
			private byte[] mask;
			private boolean isMasked;
			
			/**
			 * Prevent sending this frame several times.
			 */
			private boolean isForwarded;

			/**
			 * After a frame is sealed, it cannot be changed
			 * and data can be read by {@link WebSocketFrameV13#getBuffer()}.
			 */
			private boolean isSealed = false;
			
			/**
			 * Contains value of RSV1, RSV2 & RSV3.
			 */
			private int rsv;
			
			public WebSocketFrameV13() {
				buffer = ByteBuffer.allocate(4096);
				isMasked = false;
				mask = new byte[4];
				isForwarded = false;
				rsv = 0;
			}

			/**
			 * Builds up a frame according to given payload. Sets header and
			 * metadata (opcode, payload length, mask).
			 * 
			 * @param payload
			 */
			public WebSocketFrameV13(ByteBuffer payload, Direction direction, boolean isFinished, int frameOpcode, int rsv) {
				// at maximum 16 bytes are added as header data
				buffer = ByteBuffer.allocate(payload.limit() + 16);
				this.rsv = rsv;

				int payloadLength = payload.limit();
				if (direction.equals(Direction.OUTGOING)) {
					isMasked = true;

					mask = new byte[4];
					randomizer.nextBytes(mask);
					
					// mask payload, by applying mask to each byte
					int maskPosition = 0;
					for (int i = 0; i < payloadLength; i++) {
						payload.put(i, (byte) (payload.get(i) ^ mask[maskPosition]));
						maskPosition = (maskPosition + 1) % 4;
					}
				} else {
					isMasked = false;
				}
				
				isForwarded = false;

				byte finishedBits = (byte) (isFinished ? 0x80 : 0x00);
				byte rsvBits = (byte) ((this.rsv & 0x07) << 4);
				byte opcodeBits = (byte) (frameOpcode & 0x0F);
				byte frameHeader = (byte) (finishedBits | rsvBits | opcodeBits);
				buffer.put(frameHeader);
				logger.debug("Frame header of newly created WebSocketFrame: " + getByteAsBitString(frameHeader));

				if (payloadLength < PAYLOAD_LENGTH_16) {
					buffer.put((byte) ((isMasked ? 0x80 : 0x00) | (payloadLength & 0xDF)));
				} else if (payloadLength < 65536) {
					buffer.put((byte) ((isMasked ? 0x80 : 0x00) | PAYLOAD_LENGTH_16));
					buffer.putShort((short) payloadLength);
				} else {
					buffer.put((byte) ((isMasked ? 0x80 : 0x00) | PAYLOAD_LENGTH_63));
					buffer.putLong(payloadLength);
				}
				
				if (isMasked) {
					buffer.put(mask);
				}
				
				buffer.put(payload.array());
				
				seal();
			}

			public void put(byte b) throws WebSocketException {
				if (isSealed) {
					throw new WebSocketException("You cannot change a 'sealed' frame.");
				}
				buffer.put(b);
			}

			public void put(byte[] b) throws WebSocketException {
				if (isSealed) {
					throw new WebSocketException("You cannot change a 'sealed' frame.");
				}
				buffer.put(b);
			}

			public void setMasked(boolean isMasked) {
				this.isMasked = isMasked;
			}
			
			public boolean isMasked() {
				return isMasked;
			}

			public void setMask(byte[] read) {
				mask = read;
			}

			public byte getMaskAt(int index) {
				return mask[index];
			}

			public int getFreeSpace() {
				if (isSealed) {
					return 0;
				}
				return buffer.capacity() - buffer.position();
			}

			public void reallocateFor(int bytesRead) throws WebSocketException {
				if (isSealed) {
					throw new WebSocketException("You cannot change size of 'sealed' frame's buffer.");
				}
				buffer = reallocate(buffer, buffer.position() + bytesRead);
			}
			
			public boolean isForwarded() {
				return isForwarded;
			}

			public void seal() {
				if (!isSealed) {
					buffer.flip();
					isSealed  = true;
				}
			}

			public byte[] getBuffer() throws WebSocketException {
				if (!isSealed) {
					throw new WebSocketException("You should call seal() on WebSocketFrame first, before getBuffer().");
				}
				byte[] result = new byte[buffer.limit()];
				buffer.get(result);
				return result;
			}

			public void setForwarded(boolean isForwarded) {
				this.isForwarded = isForwarded;
			}

			/**
			 * Valid values are in the interval [1,6].
			 * 
			 * @param rsv
			 */
			public void setRsv(int rsv) {
				this.rsv  = rsv;
			}

//			public int getRsv() {
//				return rsv;
//			}
		}
		
		private List<WebSocketFrameV13> receivedFrames = new ArrayList<>();
		
		/**
		 * Temporary buffer that will be added to
		 * {@link WebSocketMessage#receivedFrames} as soon as whole frame is read.
		 */
		private WebSocketFrameV13 currentFrame;

		/**
		 * Contains the number of bytes representing the payload.
		 */
		private int payloadLength;

		/**
		 * Marks this object as changed, indicating that frame headers have to
		 * be built manually on forwarding.
		 */
		private boolean hasChanged;

		/**
		 * Is set by {@link WebSocketMessageV13#getReadablePayload()}.
		 */
		private boolean isValidUtf8Payload;

		/**
		 * By default, there are 7 bits to indicate the payload length. If the
		 * length can not be shown with 7 bits, the payload length is set to
		 * 126. Then the next 16 bits interpreted as unsigned integer is the
		 * payload length.
		 */
		private static final int PAYLOAD_LENGTH_16 = 126;

		/**
		 * If the 7 bits represent the value 127, then the next 64 bits
		 * interpreted as an unsigned integer is the payload length (the most
		 * significant bit MUST be 0).
		 */
		private static final int PAYLOAD_LENGTH_63 = 127;

		/**
		 * Creates a message with the first byte already read.
		 * 
		 * @param proxy
		 * @param in
		 * @param frameHeader
		 * @throws IOException 
		 */
		public WebSocketMessageV13(WebSocketProxy proxy, InputStream in, byte frameHeader) throws IOException {
			super(proxy, getIncrementedMessageCount());
			
			// 4 least significant bits are opcode
			opcode = (frameHeader & 0x0F);
			
			// timestamp represents first arrival of message
			Calendar calendar = Calendar.getInstance();
			timestamp = new Timestamp(calendar.getTimeInMillis());
			
			readFrame(in, frameHeader);
			direction = receivedFrames.get(0).isMasked() ? Direction.OUTGOING : Direction.INCOMING;
		}

		/**
		 * Use this constructor to create a custom message from given data.
		 * 
		 * @param proxy
		 * @param message
		 * @throws WebSocketException 
		 */
		public WebSocketMessageV13(WebSocketProxy proxy, WebSocketMessageDTO message) throws WebSocketException {
			super(proxy, getIncrementedMessageCount(), message);
			message.id = getMessageId();
			
			Calendar calendar = Calendar.getInstance();
			timestamp = new Timestamp(calendar.getTimeInMillis());
			message.setTime(timestamp);
			
			isFinished = true;
			opcode = message.opcode;
			closeCode = (message.closeCode == null) ? -1 : message.closeCode;
			direction = message.isOutgoing ? Direction.OUTGOING : Direction.INCOMING;
			
			payload = ByteBuffer.allocate(0);
			if (message.payload instanceof byte[]) {
				setPayload((byte[])message.payload);
			} else {
				setReadablePayload((String)message.payload);
			}
		}

		/**
		 * Add next frame to this message. First byte already
		 * given by parameter <em>frameHeader</em>.
		 * 
		 * @param in
		 * @param frameHeader
		 * @throws IOException
		 */
		@Override
		public void readContinuation(InputStream in, byte frameHeader) throws IOException {			
			readFrame(in, frameHeader);
		}
		
		/**
		 * Can be used to print or log bytes.
		 * 
		 * @param word
		 * @return
		 */
		private String getByteAsBitString(byte word) {
	      StringBuilder buf = new StringBuilder();
	      for (int i = 0; i < 8; i++) {
	         buf.append((word >> (8 - (i+1)) & 0x0001));
	      }
	      return buf.toString();
	   }

		/**
		 * Given an {@link InputStream} and the first byte of a frame,
		 * this method reads the second byte until the end of the frame.
		 * 
		 * @param in
		 * @param frameHeader
		 * @throws IOException
		 */
		private void readFrame(InputStream in, byte frameHeader) throws IOException {
			// most significant bit of first byte is FIN flag
			isFinished = (frameHeader >> 7 & 0x1) == 1;
			
			// currentFrame buffer is filled by read() method directly
			currentFrame = new WebSocketFrameV13();
			currentFrame.put(frameHeader);
			
			currentFrame.setRsv((frameHeader >> 4 & 0x7));

			byte payloadByte = read(in);
			
			// most significant bit of second byte is MASK flag
			currentFrame.setMasked((payloadByte >> 7 & 0x1) == 1);
			
			payloadLength = determinePayloadLength(in, payloadByte);
			logger.debug("length of current frame payload is: " + payloadLength + "; first two bytes: " + getByteAsBitString(frameHeader) + " " + getByteAsBitString(payloadByte));

			if (currentFrame.isMasked()) {
				// read 4 bytes mask
				currentFrame.setMask(read(in, 4));
			}

			byte[] payload = read(in, payloadLength);

			if (currentFrame.isMasked()) {
				int currentMaskByteIndex = 0;
				for (int i = 0; i < payload.length; i++) {
					// unmask payload by XOR it continuously with frame mask
					payload[i] = (byte) (payload[i] ^ currentFrame.getMaskAt(currentMaskByteIndex));
					currentMaskByteIndex = (currentMaskByteIndex + 1) % 4;
				}
			}
			
			if (isText(opcode)) {
				logger.info("got text frame payload");
			} else if (isBinary(opcode)) {
				logger.info("got binary frame payload");				
			} else {
				if (opcode == OPCODE_CLOSE) {
					if (payload.length > 1) {
						// if there is a body, the first two bytes are a
						// 2-byte unsigned integer (in network byte order)
						closeCode = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
						logger.debug("close code is: " + closeCode);
						
						payload = getReadableCloseFramePayload(payload, closeCode);
					}
					
					if (payload.length > 0) {
						// process close message
						try {
							logger.debug("got control-payload: " + Utf8Util.encodePayloadToUtf8(payload));
						} catch (InvalidUtf8Exception e) {
							// safely ignore utf8 error here
						}
					}
				}
			}
			
			appendPayload(payload);
			
			// add currentFrame to frames list
			currentFrame.seal();
			receivedFrames.add(currentFrame);
		}

		/**
		 * Looks at the payload byte from the WebSockets header and determines
		 * the packets length. It reads bytes from the extended length field if
		 * required.
		 * 
		 * @param in
		 * @param payloadByte
		 * @return
		 * @throws IOException
		 */
		private int determinePayloadLength(InputStream in, byte payloadByte) throws IOException {
			int length = (payloadByte & 0x7F);

			// multiple bytes for payload length are submitted in network byte order (MSB first)
			if (length < PAYLOAD_LENGTH_16) {
				// payload length is between 0-125 bytes and contained in payloadByte
			} else {
				int bytesToRetrieve = 0;

				// we have got PAYLOAD_LENGTH_16 or PAYLOAD_LENGTH_63
				if (length == PAYLOAD_LENGTH_16) {
					// payload length is between 126-65535 bytes represented by 2 bytes.
					bytesToRetrieve = 2;
				} else if (length == PAYLOAD_LENGTH_63) {
					// payload length is between 65536-2^63 bytes represented by 8 bytes
					// (most significant bit must be zero)
					bytesToRetrieve = 8;
				}

				byte[] extendedPayloadLength = read(in, bytesToRetrieve);

				length = 0;
				for (int i = 0; i < bytesToRetrieve; i++) {
					byte extendedPayload = extendedPayloadLength[i];
					
					// shift previous bits left and add next byte
					length = (length << 8) | (extendedPayload & 0xFF);
				}
			}
			
			return length;
		}

		/**
		 * Takes the payload of a close frame and transforms the 2 bytes
		 * representation of the status code to a 4 digit string representation
		 * readable by humans.
		 * 
		 * @param payload
		 * @param statusCode
		 * @return
		 */
		private byte[] getReadableCloseFramePayload(byte[] payload, int statusCode) {
			byte[] closeCode = Integer.toString(statusCode).getBytes();
			
			// close code might consist of illegal 5 digits (as one of the Autobahn tests)
			byte[] newPayload = new byte[payload.length + (closeCode.length - 2)];
			
			try {
				System.arraycopy(closeCode, 0, newPayload, 0, closeCode.length);
			} catch (IndexOutOfBoundsException e) {
				logger.error(e);
			}
			
			try {
				System.arraycopy(payload, 2, newPayload, closeCode.length, payload.length - 2);
			} catch (IndexOutOfBoundsException e) {
				logger.error(e);
			}
			
			return newPayload;
		}

		/**
		 * Takes the modified payload of a close frame and transforms the
		 * readable status code (4 digit string) back to 2 byte representation.
		 * 
		 * @param payload
		 * @return
		 * @throws WebSocketException
		 * @throws NumberFormatException
		 */
		private ByteBuffer getTransmittableCloseFramePayload(ByteBuffer payload) throws NumberFormatException, WebSocketException {
			String closeCodePayload;
			try {
				closeCodePayload = Utf8Util.encodePayloadToUtf8(payload.array(), 0, 4);
			} catch (InvalidUtf8Exception e) {
				throw new WebSocketException(e.getMessage(), e);
			}
			
			int newCloseCode = Integer.parseInt(closeCodePayload);
			
			byte[] newCloseCodeByte = new byte[2];
			newCloseCodeByte[0] = (byte) ((newCloseCode >> 8) & 0xFF);
			newCloseCodeByte[1] = (byte) ((newCloseCode) & 0xFF);
			
			ByteBuffer newPayload = ByteBuffer.allocate(payload.limit() - 2);
			if (payload.limit() > 4) {
				newPayload.put(ArrayUtils.subarray(payload.array(), 4, payload.limit()), 2, payload.limit() - 4);
			}
			newPayload.put(newCloseCodeByte, 0, 2);
			
			return newPayload;
		}

		/**
		 * Read only one byte from input stream.
		 * 
		 * @param in
		 * @return
		 * @throws IOException
		 */
		private byte read(InputStream in) throws IOException {
			byte[] buffer = read(in, 1);
			return buffer[0];
		}

		/**
		 * Reads given length from the given stream.
		 * 
		 * @param in {@link InputStream} to read from.
		 * @param length Determines how much bytes should be read from the given stream.
		 * @return Bytes read from stream - blocks until given length is read!
		 * @throws IOException
		 */
		private byte[] read(InputStream in, int length) throws IOException {
			byte[] buffer = new byte[length];
			
			// read until buffer is full 
			int bytesRead = 0;
			do {
				bytesRead += in.read(buffer, bytesRead, length - bytesRead);
			} while (length != bytesRead);
			
			// maybe we have to increase the size of the current frame buffer
			if (currentFrame.getFreeSpace() < bytesRead) {
				currentFrame.reallocateFor(bytesRead);
			}

			// add bytes to current frame
			currentFrame.put(buffer);
			
			return buffer;
		}

	    /**
	     * @see WebSocketMessage#forward(OutputStream)
	     */
		@Override
		public boolean forward(OutputStream out) throws IOException {
			if (out == null) {
				return false;
			}
			
			logger.debug("forward message#" + getMessageId());
			
			if (hasChanged) {
				if (opcode == OPCODE_CLOSE) {
					payload = getTransmittableCloseFramePayload(payload);
				}
				
				// split into chunks according to maximum frame length
				int writtenBytes = 0;
				int frameLength = Math.min(PAYLOAD_MAX_FRAME_LENGTH, payload.limit());
				int frameOpcode = opcode;
				boolean isLastFrame;
				
				do {
					ByteBuffer tempBuffer = ByteBuffer.allocate(frameLength);
					tempBuffer.hasArray();
					payload.get(tempBuffer.array(), 0, frameLength);
					
					writtenBytes = frameLength + writtenBytes;
					frameLength = Math.min(PAYLOAD_MAX_FRAME_LENGTH, payload.limit() - writtenBytes);
					
					isLastFrame = (frameLength <= 0); 
				
					// TODO: use RSV from first original frame?
					WebSocketFrameV13 frame = new WebSocketFrameV13(tempBuffer, getDirection(), isLastFrame, frameOpcode, 0);
					logger.debug("forward modified frame");
					forwardFrame(frame, out);
					// next frame is a continuation of the current one
					frameOpcode = OPCODE_CONTINUATION;
					
					// TODO: What if e.g.: a close frame has got a huge payload
					// that exceeds INTEGER.MAX_VALUE, then I send a OPCODE_CLOSE
					// followed by another OPCODE_CONTINUATION, but that is not
					// allowed by RFC6455 (control frames aren't allowed to
					// exceed one frame)
				} while (!isLastFrame);
			} else {
				for (WebSocketFrameV13 frame : receivedFrames) {
					// forward frame by frame
					if (!frame.isForwarded()) {
						logger.debug("forward frame");
						forwardFrame(frame, out);
					}
				}
			}
			
			return true;
		}

		/**
		 * Helper method to forward frames.
		 * 
		 * @param frame
		 * @param out
		 */
		private void forwardFrame(WebSocketFrameV13 frame, OutputStream out) throws IOException {
			synchronized (out) {
				out.write(frame.getBuffer());
				out.flush();
			}
			
			frame.setForwarded(true);
		}
		
		@Override
		public byte[] getPayload() {
			if (!isFinished) {
				return new byte[0];
			}
			payload.rewind();
			byte[] bytes = new byte[payload.limit()];
			payload.get(bytes);
			return bytes;
		}

		@Override
		public void setPayload(byte[] newPayload) throws WebSocketException {
			if (!isFinished()) {
				throw new WebSocketException("Only allowed to set payload of finished message!");
			}
			
			if (!Arrays.equals(newPayload, getPayload())) {
				hasChanged = true;
				payload = ByteBuffer.wrap(newPayload);
			}
		}

		@Override
		public Integer getPayloadLength() {
			int length = payload.limit();
			
			if (opcode == OPCODE_CLOSE) {
				// if there is a body, the first two bytes are a
				// 2-byte unsigned integer (in network byte order)
				length = Math.max(0, length - 2);
			}
			
			return length;
		}

		@Override
		public String getReadablePayload() {
			try {
				isValidUtf8Payload = true;
				return Utf8Util.encodePayloadToUtf8(payload.array(), 0, payload.limit());
			} catch (InvalidUtf8Exception e) {
				isValidUtf8Payload  = false;
				return "<invalid UTF-8>";
			}
		}

		@Override
		public void setReadablePayload(String newReadablePayload) throws WebSocketException {
			if (!isFinished()) {
				throw new WebSocketException("Only allowed to set payload of finished message!");
			}
			
			String readablePayload = getReadablePayload();
			byte[] newBytesPayload = Utf8Util.decodePayloadFromUtf8(newReadablePayload);
			// compare readable strings (working on byte arrays did not work)
			if (isValidUtf8Payload && !Arrays.equals(newBytesPayload, Utf8Util.decodePayloadFromUtf8(readablePayload))) {
				// mark this message as changed in order to propagate changed
				// payload into frames or build up a big frame (see forward())
				hasChanged = true;
				payload = ByteBuffer.wrap(newBytesPayload);
			}
		}

		@Override
		public Direction getDirection() {
			return direction;
		}
		
		@Override
		public WebSocketMessageDTO getDTO() {
			WebSocketMessageDTO message = super.getDTO();
			
			message.channel.id = getChannelId();
			message.id = getMessageId();
			
			return message;
		}
	}
}
