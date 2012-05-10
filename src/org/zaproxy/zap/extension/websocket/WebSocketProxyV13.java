package org.zaproxy.zap.extension.websocket;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

/**
 * This proxy implements the WebSocket protocol version 13 as specified in RFC6455.
 * A lot of code is reused from the Monsoon project at http://code.google.com/p/monsoon/.
 */
public class WebSocketProxyV13 extends WebSocketProxy {

	private static Logger logger = Logger.getLogger(WebSocketProxyV13.class);

	/**
	 * @see WebSocketProxy
	 */
	public WebSocketProxyV13(SocketChannel localChannel,
			SocketChannel remoteChannel) throws WebSocketException {
		super(localChannel, remoteChannel);
	}

	/**
	 * Version 13 specific WebSockets message.
	 */
	protected class WebSocketMessageV13 extends WebSocketMessage {
		private ByteBuffer currentFrame;

		private boolean isMasked;

		private byte[] mask = new byte[4];
		private int currentMaskByteIndex = 0;

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
		 * interpreted as an unsigned integer is the payload length. (the most
		 * significant bit MUST be 0)
		 */
		private static final int PAYLOAD_LENGTH_63 = 127;

		/**
		 * Creates a message with the first byte already read.
		 * 
		 * @param flagsByte
		 * @throws IOException 
		 */
		public WebSocketMessageV13(byte flagsByte, SocketChannel channel) throws IOException {			
			// 4 least significant bits are OpCode
			opcode = (flagsByte & 0x0F);
			
			readFrame(flagsByte, channel);
		}

		/**
		 * Add next frame to this message. First byte already
		 * given by parameter <em>flagsByte</em>.
		 * 
		 * @param flagsByte
		 * @param channel
		 * @throws IOException
		 */
		public void readContinuation(byte flagsByte, SocketChannel channel) throws IOException {			
			readFrame(flagsByte, channel);
		}

		/**
		 * Given the first byte of a frame and a channel,
		 * this method reads from the second byte until the end of the frame.
		 * 
		 * @param flagsByte
		 * @param channel
		 * @throws IOException
		 */
		public void readFrame(byte flagsByte, SocketChannel channel) throws IOException {
			// most significant bit is FIN flag & 0x1
			isFinished = (flagsByte >> 7 & 0x1) == 1;
			
			// currentFrame buffer is filled by read()
			currentFrame = ByteBuffer.allocate(4096);
			// TODO: what if bigger, reallocate
			// TODO: what if less at the end? free
			currentFrame.put(flagsByte);
			frames.add(currentFrame);
			
			ByteBuffer baseFrameHeader = read(channel, 1);

			byte payloadByte = baseFrameHeader.get();
			isMasked = (payloadByte >> 7 & 0x1) == 1; // most significant bit is MASK flag
			payloadLength = (payloadByte & 0x7F);
			
			baseFrameHeader.clear();

			/*
			 * Multiple bytes for payload length are submitted in network byte
			 * order: most significant byte first
			 */
			if (payloadLength < PAYLOAD_LENGTH_16) {
				// payload length is between 0-125 bytes
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

				ByteBuffer extendedPayloadLength = read(channel, bytesToRetrieve);

				payloadLength = 0;
				for (int i = 0; i < bytesToRetrieve; i++) {
					byte extendedPayload = extendedPayloadLength.get();
					
					// shift previous bits left and add next byte
					payloadLength = (payloadLength << 8) | (extendedPayload & 0xFF);
				}
				extendedPayloadLength.clear();
			}

			// now we know the payload length
			// we are able to determine frame length
			logger.debug("Length of current frame payload is: " + payloadLength);

			int remainingBytes = payloadLength;
			if (isMasked) {
				remainingBytes += 4;
			}

			ByteBuffer remainingByteBuffer = read(channel, remainingBytes);

			byte[] payload = new byte[payloadLength];
			if (isMasked) {
				// read 4 bytes mask
				remainingByteBuffer.get(mask);
			}

			// the rest goes into payload
			try {
				remainingByteBuffer.get(payload);
			} catch (BufferUnderflowException e) {
				logger.error("remainingByteBuffer has got size of "
						+ remainingByteBuffer.capacity() + " with limit of "
						+ remainingByteBuffer.limit());
				// TODO: read byte by byte??
			}

			if (isMasked) {
				for (int i = 0; i < payload.length; i++) {
					// unmask payload by XOR it continuously with frame mask
					payload[i] = (byte) (payload[i] ^ mask[currentMaskByteIndex]);
					currentMaskByteIndex = (currentMaskByteIndex + 1) % 4;
				}
			}
			
			if (isText(opcode)) {
				Utf8StringBuilder utf8 = new Utf8StringBuilder();
				utf8.append(payload, 0, payload.length);
				logger.info("got payload: " + utf8.toString());
			} else if (isBinary(opcode)) {
				logger.info("got binary payload");				
			} else {
				// ignore control messages
			}
			
			currentFrame.flip();
		}

		/**
		 * Reads given length from the channel. For long messages it performs
		 * several iterations and ensures, that everything is present.
		 * 
		 * @param channel
		 * @param length Determines how much bytes should be read from the given channel.
		 * @return ByteBuffer read for reading (i.e.: already flipped).
		 * @throws IOException
		 */
		private ByteBuffer read(SocketChannel channel, int length) throws IOException {
			ByteBuffer buffer = ByteBuffer.allocate(length);
			
			int bytesRead = 0;
			do {
				bytesRead += channel.read(buffer);
			} while (length != bytesRead);
			
			buffer.flip();
			
			int freeSpace = currentFrame.capacity() - currentFrame.position();
			if (freeSpace < bytesRead) {
				currentFrame = reallocate(currentFrame, currentFrame.position() + bytesRead);
			}

			// add bytes to current frame and reset to be able to read again
			currentFrame.put(buffer);
			buffer.rewind();
			
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

		@Override
		public void forward(SocketChannel channel) throws IOException {
			for (ByteBuffer frame : frames) {
				channel.write(frame);
			}
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

	@Override
	protected WebSocketMessage createWebSocketMessage(byte flagsByte,
			SocketChannel channel) throws IOException {
		return new WebSocketMessageV13(flagsByte, channel);
	}
}
