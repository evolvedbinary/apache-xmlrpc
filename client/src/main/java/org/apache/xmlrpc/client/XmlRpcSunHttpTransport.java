package org.apache.xmlrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;


/** Default implementation of an HTTP transport, based on the
 * {@link java.net.HttpURLConnection} class.
 */
public class XmlRpcSunHttpTransport extends XmlRpcHttpTransport {
	private static final String userAgent = USER_AGENT + " (Sun HTTP Transport)";
	private URLConnection conn;

	/** Creates a new instance.
	 * @param pClient The client controlling this instance.
	 */
	public XmlRpcSunHttpTransport(XmlRpcClient pClient) {
		super(pClient, userAgent);
	}

	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();
		try {
			conn = config.getServerURL().openConnection();
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
		} catch (IOException e) {
			throw new XmlRpcException("Failed to create URLConnection: " + e.getMessage(), e);
		}
		return super.sendRequest(pRequest);
	}

	protected void setRequestHeader(String pHeader, String pValue) {
		conn.setRequestProperty(pHeader, pValue);
	}

	protected void close() throws XmlRpcClientException {
		if (conn instanceof HttpURLConnection) {
			((HttpURLConnection) conn).disconnect();
		}
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig) {
		return HttpUtil.isUsingGzipEncoding(conn.getHeaderField("Content-Encoding"));
	}

	protected InputStream getInputStream() throws XmlRpcException {
		try {
			return conn.getInputStream();
		} catch (IOException e) {
			throw new XmlRpcException("Failed to create input stream: " + e.getMessage(), e);
		}
	}

	protected void writeRequest(RequestWriter pWriter) throws XmlRpcException {
		OutputStream ostream;
		try {
			 ostream = conn.getOutputStream();
		} catch (IOException e) {
			throw new XmlRpcException("Failed to create output stream: " + e.getMessage(), e);
		}
		pWriter.write(ostream);
	}
}