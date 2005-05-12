/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xmlrpc.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.UndeclaredThrowableException;


/** Performs Base64 encoding and/or decoding. This is an on-the-fly decoder: Unlike,
 * for example, the commons-codec classes, it doesn't depend on byte array. In
 * other words, it has an extremely low memory profile.
 */
public class Base64 {
	/** An exception of this type is thrown, if the decoded
	 * character stream contains invalid input.
	 */
	public static class DecodingException extends IOException {
		private static final long serialVersionUID = 3257006574836135478L;
		DecodingException(String pMessage) { super(pMessage); }
	}

	/** Default line separator: \r\n
	 */
	public static final String LINE_SEPARATOR = "\r\n";

	/** Default size for line wrapping.
	 */
	public static final int LINE_SIZE = 76;

	/**
     * This array is a lookup table that translates 6-bit positive integer
     * index values into their "Base64 Alphabet" equivalents as specified 
     * in Table 1 of RFC 2045.
     */
    private static final char intToBase64[] = {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
        'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
        'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * This array is a lookup table that translates unicode characters
     * drawn from the "Base64 Alphabet" (as specified in Table 1 of RFC 2045)
     * into their 6-bit positive integer equivalents.  Characters that
     * are not in the Base64 alphabet but fall within the bounds of the
     * array are translated to -1.
     */
    private static final byte base64ToInt[] = {
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4,
        5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23,
        24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34,
        35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
    };

	/** An encoder is an object, which is able to encode byte array
	 * in blocks of three bytes. Any such block is converted into an
	 * array of four bytes.
	 */
	public static abstract class Encoder {
		private int num, numBytes;
		protected final char[] charBuffer;
		protected int charOffset;
		protected Encoder(int pBufSize) {
			charBuffer = new char[pBufSize];
		}
		protected abstract void writeBuffer() throws IOException;
		/** Encodes the given byte array.
		 * @param pBuffer Byte array being encoded.
		 * @param pOffset Offset of first byte being encoded.
		 * @param pLen Number of bytes being encoded.
		 * @throws IOException Invoking the {@link #writeBuffer()} method
		 * for writing the encoded data failed.
		 */
		public void write(byte[] pBuffer, int pOffset, int pLen) throws IOException {
			for(int i = 0;  i < pLen;  i++) {
				num = (num << 8) + pBuffer[pOffset++];
				pLen--;
				if (++numBytes == 3) {
					charBuffer[charOffset++] = intToBase64[num >> 18];
					charBuffer[charOffset++] = intToBase64[(num >> 12) & 0x3f];
					charBuffer[charOffset++] = intToBase64[(num >> 6) & 0x3f];
					charBuffer[charOffset++] = intToBase64[num & 0x3f];
					num = 0;
					numBytes = 0;
					if (charOffset == charBuffer.length) {
						writeBuffer();
						charOffset = 0;
					}
				}
			}
		}
		/** Writes any currently buffered data to the destination.
		 * @throws IOException Invoking the {@link #writeBuffer()} method
		 * for writing the encoded data failed.
		 */
		public void flush() throws IOException {
			if (numBytes > 0) {
				if (numBytes == 1) {
					charBuffer[charOffset++] = intToBase64[num >> 2];
					charBuffer[charOffset++] = intToBase64[(num << 4) & 0x3f];
					charBuffer[charOffset++] = '=';
					charBuffer[charOffset++] = '=';
				} else {
					charBuffer[charOffset++] = intToBase64[num >> 10];
					charBuffer[charOffset++] = intToBase64[(num >> 4) & 0x3f];
					charBuffer[charOffset++] = intToBase64[(num << 2) & 0x3f];
				}
				writeBuffer();
				charOffset = 0;
				num = 0;
				numBytes = 0;
			}
		}
	}

	/** Returns an {@link OutputStream}, that encodes its input in Base64
	 * and writes it to the given {@link Writer}. If the Base64 stream
	 * ends, then the output streams {@link OutputStream#close()} method
	 * must be invoked. Note, that this will <em>not</em> close the
	 * target {@link Writer}!
	 * @param pWriter Target writer.
	 * @return An output stream, encoding its input in Base64 and writing
	 * the output to the writer <code>pWriter</code>.
	 */
	public OutputStream newEncoder(final Writer pWriter) {
		return new OutputStream(){
			private final byte[] oneByte = new byte[1];
			private final Encoder encoder = new Encoder(1024){
				protected void writeBuffer() throws IOException {
					pWriter.write(charBuffer, 0, charOffset);
				}
			};
			public void write(int b) throws IOException {
				oneByte[0] = (byte) b;
				encoder.write(oneByte, 0, 1);
			}
			public void write(byte[] pBuffer, int pOffset, int pLen) throws IOException {
				encoder.write(pBuffer, pOffset, pLen);
			}
			public void close() throws IOException {
				encoder.flush();
			}
		};
	}

	/** Converts the given byte array into a base64 encoded character
	 * array.
	 * @param pBuffer The buffer being encoded.
	 * @param pOffset Offset in buffer, where to begin encoding.
	 * @param pLen Number of bytes being encoded.
	 * @return Character array of encoded bytes.
	 */
	public static char[] encode(byte[] pBuffer, int pOffset, int pLen) {
		int resultSize = ((pLen + 2) / 3) * 4;
		Encoder encoder = new Encoder(resultSize){
			protected void writeBuffer() throws IOException {
			}
		};
		try {
			encoder.write(pBuffer, pOffset, pLen);
		} catch (IOException e) {
			throw new UndeclaredThrowableException(e);
		}
		return encoder.charBuffer;
	}

	/** Converts the given byte array into a base64 encoded character
	 * array.
	 * @param pBuffer The buffer being encoded.
	 * @return Character array of encoded bytes.
	 */
	public static char[] encode(byte[] pBuffer) {
		return encode(pBuffer, 0, pBuffer.length);
	}

	/** An encoder is an object, which is able to decode char arrays
	 * in blocks of four bytes. Any such block is converted into a
	 * array of three bytes.
	 */
	public static abstract class Decoder {
		protected byte[] byteBuffer;
		protected int byteBufferOffset;
		private int num, numBytes;
		private int eofBytes;
		protected Decoder(int pBufLen) {
			byteBuffer = new byte[pBufLen];
		}
		protected abstract void writeBuffer() throws IOException;
		/** Converts the Base64 encoded character array.
		 * @param pData The character array being decoded.
		 * @param pOffset Offset of first character being decoded.
		 * @param pLen Number of characters being decoded.
		 * @throws DecodingException Decoding failed.
		 * @throws IOException An invocation of the {@link #writeBuffer()} method failed.
		 */
		public void write(char[] pData, int pOffset, int pLen) throws IOException {
			for (int i = 0;  i < pLen;  i++) {
				char c = pData[pOffset++];
				pLen--;
				if (c == '=') {
					++eofBytes;
					num = num << 6;
					switch(++numBytes) {
						case 1:
						case 2:
							throw new DecodingException("Unexpected end of stream character (=)");
						case 3:
							// Wait for the next '='
							break;
						case 4:
							byteBuffer[byteBufferOffset++] = (byte) (num >> 8);
							if (eofBytes == 1) {
								byteBuffer[byteBufferOffset++] = (byte) ((num >> 8) & 0xff);
							}
							writeBuffer();
							break;
						case 5:
							throw new DecodingException("Trailing garbage detected");
						default:
							throw new IllegalStateException("Invalid value for numBytes");
					}
				} else {
					if (eofBytes > 0) {
						throw new DecodingException("Base64 characters after end of stream character (=) detected.");
					}
					int result;
					if (c >= 0  &&  c < base64ToInt.length) {
						result = base64ToInt[c];
						if (result >= 0) {
							num = (num << 6) + result;
							if (++numBytes == 4) {
								byteBuffer[byteBufferOffset++] = (byte) (num >> 16);
								byteBuffer[byteBufferOffset++] = (byte) ((num >> 8) & 0xff);
								byteBuffer[byteBufferOffset++] = (byte) (num & 0xff);
								if (byteBufferOffset + 3 > byteBuffer.length) {
									writeBuffer();
									byteBufferOffset = 0;
								}
								num = 0;
								numBytes = 0;
							}
							continue;
						}
				    }
					if (!Character.isWhitespace(c)) {
						throw new DecodingException("Invalid Base64 character: " + (int) c);
					}
				}
			}
		}
		/* Indicates, that no more data is being expected.
		 * @throws DecodingException Decoding failed (Unexpected end of file).
		 * @throws IOException An invocation of the {@link #writeBuffer()} method failed.
		 */
		protected void finished() throws IOException {
			if (numBytes != 0  &&  numBytes != 4) {
				throw new DecodingException("Unexpected end of file");
			}
		}
	}

	/** Returns a {@link Writer}, that decodes its Base64 encoded
	 * input and writes it to the given {@link OutputStream}.
	 * Note, that the writers {@link Writer#close()} method will
	 * <em>not</em> close the output stream <code>pStream</code>!
	 * @param pStream Target output stream.
	 * @return An output stream, encoding its input in Base64 and writing
	 * the output to the writer <code>pWriter</code>.
	 */
	public Writer newDecoder(final OutputStream pStream) {
		return new Writer(){
			private final Decoder decoder = new Decoder(1024){
				protected void writeBuffer() throws IOException {
					pStream.write(byteBuffer, 0, byteBufferOffset);
				}
			};
			public void close() throws IOException {
				decoder.finished();
			}
			public void flush() throws IOException {
				decoder.writeBuffer();
				pStream.flush();
			}
			public void write(char[] cbuf, int off, int len) throws IOException {
				decoder.write(cbuf, off, len);
			}
		};
	}

	/** Converts the given base64 encoded character buffer into a byte array.
	 * @param pBuffer The character buffer being decoded.
	 * @param pOffset Offset of first character being decoded.
	 * @param pLen Number of characters being decoded.
	 * @return Converted byte array
	 * @throws DecodingException The input character stream contained invalid data.
	 */
	public static byte[] decode(char[] pBuffer, int pOffset, int pLen) throws DecodingException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(){
			/** The original implementation would return a clone,
			 * which we don't want.
			 */
			public byte[] toByteArray() { return buf; }
		};
		Decoder d = new Decoder(1024){
			protected void writeBuffer() throws IOException {
				baos.write(byteBuffer, 0, byteBufferOffset);
			}
		};
		try {
			d.write(pBuffer, pOffset, pLen);
		} catch (DecodingException e) {
			throw e;
		} catch (IOException e) {
			throw new UndeclaredThrowableException(e);
		}
		return baos.toByteArray();
	}

	/** Converts the given base64 encoded character buffer into a byte array.
	 * @param pBuffer The character buffer being decoded.
	 * @return Converted byte array
	 * @throws DecodingException The input character stream contained invalid data.
	 */
	public byte[] decode(char[] pBuffer) throws DecodingException {
		return decode(pBuffer, 0, pBuffer.length);
	}

	/** Converts the given base64 encoded String into a byte array.
	 * @param pBuffer The string being decoded.
	 * @return Converted byte array
	 * @throws DecodingException The input character stream contained invalid data.
	 */
	public byte[] decode(String pBuffer) throws DecodingException {
		return decode(pBuffer.toCharArray());
	}
}
