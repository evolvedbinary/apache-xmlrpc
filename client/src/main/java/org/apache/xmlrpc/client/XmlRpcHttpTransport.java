package org.apache.xmlrpc.client;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.ClientStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;


/** Abstract base implementation of an HTTP transport. Base class for the
 * concrete implementations, like {@link org.apache.xmlrpc.client.XmlRpcSunHttpTransport},
 * or {@link org.apache.xmlrpc.client.XmlRpcCommonsTransport}.
 */
public abstract class XmlRpcHttpTransport extends XmlRpcStreamTransport {
	/** The user agent string.
	 */
	public static final String USER_AGENT = "Apache XML RPC 3.0";

	protected XmlRpcHttpTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	protected abstract void setRequestHeader(ClientStreamConnection pConnection, String pHeader, String pValue);

	protected void setCredentials(XmlRpcHttpClientConfig pConfig, ClientStreamConnection pConnection)
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

	protected void setContentLength(ClientStreamConnection pConnection, int pLength) {
		setRequestHeader(pConnection, "Content-Length", Integer.toString(pLength));
	}

	protected InputStream getInputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection, byte[] pContent) throws XmlRpcException {
		if (pContent != null) {
			setContentLength(pConnection, pContent.length);
		}
		return super.getInputStream(pConfig, pConnection, pContent);
	}

	protected void setCompressionHeaders(XmlRpcHttpClientConfig pConfig, ClientStreamConnection pConnection) {
		if (pConfig.isGzipCompressing()) {
			setRequestHeader(pConnection, "Content-Encoding", "gzip");
		}
		if (pConfig.isGzipRequesting()) {
			setRequestHeader(pConnection, "Accept-Encoding", "gzip");
		}
	}

	protected String getUserAgent() { return USER_AGENT; }

	protected void initConnection(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection) throws XmlRpcClientException {
		super.initConnection(pConfig, pConnection);
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pConfig;
		setRequestHeader(pConnection, "Content-Type", "text/xml");
		setRequestHeader(pConnection, "User-Agent", getUserAgent());
		setCredentials(config, pConnection);
		setCompressionHeaders(config, pConnection);
	}

	protected abstract boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection);

	protected boolean isUsingByteArrayOutput(XmlRpcStreamRequestConfig pConfig) {
		return !pConfig.isEnabledForExtensions()
			|| !((XmlRpcHttpClientConfig) pConfig).isContentLengthOptional();
	}
}
