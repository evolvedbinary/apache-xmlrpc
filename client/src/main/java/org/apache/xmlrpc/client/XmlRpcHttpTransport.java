package org.apache.xmlrpc.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.util.HttpUtil;


/** Abstract base implementation of an HTTP transport. Base class for the
 * concrete implementations, like {@link org.apache.xmlrpc.client.XmlRpcSunHttpTransport},
 * or {@link org.apache.xmlrpc.client.XmlRpcCommonsTransport}.
 */
public abstract class XmlRpcHttpTransport extends XmlRpcStreamTransport {
	protected class ByteArrayRequestWriter extends RequestWriter {
		private final ByteArrayOutputStream baos = new ByteArrayOutputStream();

		protected ByteArrayRequestWriter(XmlRpcRequest pRequest)
				throws XmlRpcException {
			super(pRequest);
			super.writeUncompressed(baos);
		}

		protected void writeUncompressed(OutputStream pStream) throws XmlRpcException {
			try {
				baos.writeTo(pStream);
				pStream.close();
				pStream = null;
			} catch (IOException e) {
				throw new XmlRpcException("Failed to write request: " + e.getMessage(), e);
			} finally {
				if (pStream != null) { try { pStream.close(); } catch (Throwable ignore) {} }
			}
		}

		protected int getContentLength() { return baos.size(); }
	}

	private String userAgent;

	/** The user agent string.
	 */
	public static final String USER_AGENT = "Apache XML RPC 3.0";

	protected XmlRpcHttpTransport(XmlRpcClient pClient, String pUserAgent) {
		super(pClient);
		userAgent = pUserAgent;
	}

	protected String getUserAgent() { return userAgent; }

	protected abstract void setRequestHeader(String pHeader, String pValue);

	protected void setCredentials(XmlRpcHttpClientConfig pConfig)
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
			setRequestHeader("Authorization", "Basic " + auth);
		}
	}

	protected void setContentLength(int pLength) {
		setRequestHeader("Content-Length", Integer.toString(pLength));
	}

	protected void setCompressionHeaders(XmlRpcHttpClientConfig pConfig) {
		if (pConfig.isGzipCompressing()) {
			setRequestHeader("Content-Encoding", "gzip");
		}
		if (pConfig.isGzipRequesting()) {
			setRequestHeader("Accept-Encoding", "gzip");
		}
	}

	protected void initHttpHeaders(XmlRpcRequest pRequest) throws XmlRpcClientException {
		XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();
		setRequestHeader("Content-Type", "text/xml");
		setRequestHeader("User-Agent", getUserAgent());
		setCredentials(config);
		setCompressionHeaders(config);
	}

	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		initHttpHeaders(pRequest);
		return super.sendRequest(pRequest);
	}
	
	protected boolean isUsingByteArrayOutput(XmlRpcHttpClientConfig pConfig) {
		return !pConfig.isEnabledForExtensions()
			|| !pConfig.isContentLengthOptional();
	}

	protected RequestWriter newRequestWriter(XmlRpcRequest pRequest)
			throws XmlRpcException {
		final XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();
		if (isUsingByteArrayOutput(config)) {
			ByteArrayRequestWriter result = new ByteArrayRequestWriter(pRequest);
			setContentLength(result.getContentLength());
			return result;
		} else {
			return super.newRequestWriter(pRequest);
		}
	}
}
