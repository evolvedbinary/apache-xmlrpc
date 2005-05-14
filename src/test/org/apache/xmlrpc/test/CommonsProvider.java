package org.apache.xmlrpc.test;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;


/** Provider for testing the
 * {@link org.apache.xmlrpc.client.XmlRpcCommonsTransport}.
 */
public class CommonsProvider extends WebServerProvider {
	/** Creates a new instance.
	 * @param pMapping The test servers handler mapping.
	 */
	public CommonsProvider(XmlRpcHandlerMapping pMapping) {
		super(pMapping);
	}

	protected XmlRpcTransportFactory getTransportFactory(XmlRpcClient pClient) {
		return new XmlRpcCommonsTransportFactory(pClient);
	}
}
