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
import java.util.Calendar;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.zaproxy.zap.extension.websocket.ui.WebSocketMessageDAO;

/**
 * This proxy implements the WebSocket protocol version 13 as specified in
 * RFC6455. Code was inspired by the Monsoon project
 * (http://code.google.com/p/monsoon/).
 */
public class WebSocketProxyV13 extends WebSocketProxy {

	private static final Logger logger = Logger.getLogger(WebSocketProxyV13.class);

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
		return new WebSocketMessageV13(in, frameHeader);
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
			private boolean isMasked;
			private byte[] mask;
			
			/**
			 * Prevent sending this frame several times.
			 */
			private boolean isForwarded;

			/**
			 * After a frame is sealed, it cannot be changed
			 * and data can be read by {@link WebSocketFrameV13#getBuffer()}.
			 */
			private boolean isSealed = false;

			public WebSocketFrameV13() {
				buffer = ByteBuffer.allocate(4096);
				isMasked = false;
				mask = new byte[4];
				isForwarded = false;
			}

			/**
			 * Builds up a frame according to given payload. Sets header and
			 * metadata (opcode, payload length, mask).
			 * 
			 * @param payload
			 */
			public WebSocketFrameV13(ByteBuffer payload, Direction direction) {
				// at maximum 16 bytes are added as header data
				buffer = ByteBuffer.allocate(payload.limit() + 16);
				isFinished = true;

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
				
				byte frameHeader = (byte) ((isFinished ? 0x80 : 0x00) | (opcode & 0x0F));
				buffer.put(frameHeader);

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
		}
		
		/**
		 * Consecutive number identifying a {@link WebSocketMessage} for one
		 * {@link WebSocketProxy}.
		 */
		private int messageId;
		
		/**
		 * This list will contain all the original bytes for each frame.
		 */
		private ArrayList<WebSocketFrameV13> receivedFrames = new ArrayList<WebSocketFrameV13>();

		private ArrayList<WebSocketFrameV13> modifiedFrames = new ArrayList<WebSocketFrameV13>();
		
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
		 * @param in
		 * @param frameHeader
		 * @throws IOException 
		 */
		public WebSocketMessageV13(InputStream in, byte frameHeader) throws IOException {			
			// 4 least significant bits are opcode
			opcode = (frameHeader & 0x0F);
			
			readFrame(in, frameHeader);
			
			messageId = getIncrementedMessageCount();
		}

		public int getMessageId() {
			return messageId;
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

			byte payloadByte = read(in);
			
			// most significant bit of second byte is MASK flag
			currentFrame.setMasked((payloadByte >> 7 & 0x1) == 1);
			payloadLength = (payloadByte & 0x7F);

			// multiple bytes for payload length are submitted in network byte order (MSB first)
			if (payloadLength < PAYLOAD_LENGTH_16) {
				// payload length is between 0-125 bytes and contained in payloadByte
			} else {
				int bytesToRetrieve = 0;

				// we have got PAYLOAD_LENGTH_16 or PAYLOAD_LENGTH_63
				if (payloadLength == PAYLOAD_LENGTH_16) {
					// payload length is between 126-65535 bytes represented by 2 bytes.
					bytesToRetrieve = 2;
				} else if (payloadLength == PAYLOAD_LENGTH_63) {
					// payload length is between 65536-2^63 bytes represented by 8 bytes
					// (most significant bit must be zero)
					bytesToRetrieve = 8;
				}

				byte[] extendedPayloadLength = read(in, bytesToRetrieve);

				payloadLength = 0;
				for (int i = 0; i < bytesToRetrieve; i++) {
					byte extendedPayload = extendedPayloadLength[i];
					
					// shift previous bits left and add next byte
					payloadLength = (payloadLength << 8) | (extendedPayload & 0xFF);
				}
			}
			
			logger.debug("length of current frame payload is: " + payloadLength);

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
				if (0 == payload.length) {
					logger.debug("got empty payload");
				} else if (payload.length < 10000) {
					logger.debug("got payload: '" + encodePayloadToUtf8(payload) + "'");
				} else {
					logger.debug("got huge payload, do not print it");
					// + decodePayload(payload, 0, 100) may result in non-finished string
				}
			} else if (isBinary(opcode)) {
				logger.info("got binary payload");				
			} else {
				if (opcode == OPCODE_CLOSE) {
					if (payload.length > 1) {
						closeCode = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
						logger.debug("close code is: " + closeCode);
						
						// remove close code from payload
						ArrayUtils.subarray(payload, 2, payload.length);
						
						// TODO: Close code handling. Users might want to change it
						// in break panel (also: show in WebSocketPanel, but where?)
					}
					
					if (payload.length > 0) {
						// process close message
						logger.debug("got control-payload: " + encodePayloadToUtf8(payload));
					}
				}
			}
			
			addPayload(payload);
			Calendar calendar = Calendar.getInstance();
			timestamp = new Timestamp(calendar.getTimeInMillis());
			
			// add currentFrame to frames list
			currentFrame.seal();
			receivedFrames.add(currentFrame);
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
		public void forward(OutputStream out) throws IOException {
			if (hasChanged) {
				// TODO: split into chunks according to PAYLOAD_LENGTH_63
				WebSocketFrameV13 frame = new WebSocketFrameV13(payload, getDirection());
				modifiedFrames.add(frame);
				forwardFrame(frame, out);
			} else {
				for (WebSocketFrameV13 frame : receivedFrames) {
					// forward frame by frame
					if (!frame.isForwarded()) {
						forwardFrame(frame, out);
					}
				}
			}
		}
		
		/**
		 * Helper method to forward frames.
		 * 
		 * @param frame
		 * @param out
		 */
		private void forwardFrame(WebSocketFrameV13 frame, OutputStream out) throws IOException {
			out.write(frame.getBuffer());
			out.flush();
			
			frame.setForwarded(true);
		}
		
		@Override
		public byte[] getPayload() {
			byte[] bytes = new byte[payload.limit()];
			payload.get(bytes);
			return bytes;
		}

		@Override
		public Integer getPayloadLength() {
			return payload.limit();
		}

		@Override
		public String getReadablePayload() {
			return encodePayloadToUtf8(payload.array());
		}

		@Override
		public void setReadablePayload(String newReadablePayload) throws WebSocketException {
			if (!isFinished()) {
				throw new WebSocketException("Only allowed to set payload of finished message");
			}
			
			ByteBuffer newPayload = ByteBuffer.wrap(decodePayloadFromUtf8(newReadablePayload));
			if (!payload.equals(newPayload)) {
				// mark this messages as changed in order to propagate changed
				// payload into frames or build up a big frame (see forward())
				hasChanged = true;
				payload = newPayload;
			}
		}

		@Override
		public Direction getDirection() {
			return receivedFrames.get(0).isMasked() ? Direction.OUTGOING : Direction.INCOMING;
		}
		
		@Override
		public WebSocketMessageDAO getDAO() {
			WebSocketMessageDAO dao = super.getDAO();
			
			dao.channelId = getChannelId();
			dao.id = getMessageId();
			
			return dao;
		}
	}
}
