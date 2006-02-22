package org.apache.xmlrpc.client;

import org.apache.xmlrpc.common.XmlRpcWorker;
import org.apache.xmlrpc.common.XmlRpcWorkerFactory;


/** A worker factory for the client, creating instances of
 * {@link org.apache.xmlrpc.client.XmlRpcClientWorker}.
 */
public class XmlRpcClientWorkerFactory extends XmlRpcWorkerFactory {
	/** Creates a new instance.
	 * @param pClient The factory controller.
	 */
	public XmlRpcClientWorkerFactory(XmlRpcClient pClient) {
		super(pClient);
	}

	/** Creates a new worker instance.
	 * @return New instance of {@link XmlRpcClientWorker}.
	 */
	protected XmlRpcWorker newWorker() {
		return new XmlRpcClientWorker(this);
	}
}
