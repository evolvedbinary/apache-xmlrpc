/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xmlrpc.client;

import java.io.Serializable;
import java.net.URL;

import org.apache.xmlrpc.common.XmlRpcHttpRequestConfigImpl;
import org.apache.xmlrpc.server.XmlRpcServer;


/** Default implementation of a clients request configuration.
 */
public class XmlRpcClientConfigImpl extends XmlRpcHttpRequestConfigImpl
		implements XmlRpcHttpClientConfig, XmlRpcLocalClientConfig, Cloneable, Serializable {
	private static final long serialVersionUID = 4121131450507800889L;
	private URL serverURL;
	private XmlRpcServer xmlRpcServer;

	/** Creates a new client configuration with default settings.
	 */
	public XmlRpcClientConfigImpl() {
	}

	/** Creates a clone of this client configuration.
	 * @return A clone of this configuration.
	 */
	public XmlRpcClientConfigImpl cloneMe() {
		try {
			return (XmlRpcClientConfigImpl) clone();
		} catch (CloneNotSupportedException e) {
			throw new IllegalStateException("Unable to create my clone");
		}
	}

	/** Sets the servers URL.
	 * @param pURL Servers URL
	 */
	public void setServerURL(URL pURL) {
		serverURL = pURL;
	}
	public URL getServerURL() { return serverURL; }
	/** Returns the {@link org.apache.xmlrpc.server.XmlRpcServer} being invoked.
	 * @param pServer Server object being invoked. This will typically
	 * be a singleton instance, but could as well create a new
	 * instance with any call.
	 */
	public void setXmlRpcServer(XmlRpcServer pServer) {
		xmlRpcServer = pServer;
	}
	public XmlRpcServer getXmlRpcServer() { return xmlRpcServer; }
}