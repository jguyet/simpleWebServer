package org.web.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class WebSocketCryptage {
	
	/**
	 * INUTILE SAUF TEXT POUR LE MOMENT (MESSAGES TYPE)
	 */
	private static final byte OPCODE_CONT = 0x0;
	private static final byte OPCODE_BINARY = 0x2;
	private static final byte OPCODE_CLOSE = 0x8;
	private static final byte OPCODE_PING = 0x9;
	private static final byte OPCODE_PONG = 0xA;
	private static final byte OPCODE_TEXT = 0x1;
	
	private int					fragmentedFramesCount;
	private boolean				frameFinalFlag;
	private int					frameRsv;
	private int					frameOpcode;
	private long				framePayloadLength;
	private byte[]				maskingKey = new byte[4];
	private final boolean		allowExtensions;
	private final boolean		maskedPayload;
	
	//Index Frame
	private int					payloadStartIndex = -1;

	//Buffer
	private byte[]				buffer = new byte[66000];
	private int					writeIndex = 0;
	private int					readIndex;
	
	//Total webscoket frame (metadata + payload)
	private long				totalPacketLength = -1;

	public WebSocketCryptage(boolean maskedPayload, boolean allowExtensions)
	{
		this.maskedPayload = maskedPayload;
		this.allowExtensions = allowExtensions;
	}
	
	private byte readNextByte()
	{
		if(readIndex >= writeIndex)
			return (0);
		return (this.buffer[readIndex++]);
	}
	
	/**
	 * <b>public byte[] decode(InputStream is)</b><br>
	 * decode socketMessage
	 * @param is
	 * @return
	 */
	public byte[] decode(InputStream is)
	{
		try
		{
			int bytesRead = is.read(buffer, writeIndex, buffer.length - writeIndex);
			
			if(bytesRead < 0)
				bytesRead = 0;
			
			//Update the count in the buffer
			writeIndex += bytesRead;
			//Start over from scratch.
			readIndex = 0;
			//length 4 is a length of the mask
			if(writeIndex < 4)
				return null;
	
			byte b = readNextByte();
			frameFinalFlag = (b & 0x80) != 0;
			frameRsv = (b & 0x70) >> 4;
			frameOpcode = b & 0x0F;
	
			//mask, payload length 1
			b = readNextByte();
			boolean frameMasked = (b & 0x80) != 0;
			int framePayloadLen1 = b & 0x7F;
	
			if (frameRsv != 0 && !allowExtensions)
				return null;
			if (maskedPayload && !frameMasked)
				return null;
			try
			{
				//Read frame payload length
				if (framePayloadLen1 == 126)
				{
					int byte1 = 0xff & readNextByte();
					int byte2 = 0xff & readNextByte();
					int value = (byte1 << 8) | byte2;
					framePayloadLength = value;
				}
				else if (framePayloadLen1 == 127)
				{
					long value = 0;
					for(int q = 0 ;q < 8; q++)
					{
						value &= (0xff & readNextByte()) << (7 - q);
					}
					framePayloadLength = value;
	
					if (framePayloadLength < 65536)
						return null;
				}
				else
					framePayloadLength = framePayloadLen1;
				
				if(framePayloadLength < 0)
					return null;
	
				//save mask
				if (maskedPayload)
				{
					for(int q = 0; q < 4; q++)
						maskingKey[q] = readNextByte();
				}
			}
			catch (IllegalStateException e) {
				return null;
			}
			//remember the payload position
			payloadStartIndex = readIndex;
			totalPacketLength = readIndex + framePayloadLength;
	
			//check if we have enough data at all
			if(writeIndex < totalPacketLength)//wait for more data
				return null;
	
			//unmask data if needed and only if the condition above is true
			if (maskedPayload)
				unmask(buffer, payloadStartIndex, (int)(payloadStartIndex + framePayloadLength));
	
			//finally isolate the unmasked payload, the bytes are plaintext here
			byte[] plainTextBytes = new byte[(int)framePayloadLength];
			System.arraycopy(buffer, payloadStartIndex, plainTextBytes, 0, (int) framePayloadLength);
			
			//now move the pending data to the begining of the buffer so we can continue having good stream
			for(int q = 1; q < writeIndex - totalPacketLength; q++)
			{
				buffer[q] = buffer[(int)totalPacketLength + q];
			}
			writeIndex -= totalPacketLength;
			
			//all done, we are ready to be called again
			return plainTextBytes;
		} catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * <b>private void unmask(byte[] frame, int startIndex, int endIndex)</b><br>
	 * Unmask XOR KEY<br>
	 * @param frame
	 * @param startIndex
	 * @param endIndex
	 */
	private void unmask(byte[] frame, int startIndex, int endIndex)
	{
		for (int i = 0; i < endIndex-startIndex; i++)
		{
			frame[startIndex+i] = (byte) (frame[startIndex+i] ^ maskingKey[i % 4]);
		}
	}
	
	/**
	 * <b>public static byte[] encode(byte[] msg, int rsv, boolean fin)</b><br>
	 * Encode message
	 * @param msg
	 * @param rsv
	 * @param fin
	 * @return byte[]
	 */
	public byte[] encode(byte[] msg, int rsv, boolean fin)
	{

		try {
			boolean maskPayload = false;
			ByteArrayOutputStream frame = new ByteArrayOutputStream();
			byte opcode = OPCODE_TEXT;
			int length = msg.length;
			int b0 = 0;
			
			if (fin)
				b0 |= 1 << 7;
			b0 |= rsv % 8 << 4;
			b0 |= opcode % 128;
	
	
			if (length <= 125)
			{
				frame.write(b0);
				byte b = (byte) (maskPayload ? 0x80 | (byte) length : (byte) length);
				frame.write(b);
			}
			else if (length <= 0xFFFF)
			{
				frame.write(b0);
				frame.write(maskPayload ? 0xFE : 126);
				frame.write(length >>> 8 & 0xFF);
				frame.write(length & 0xFF);
			}
			else
			{
				frame.write(b0);
				frame.write(maskPayload ? 0xFF : 127);
				for(int q = 0; q < 8; q++)
				{
					frame.write((0xFF) & (length >> q));
				}
			}
	
			frame.write(msg);
			return frame.toByteArray();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return (new byte[0]);
	}
}
