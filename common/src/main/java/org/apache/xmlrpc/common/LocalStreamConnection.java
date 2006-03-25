package org.apache.xmlrpc.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;


/** Implementation of {@link ServerStreamConnection} for
 * use by the
 * {@link org.apache.xmlrpc.client.XmlRpcLocalStreamTransport}.
 */
public class LocalStreamConnection
		implements ServerStreamConnection {
	private final InputStream request;
	private final XmlRpcStreamRequestConfig config;
	private final ByteArrayOutputStream response = new ByteArrayOutputStream();

	/** Creates a new instance with the given request stream.
	 */
	public LocalStreamConnection(XmlRpcStreamRequestConfig pConfig, 
			InputStream pRequest) {
		config = pConfig;
		request = pRequest;
	}

	/** Returns the request stream.
	 */
	public InputStream getRequest() {
		return request;
	}

	/** Returns the request configuration.
	 */
	public XmlRpcStreamRequestConfig getConfig() {
		return config;
	}

	/** Returns an output stream, to which the response
	 * may be written.
	 */
	public ByteArrayOutputStream getResponse() {
		return response;
	}
}
