package org.apache.xmlrpc.common;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;


/** Interface of an object, which is able to process
 * XML-RPC requests.
 */
public interface XmlRpcRequestProcessor {
	/** Processes the given request and returns a
	 * result object.
	 * @throws XmlRpcException Processing the request failed.
	 */
	Object execute(XmlRpcRequest pRequest) throws XmlRpcException;

	/** Returns the request processors {@link TypeConverterFactory}.
	 */
    TypeConverterFactory getTypeConverterFactory();
}
