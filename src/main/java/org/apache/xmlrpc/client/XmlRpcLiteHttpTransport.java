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
package org.apache.xmlrpc.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;
import org.apache.xmlrpc.util.LimitedInputStream;


/** A "light" HTTP transport implementation.
 */
public class XmlRpcLiteHttpTransport extends XmlRpcHttpTransport {
	private class Connection {
		private final String hostname;
	    private final String host;
		private int port;
		private final String uri;
	    private Socket socket;
		private OutputStream output;
	    private InputStream input;
		private final Map headers = new HashMap();
		Connection(URL pURL) {
	        hostname = pURL.getHost();
	        int p = pURL.getPort();
			port = p < 1 ? 80 : p;
			String u = pURL.getFile();
			uri = (u == null  ||  "".equals(u)) ? "/" : u;
			host = port == 80 ? hostname : hostname + ":" + port;
			headers.put("Host", host);
		}
	}

	private final String userAgent = super.getUserAgent() + " (Lite HTTP Transport)";

	/** Creates a new instance.
	 * @param pClient The client controlling this instance.
	 */
	public XmlRpcLiteHttpTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	protected String getUserAgent() { return userAgent; }

	protected void setRequestHeader(Object pConnection, String pHeader,
									String pValue) {
		Connection conn = (Connection) pConnection;
		Object value = conn.headers.get(pHeader);
		if (value == null) {
			conn.headers.put(pHeader, pValue);
		} else {
			List list;
			if (value instanceof String) {
				list = new ArrayList();
				list.add(value);
				conn.headers.put(pHeader, list);
			} else {
				list = (List) value;
			}
			list.add(pValue);
		}
	}

	protected boolean isResponseGzipCompressed(
			XmlRpcStreamRequestConfig pConfig, Object pConnection) {
		// TODO Auto-generated method stub
		return false;
	}

	protected Object newConnection(XmlRpcStreamRequestConfig pConfig)
			throws XmlRpcClientException {
		return new Connection(((XmlRpcHttpClientConfig) pConfig).getServerURL());
	}

	protected void closeConnection(Object pConnection)
			throws XmlRpcClientException {
		Connection conn = (Connection) pConnection;
		IOException e = null;
		if (conn.input != null) {
			try {
				conn.input.close();
			} catch (IOException ex) {
				e = ex;
			}
		}
		if (conn.output != null) {
			try {
				conn.output.close();
			} catch (IOException ex) {
				if (e != null) {
					e = ex;
				}
			}
		}
		if (conn.socket != null) {
			try {
				conn.socket.close();
			} catch (IOException ex) {
				if (e != null) {
					e = ex;
				}
			}
		}
		if (e != null) {
			throw new XmlRpcClientException("Failed to close connection: " + e.getMessage(), e);
		}
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig,
										   Object pConnection) throws XmlRpcClientException {
		final Connection conn = (Connection) pConnection;
		final int retries = 3;
        final int delayMillis = 100;
        
		for (int tries = 0;  ;  tries++) {
			try {
				conn.socket = new Socket(conn.hostname, conn.port);
				conn.output = new BufferedOutputStream(conn.socket.getOutputStream()){
					/** Closing the output stream would close the whole socket, which we don't want,
					 * because the don't want until the request is processed completely.
					 * A close will later occur within
					 * {@link XmlRpcLiteHttpTransport#closeConnection(Object)}.
					 */
					public void close() throws IOException {
						flush();
						conn.socket.shutdownOutput();
					}
				};
				sendRequestHeaders(conn, conn.output);
				return conn.output;
			} catch (ConnectException e) {
				if (tries >= retries) {
					throw new XmlRpcClientException("Failed to connect to " + conn.host + ": " + e.getMessage(), e);
				} else {
                    try {
                        Thread.sleep(delayMillis);
                    } catch (InterruptedException ignore) {
                    }
				}
			} catch (IOException e) {
				throw new XmlRpcClientException("Failed to connect to " + conn.host + ": " + e.getMessage(), e);
			}
		}
	}

	private byte[] toHTTPBytes(String pValue) throws UnsupportedEncodingException {
		return pValue.getBytes("US-ASCII");
	}

	private void sendHeader(OutputStream pOut, String pKey, String pValue) throws IOException {
		pOut.write(toHTTPBytes(pKey + ": " + pValue + "\r\n"));
	}

	private void sendRequestHeaders(Connection pConnection,
									OutputStream pOut) throws IOException {
		pOut.write(("POST " + pConnection.uri + " HTTP/1.0\r\n").getBytes("US-ASCII"));
		for (Iterator iter = pConnection.headers.entrySet().iterator();  iter.hasNext();  ) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String) entry.getKey();
			Object value = entry.getValue();
			if (value instanceof String) {
				sendHeader(pOut, key, (String) value);
			} else {
				List list = (List) value;
				for (int i = 0;  i < list.size();  i++) {
					sendHeader(pOut, key, (String) list.get(i));
				}
			}
		}
		pOut.write(toHTTPBytes("\r\n"));
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig,
										 Object pConnection,
										 byte[] pContent) throws XmlRpcException {
		try {
			Connection conn = (Connection) pConnection;
			conn.socket = new Socket(conn.hostname, conn.port);
			conn.output = new BufferedOutputStream(conn.socket.getOutputStream());
			sendRequestHeaders(conn, conn.output);
			conn.output.write(pContent);
			conn.output.flush();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to send request to sender: " + e.getMessage(), e);
		}
		return newInputStream(pConfig, pConnection);
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig,
										 Object pConnection) throws XmlRpcException {
		Connection conn = (Connection) pConnection;
		final byte[] buffer = new byte[2048];
		try {
			conn.input = new BufferedInputStream(conn.socket.getInputStream());
			// start reading  server response headers
			String line = HttpUtil.readLine(conn.input, buffer);
			StringTokenizer tokens = new StringTokenizer(line);
			tokens.nextToken(); // Skip HTTP version
			String statusCode = tokens.nextToken();
			String statusMsg = tokens.nextToken("\n\r");
			if (! "200".equals(statusCode)) {
				throw new IOException("Unexpected Response from Server: "
									  + statusMsg);
			}
			int contentLength = -1;
			for (;;) {
				line = HttpUtil.readLine(conn.input, buffer);
				if (line == null  ||  "".equals(line)) {
					break;
				}
				line = line.toLowerCase();
				if (line.startsWith("content-length:")) {
					contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
				}
			}
			InputStream result;
			if (contentLength == -1) {
				result = conn.input;
			} else {
				result = new LimitedInputStream(conn.input, contentLength);
			}
			return result;
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to read server response: " + e.getMessage(), e);
		}
	}
}
