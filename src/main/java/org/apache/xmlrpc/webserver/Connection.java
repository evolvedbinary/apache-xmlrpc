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
package org.apache.xmlrpc.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.StringTokenizer;

import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.server.XmlRpcHttpServerConfig;
import org.apache.xmlrpc.server.XmlRpcStreamServer;
import org.apache.xmlrpc.util.HttpUtil;
import org.apache.xmlrpc.util.LimitedInputStream;
import org.apache.xmlrpc.util.ThreadPool;



/** Handler for a single clients connection. This implementation
 * is able to do HTTP keepalive. In other words, it can serve
 * multiple requests via a single, physical connection.
 */
public class Connection implements ThreadPool.Task {
	private static final String US_ASCII = "US-ASCII";
    private static final byte[] ctype = toHTTPBytes("Content-Type: text/xml\r\n");
    private static final byte[] clength = toHTTPBytes("Content-Length: ");
    private static final byte[] newline = toHTTPBytes("\r\n");
    private static final byte[] doubleNewline = toHTTPBytes("\r\n\r\n");
    private static final byte[] conkeep = toHTTPBytes("Connection: Keep-Alive\r\n");
    private static final byte[] conclose = toHTTPBytes("Connection: close\r\n");
    private static final byte[] ok = toHTTPBytes(" 200 OK\r\n");
    private static final byte[] serverName = toHTTPBytes("Server: Apache XML-RPC 1.0\r\n");
    private static final byte[] wwwAuthenticate = toHTTPBytes("WWW-Authenticate: Basic realm=XML-RPC\r\n");

	private static class BadRequestException extends IOException {
		private static final long serialVersionUID = 3257848779234554934L;
		BadRequestException(String pMethod) {
			super(pMethod);
		}
	}

	/** Returns the US-ASCII encoded byte representation of text for
     * HTTP use (as per section 2.2 of RFC 2068).
     */
    private static final byte[] toHTTPBytes(String text) {
        try {
            return text.getBytes(US_ASCII);
        } catch (UnsupportedEncodingException e) {
			throw new Error(e.getMessage() +
                            ": HTTP requires US-ASCII encoding");
        }
    }

	private final WebServer webServer;
	private final Socket socket;
    private final InputStream input;
    private final OutputStream output;
	private final XmlRpcStreamServer server;
	private byte[] buffer;

    /** Creates a new webserver connection on the given socket.
     * @param pWebServer The webserver maintaining this connection.
     * @param pServer The server being used to execute requests.
     * @param pSocket The server socket to handle; the <code>Connection</code>
     * is responsible for closing this socket.
     * @throws IOException
     */
    public Connection(WebServer pWebServer, XmlRpcStreamServer pServer, Socket pSocket)
			throws IOException {
		webServer = pWebServer;
		server = pServer;
		socket = pSocket;
		// set read timeout to 30 seconds
        socket.setSoTimeout (30000);
        input = new BufferedInputStream(socket.getInputStream()){
    		/** It may happen, that the XML parser invokes close().
    		 * Closing the input stream must not occur, because
    		 * that would close the whole socket. So we suppress it.
    		 */
        	public void close() throws IOException {
        	}
        };
        output = new BufferedOutputStream(socket.getOutputStream());
	}

	/** Returns the connections request configuration by
	 * merging the HTTP request headers and the servers configuration.
	 * @return The connections request configuration.
	 * @throws IOException Reading the request headers failed.
	 */
	private RequestData getRequestConfig() throws IOException {
		RequestData result = new RequestData(this);
		XmlRpcHttpServerConfig serverConfig = (XmlRpcHttpServerConfig) server.getConfig();
		result.setBasicEncoding(serverConfig.getBasicEncoding());
		result.setContentLengthOptional(serverConfig.isContentLengthOptional());
		result.setEnabledForExtensions(serverConfig.isEnabledForExtensions());

		// reset user authentication
		String line = readLine();
		// Netscape sends an extra \n\r after bodypart, swallow it
		if (line != null && line.length() == 0) {
			line = readLine();
			if (line == null  ||  line.length() == 0) {
				return null;
			}
    	}

		// tokenize first line of HTTP request
		StringTokenizer tokens = new StringTokenizer(line);
		String method = tokens.nextToken();
		if (!"POST".equalsIgnoreCase(method)) {
			throw new BadRequestException(method);
		}
		result.setMethod(method);
		tokens.nextToken(); // Skip URI
		String httpVersion = tokens.nextToken();
		result.setHttpVersion(httpVersion);
		result.setKeepAlive(serverConfig.isKeepAliveEnabled()
							&& WebServer.HTTP_11.equals(httpVersion));
		do {
			line = readLine();
			if (line != null) {
				String lineLower = line.toLowerCase();
				if (lineLower.startsWith("content-length:")) {
					String cLength = line.substring("content-length:".length());
					result.setContentLength(Integer.parseInt(cLength.trim()));
				} else if (lineLower.startsWith("connection:")) {
					result.setKeepAlive(serverConfig.isKeepAliveEnabled()
										&&  lineLower.indexOf("keep-alive") > -1);
				} else if (lineLower.startsWith("authorization:")) {
					String credentials = line.substring("authorization:".length());
					HttpUtil.parseAuthorization(result, credentials);
				}
			}
		}
		while (line != null && line.length() != 0);
		
		return result;
	}

    public void run() {
        try {
			for (int i = 0;  ;  i++) {
				RequestData data = getRequestConfig();
				if (data == null) {
					break;
				}
				server.execute(data, this);
				output.flush();
				if (!data.isKeepAlive()  ||  !data.isSuccess()) {
					break;
				}
			}
        } catch (Throwable t) {
			webServer.log(t);
        } finally {
			try { socket.close(); } catch (Throwable ignore) {}
        }
    }

    private String readLine() throws IOException {
        if (buffer == null) {
            buffer = new byte[2048];
        }
        int next;
        int count = 0;
        for (;;) {
            next = input.read();
            if (next < 0 || next == '\n') {
                break;
            }
            if (next != '\r') {
                buffer[count++] = (byte) next;
            }
            if (count >= buffer.length) {
                throw new IOException("HTTP Header too long");
            }
        }
        return new String(buffer, 0, count, US_ASCII);
    }

    /** Returns the contents input stream.
	 * @param pData The request data
	 * @return The contents input stream.
	 */
	public InputStream getInputStream(RequestData pData) {
		int contentLength = pData.getContentLength();
		if (contentLength == -1) {
			return input;
		} else {
			return new LimitedInputStream(input, contentLength);
		}
	}

	/** Returns the output stream for writing the response.
	 * @param pConfig The request configuration.
	 * @return The response output stream.
	 */
	public OutputStream getOutputStream(XmlRpcStreamRequestConfig pConfig) {
		boolean useContentLength;
		if (pConfig instanceof XmlRpcHttpRequestConfig) {
			useContentLength = !pConfig.isEnabledForExtensions()
						||  !((XmlRpcHttpRequestConfig) pConfig).isContentLengthOptional();
		} else {
			useContentLength = true;
		}
		if (useContentLength) {
			return new ByteArrayOutputStream();
		} else {
			return output;
		}
	}

	/** Writes the response header and the response to the
	 * output stream.
	 * @param pData The request data.
	 * @param pBuffer The {@link ByteArrayOutputStream} holding the response.
	 * @throws IOException Writing the response failed.
	 */
	public void writeResponse(RequestData pData, OutputStream pBuffer)
			throws IOException {
		ByteArrayOutputStream response = (ByteArrayOutputStream) pBuffer;
		writeResponseHeader(pData, response.size());
		response.writeTo(output);
	}

	/** Writes the response header to the output stream.	 * 
	 * @param pData The request data
	 * @param pContentLength The content length, if known, or -1.
	 * @throws IOException Writing the response failed.
	 */
	public void writeResponseHeader(RequestData pData, int pContentLength)
			throws IOException {
        output.write(toHTTPBytes(pData.getHttpVersion()));
        output.write(ok);
        output.write(serverName);
        output.write(pData.isKeepAlive() ? conkeep : conclose);
        output.write(ctype);
		if (pContentLength != -1) {
	        output.write(clength);
			output.write(toHTTPBytes(Integer.toString(pContentLength)));
	        output.write(doubleNewline);
		} else {
			output.write(newline);
		}
		pData.setSuccess(true);
	}

	/** Writes an error response to the output stream.
	 * @param pData The request data.
	 * @param pError The error being reported.
	 * @param pStream The {@link ByteArrayOutputStream} with the error response.
	 * @throws IOException Writing the response failed.
	 */
	public void writeError(RequestData pData, Throwable pError, OutputStream pStream)
			throws IOException {
		ByteArrayOutputStream errorResponse = (ByteArrayOutputStream) pStream;
		writeErrorHeader(pData, pError, errorResponse.size());
		errorResponse.writeTo(output);
	}

	/** Writes an error responses headers to the output stream.
	 * @param pData The request data.
	 * @param pError The error being reported.
	 * @param pContentLength The response length, if known, or -1.
	 * @throws IOException Writing the response failed.
	 */
	public void writeErrorHeader(RequestData pData, Throwable pError, int pContentLength)
			throws IOException {
		if (pError instanceof BadRequestException) {
	        output.write(toHTTPBytes(pData.getHttpVersion()));
	        output.write(toHTTPBytes(" 400 Bad Request"));
	        output.write(newline);
	        output.write(serverName);
	        output.write(doubleNewline);
	        output.write(toHTTPBytes("Method " + pData.getMethod() +
	                                 " not implemented (try POST)"));
		} else if (pError instanceof XmlRpcNotAuthorizedException) {
	        output.write(toHTTPBytes(pData.getHttpVersion()));
	        output.write(toHTTPBytes(" 401 Unauthorized"));
	        output.write(newline);
	        output.write(serverName);
	        output.write(wwwAuthenticate);
	        output.write(doubleNewline);
	        output.write(toHTTPBytes("Method " + pData.getMethod() + " requires a " +
	                                 "valid user name and password"));
		} else {
	        output.write(toHTTPBytes(pData.getHttpVersion()));
	        output.write(ok);
	        output.write(serverName);
	        output.write(conclose);
	        output.write(ctype);
			if (pContentLength != -1) {
		        output.write(clength);
				output.write(toHTTPBytes(Integer.toString(pContentLength)));
			}
	        output.write(doubleNewline);
		}
	}
}