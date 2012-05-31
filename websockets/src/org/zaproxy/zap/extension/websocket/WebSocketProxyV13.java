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
package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

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
		 * Temporary buffer that will be added to
		 * {@link WebSocketMessage#frames} as soon as whole frame is read.
		 */
		private ByteBuffer currentFrame;

		/**
		 * Determines if last frame was masked.
		 */
		private boolean isMasked;

		/**
		 * Contains the mask from the last frame.
		 */
		private byte[] mask = new byte[4];

		/**
		 * Contains the number of bytes representing the payload.
		 */
		private int payloadLength;

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
		public void readFrame(InputStream in, byte frameHeader) throws IOException {
			// most significant bit of first byte is FIN flag
			isFinished = (frameHeader >> 7 & 0x1) == 1;
			
			// currentFrame buffer is filled by read() method directly
			currentFrame = ByteBuffer.allocate(4096);
			currentFrame.put(frameHeader);

			byte payloadByte = read(in);
			
			// most significant bit of second byte is MASK flag
			isMasked = (payloadByte >> 7 & 0x1) == 1;
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

			if (isMasked) {
				// read 4 bytes mask
				mask = read(in, 4);
			}

			byte[] payload = read(in, payloadLength);

			if (isMasked) {
				int currentMaskByteIndex = 0;
				for (int i = 0; i < payload.length; i++) {
					// unmask payload by XOR it continuously with frame mask
					payload[i] = (byte) (payload[i] ^ mask[currentMaskByteIndex]);
					currentMaskByteIndex = (currentMaskByteIndex + 1) % 4;
				}
			}
			
			if (isText(opcode)) {
				if (0 == payload.length) {
					logger.debug("got empty payload");
				} else if (payload.length < 10000) {
					logger.debug("got payload: '" + decodePayload(payload) + "'");
				} else {
					logger.debug("got huge payload, do not print it");
					// + decodePayload(payload, 0, 100) may result in non-finished string
				}
			} else if (isBinary(opcode)) {
				logger.info("got binary payload");				
			} else {
				if (opcode == OPCODE_CLOSE) {
					if (payload.length > 1) {
						int closeCode = ((payload[0] & 0xFF) << 8) | (payload[1] & 0xFF);
						logger.debug("close code is: " + closeCode);
					}
					
					if (payload.length > 2) {
						// process close message
						logger.debug("got control-payload: " + decodePayload(payload, 2));
					}
				}
			}
			
			// add currentFrame to frames list
			currentFrame.flip();
			frames.add(currentFrame);
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
			int freeSpace = currentFrame.capacity() - currentFrame.position();
			if (freeSpace < bytesRead) {
				currentFrame = reallocate(currentFrame, currentFrame.position() + bytesRead);
			}

			// add bytes to current frame
			currentFrame.put(buffer);
			
			return buffer;
		}
		
		/**
	     * Resizes a given ByteBuffer to a new size.
	     * 
	     * @param src ByteBuffer to get resized
	     * @param newSize size in bytes for the resized buffer
	     */
	    public ByteBuffer reallocate(ByteBuffer src, int newSize) {
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
	     * @see WebSocketMessage#forward(OutputStream)
	     */
		@Override
		public void forward(OutputStream out) throws IOException {
			if (!isFinished) {
				// do not forward unfinished messages
				return;
			}
			
			for (ByteBuffer frame : frames) {
				// forward frame by frame
				forwardFrame(frame, out);
			}
		}

		/**
		 * @see WebSocketMessage#forwardCurrentFrame(OutputStream)
		 */
		@Override
		public void forwardCurrentFrame(OutputStream out) throws IOException {
			logger.debug("forward current frame");
			
			forwardFrame(currentFrame, out);
		}
		
		/**
		 * Helper method to forward frames.
		 * 
		 * @param frame
		 * @param out
		 */
		private void forwardFrame(ByteBuffer frame, OutputStream out) throws IOException {
			byte[] buffer = new byte[frame.limit()];
			frame.get(buffer);
			out.write(buffer);
			out.flush();
		}

//		protected int writeFrame(int opcode, ByteBuffer buf, boolean blocking, boolean fin) throws IOException {
//			// TODO: handle the non-blocking case
//			// TODO: position buf past MAX_HEADER_SIZE and build the header
//			// before
//			// the payload to avoid gathering writes to the channel.
//			int n = buf.remaining();
//			buildHeader(opcode, n, fin);
//			if (isClient) {
//				byte[] mask = generateMask();
//				int maskPosition = 0;
//				// copy to avoid trashing the caller's buffer
//				ByteBuffer copy = ByteBuffer.allocate(buf.remaining());
//				copy.put(buf);
//				copy.flip();
//				buffers[1] = copy;
//				for (int k = 0; k < 2; k++) {
//					for (int i = buffers[k].position(); i < buffers[k].limit(); i++) {
//						buffers[k]
//								.put(i,
//										(byte) (buffers[k].get(i) ^ mask[maskPosition]));
//						maskPosition = (maskPosition + 1) % 4;
//					}
//				}
//				localChannel.write(ByteBuffer.wrap(mask));
//			} else {
//				buffers[1] = buf;
//			}
//			int totbytes = -buffers[0].remaining(); // subtract header length
//			totbytes += localChannel.write(buffers);
//			return totbytes;
//		}

//		/**
//		 * TODO: document me
//		 * 
//		 * @param opcode
//		 * @param length
//		 * @param fin
//		 */
//		private void buildHeader(int opcode, long length, boolean fin) {
//			headerBuf.clear();
//			byte firstByte = (byte) ((fin ? 0x80 : 0x00) | (opcode & 15));
//			headerBuf.put(firstByte);
//			if (length < 126) {
//				headerBuf.put((byte) (length & 127));
//			} else if (length < 65536) {
//				headerBuf.put((byte) 126);
//				headerBuf.putShort((short) length);
//			} else {
//				headerBuf.put((byte) 127);
//				headerBuf.putLong(length);
//			}
//			headerBuf.flip();
//		}
	}
}
