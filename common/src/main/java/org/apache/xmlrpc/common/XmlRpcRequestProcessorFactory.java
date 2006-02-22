package org.apache.xmlrpc.common;


/** Interface of an object, which may be used
 * to create instances of {@link XmlRpcRequestProcessor}.
 */
public interface XmlRpcRequestProcessorFactory {
	/** Returns the {@link XmlRpcRequestProcessor} being invoked.
	 * @return Server object being invoked. This will typically
	 * be a singleton instance, but could as well create a new
	 * instance with any call.
	 */
	XmlRpcRequestProcessor getXmlRpcServer();
}
