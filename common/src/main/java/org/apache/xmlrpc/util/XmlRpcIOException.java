package org.apache.xmlrpc.util;

import java.io.IOException;


/** This is a subclass of {@link IOException}, which
 * allows to attach a linked exception. Throwing this
 * particular instance of {@link IOException} allows
 * to catch it and throw the linked exception instead.
 */
public class XmlRpcIOException extends IOException {
	private static final long serialVersionUID = -7704704099502077919L;
	private final Throwable linkedException;

	/** Creates a new instance of {@link XmlRpcIOException}
	 * with the given cause.
	 */
	public XmlRpcIOException(Throwable t) {
		super(t.getMessage());
		linkedException = t;
	}

	/** Returns the linked exception, which is the actual
	 * cause for this exception.
	 */
	public Throwable getLinkedException() {
		return linkedException;
	}
}
