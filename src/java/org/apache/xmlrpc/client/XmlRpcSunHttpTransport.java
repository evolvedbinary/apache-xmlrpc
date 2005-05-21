package org.apache.xmlrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;


/** Default implementation of an HTTP transport, based on the
 * {@link java.net.HttpURLConnection} class.
 */
public class XmlRpcSunHttpTransport extends XmlRpcHttpTransport {
	private final String userAgent = super.getUserAgent() + " (Sun HTTP Transport)";

	/** Creates a new instance.
	 * @param pClient The client controlling this instance.
	 */
	public XmlRpcSunHttpTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	protected String getUserAgent() { return userAgent; }

	protected void setRequestHeader(Object pConnection, String pHeader, String pValue) {
		URLConnection conn = (URLConnection) pConnection;
		conn.setRequestProperty(pHeader, pValue);
	}

	protected Object newConnection(XmlRpcStreamRequestConfig pConfig) throws XmlRpcClientException {
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pConfig;
		try {
			URLConnection result = config.getServerURL().openConnection();
			result.setUseCaches(false);
			result.setDoInput(true);
			result.setDoOutput(true);
			return result;
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to create HTTP connection object", e);
		}
	}

	protected void closeConnection(Object pConnection) throws XmlRpcClientException {
		if (pConnection instanceof HttpURLConnection) {
			((HttpURLConnection) pConnection).disconnect();
		}
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection)
			throws XmlRpcClientException {
		try {
			return ((URLConnection) pConnection).getOutputStream();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to obtain output stream to server", e);
		}
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection)
			throws XmlRpcClientException {
		try {
			return ((URLConnection) pConnection).getInputStream();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to obtain input stream from server", e);
		}
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection,
										 byte[] pContent)
			throws XmlRpcClientException {
		URLConnection conn = (URLConnection) pConnection;
		try {
			OutputStream ostream = conn.getOutputStream();
			ostream.write(pContent);
			ostream.close();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to send request to server: " + e.getMessage(), e);
		}
		return newInputStream(pConfig, pConnection);
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig, Object pConnection) {
		if (pConnection instanceof HttpURLConnection) {
			HttpURLConnection conn = (HttpURLConnection) pConnection;
			return HttpUtil.isUsingGzipEncoding(conn.getHeaderField("Content-Encoding"));
		} else {
			return false;
		}
	}
}