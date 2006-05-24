package org.apache.xmlrpc.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/** Implementation of {@link ServerStreamConnection} for
 * use by the
 * {@link org.apache.xmlrpc.client.XmlRpcLocalStreamTransport}.
 */
public class LocalStreamConnection {
    private class LocalServerStreamConnection implements ServerStreamConnection {
        public InputStream newInputStream() throws IOException {
            return request;
        }

        public OutputStream newOutputStream() throws IOException {
            return response;
        }

        public void close() throws IOException {
            if (response != null) {
                response.close();
            }
        }
    }

    private final InputStream request;
	private final XmlRpcStreamRequestConfig config;
	private final ByteArrayOutputStream response = new ByteArrayOutputStream();
    private final ServerStreamConnection serverStreamConnection;

	/** Creates a new instance with the given request stream.
	 */
	public LocalStreamConnection(XmlRpcStreamRequestConfig pConfig, 
			InputStream pRequest) {
		config = pConfig;
		request = pRequest;
        serverStreamConnection = new LocalServerStreamConnection();
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

    /** Returns the servers connection.
     */
    public ServerStreamConnection getServerStreamConnection() {
        return serverStreamConnection;
    }
}
