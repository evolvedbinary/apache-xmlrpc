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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.apache.xmlrpc.common.XmlRpcStreamConfig;


/** Provides utility functions useful in HTTP communications
 */
public class HttpUtil {
	/** Creates the Base64 encoded credentials for HTTP Basic Authentication.
	 * @param pUser User name, or null, if no Basic Authentication is being used.
	 * @param pPassword Users password, or null, if no Basic Authentication is being used.
	 * @param pEncoding Encoding being used for conversion of the credential string into a byte array.
	 * @return Base64 encoded credentials, for use in the HTTP header 
	 * @throws UnsupportedEncodingException The encoding <code>pEncoding</code> is invalid.
	 */
	public static String encodeBasicAuthentication(String pUser, String pPassword, String pEncoding) throws UnsupportedEncodingException {
        if (pUser == null) {
			return null;
        }
		String s = pUser + ':' + pPassword;
		if (pEncoding == null) {
			pEncoding = XmlRpcStreamConfig.DEFAULT_ENCODING;
		}
		return new String(Base64.encode(s.getBytes(pEncoding)));
    }

	/** Returns, whether the HTTP header value <code>pHeaderValue</code>
	 * indicates, that GZIP encoding is used or may be used.
	 * @param pHeaderValue The HTTP header value being parser. This is typically
	 * the value of "Content-Encoding", or "Accept-Encoding".
	 * @return True, if the header value suggests that GZIP encoding is or may
	 * be used.
	 */
	public static boolean isUsingGzipEncoding(String pHeaderValue) {
		if (pHeaderValue == null) {
			return false;
        }
        for (StringTokenizer st = new StringTokenizer(pHeaderValue, ",");  st.hasMoreTokens();  ) {
            String encoding = st.nextToken();
            int offset = encoding.indexOf(';');
            if (offset >= 0) {
                encoding = encoding.substring(0, offset);
            }
            if ("gzip".equalsIgnoreCase(encoding.trim())) {
            	return true;
            }
        }
        return false;
    }

	/** Reads a header line from the input stream <code>pIn</code>
	 * and converts it into a string.
	 * @param pIn The input stream being read.
	 * @param pBuffer A buffer being used for temporary storage.
	 * The buffers length is a limit of the header lines length.
	 * @return Next header line or null, if no more header lines
	 * are available.
	 * @throws IOException Reading the header line failed.
	 */
	public static String readLine(InputStream pIn, byte[] pBuffer) throws IOException {
        int next;
        int count = 0;
        while (true) {
            next = pIn.read();
            if (next < 0 || next == '\n') {
                break;
            }
            if (next != '\r') {
                pBuffer[count++] = (byte) next;
            }
            if (count >= pBuffer.length) {
                throw new IOException ("HTTP Header too long");
            }
        }
        return new String(pBuffer, 0, count);
    }
}
