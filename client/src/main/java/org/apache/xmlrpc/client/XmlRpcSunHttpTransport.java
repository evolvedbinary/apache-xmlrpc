package org.apache.xmlrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.xmlrpc.common.ClientStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;


/** Default implementation of an HTTP transport, based on the
 * {@link java.net.HttpURLConnection} class.
 */
public class XmlRpcSunHttpTransport extends XmlRpcHttpTransport {
	private static class SunClientConnection implements ClientStreamConnection {
		final URLConnection conn;
		SunClientConnection(URLConnection pConn) {
			conn = pConn;
		}
	}

	private final String userAgent = super.getUserAgent() + " (Sun HTTP Transport)";

	/** Creates a new instance.
	 * @param pClient The client controlling this instance.
	 */
	public XmlRpcSunHttpTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	protected String getUserAgent() { return userAgent; }

	protected void setRequestHeader(ClientStreamConnection pConnection, String pHeader, String pValue) {
		URLConnection conn = ((SunClientConnection) pConnection).conn;
		conn.setRequestProperty(pHeader, pValue);
	}

	protected ClientStreamConnection newConnection(XmlRpcStreamRequestConfig pConfig) throws XmlRpcClientException {
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pConfig;
		try {
			URLConnection result = config.getServerURL().openConnection();
			result.setUseCaches(false);
			result.setDoInput(true);
			result.setDoOutput(true);
			return new SunClientConnection(result);
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to create HTTP connection object", e);
		}
	}

	protected void closeConnection(ClientStreamConnection pConnection) throws XmlRpcClientException {
		URLConnection conn = ((SunClientConnection) pConnection).conn;
		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection)
			throws XmlRpcClientException {
		try {
			return ((SunClientConnection) pConnection).conn.getOutputStream();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to obtain output stream to server", e);
		}
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection)
			throws XmlRpcClientException {
		try {
			return ((SunClientConnection) pConnection).conn.getInputStream();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to obtain input stream from server", e);
		}
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection,
										 byte[] pContent)
			throws XmlRpcClientException {
		URLConnection conn = ((SunClientConnection) pConnection).conn;
		try {
			OutputStream ostream = conn.getOutputStream();
			ostream.write(pContent);
			ostream.close();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to send request to server: " + e.getMessage(), e);
		}
		return newInputStream(pConfig, pConnection);
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection) {
		URLConnection conn = ((SunClientConnection) pConnection).conn;
		return HttpUtil.isUsingGzipEncoding(conn.getHeaderField("Content-Encoding"));
	}
}