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
package org.apache.xmlrpc.serializer;

import java.io.IOException;

import org.apache.xmlrpc.util.Base64.Encoder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;




/** A {@link TypeSerializer} for byte arrays.
 */
public class ByteArraySerializer extends TypeSerializerImpl {
	/** Tag name of a base64 value.
	 */
	public static final String BASE_64_TAG = "base64";
	static class SAXIOException extends IOException {
		private static final long serialVersionUID = 3258131345216451895L;
		final SAXException saxException;
		SAXIOException(SAXException e) {
			super();
			saxException = e;
		}
		SAXException getSAXException() { return saxException; }
	}
	public void write(final ContentHandler pHandler, Object pObject) throws SAXException {
		pHandler.startElement("", VALUE_TAG, VALUE_TAG, ZERO_ATTRIBUTES);
		pHandler.startElement("", BASE_64_TAG, BASE_64_TAG, ZERO_ATTRIBUTES);
		byte[] buffer = (byte[]) pObject;
		Encoder encoder = new Encoder(buffer.length >= 1024 ? 1024 : ((buffer.length+3)/4)*4) {
			protected void writeBuffer() throws IOException {
				try {
					pHandler.characters(charBuffer, 0, charOffset);
				} catch (SAXException e) {
					throw new SAXIOException(e);
				}
			}
		};
		try {
			encoder.write(buffer, 0, buffer.length);
			encoder.flush();
		} catch (ByteArraySerializer.SAXIOException e) {
			throw e.getSAXException();
		} catch (IOException e) {
			throw new SAXException(e);
		}
		pHandler.endElement("", BASE_64_TAG, BASE_64_TAG);
		pHandler.endElement("", VALUE_TAG, VALUE_TAG);
	}
}