package org.apache.xmlrpc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;


/** Default implementation of an HTTP transport, based on the
 * {@link java.net.HttpURLConnection} class.
 */
public class XmlRpcHttpTransport extends XmlRpcStreamTransport {
	/** Creates a new instance.
	 * @param pClient The client controlling this instance.
	 * @param pFactory The factory creating this instance on behalf of the client.
	 */
	public XmlRpcHttpTransport(XmlRpcClient pClient, XmlRpcHttpTransportFactory pFactory) {
		super(pClient, pFactory);
	}

	protected void setRequestHeader(Object pConnection, String pHeader, String pValue) {
		URLConnection conn = (URLConnection) pConnection;
		conn.setRequestProperty(pHeader, pValue);
	}

	protected void setCredentials(XmlRpcHttpClientConfig pConfig, Object pConnection)
			throws XmlRpcClientException {
		String auth;
		try {
			auth = HttpUtil.encodeBasicAuthentication(pConfig.getBasicUserName(),
													  pConfig.getBasicPassword(),
													  pConfig.getBasicEncoding());
		} catch (UnsupportedEncodingException e) {
			throw new XmlRpcClientException("Unsupported encoding: " + pConfig.getBasicEncoding(), e);
		}
		if (auth != null) {
			setRequestHeader(pConnection, "Authorization", "Basic " + auth);
		}
	}

	protected void setCompressionHeaders(XmlRpcHttpClientConfig pConfig, Object pConnection) {
		if (pConfig.isGzipCompressing()) {
			setRequestHeader(pConnection, "Content-Encoding", "gzip");
		}
		if (pConfig.isGzipRequesting()) {
			setRequestHeader(pConnection, "Accept-Encoding", "gzip");
		}
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

	protected void initConnection(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws XmlRpcClientException {
		super.initConnection(pConfig, pConnection);
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pConfig;
		setCredentials(config, pConnection);
		setCompressionHeaders(config, pConnection);
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