package org.apache.xmlrpc.common;

import java.io.ByteArrayOutputStream;


/** Implementation of {@link StreamConnection} for
 * use by the
 * {@link org.apache.xmlrpc.client.XmlRpcLocalStreamTransport}.
 */
public class LocalStreamConnection
		implements ClientStreamConnection, ServerStreamConnection {
	private ByteArrayOutputStream ostream, istream;

	/** Returns the output stream, to which the response
	 * is being written.
	 */
	public ByteArrayOutputStream getOstream() {
		return ostream;
	}

	/** Sets the output stream, to which the response
	 * is being written.
	 */
	public void setOstream(ByteArrayOutputStream pOstream) {
		ostream = pOstream;
	}

	/** Returns the input stream, to which the request
	 * is being written.
	 */
	public ByteArrayOutputStream getIstream() {
		return istream;
	}

	/** Sets the input stream, to which the request
	 * is being written.
	 */
	public void setIstream(ByteArrayOutputStream pIstream) {
		istream = pIstream;
	}
}
