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


/** Abstract base implementation of an {@link XmlRpcTransportFactory}.
 */
public abstract class XmlRpcTransportFactoryImpl implements XmlRpcTransportFactory {
	private final XmlRpcClient client;

	/** Creates a new instance.
	 * @param pClient The client, which will invoke the factory.
	 */
	protected XmlRpcTransportFactoryImpl(XmlRpcClient pClient) {
		client = pClient;
	}

	/** Returns the client operating this factory.
	 * @return The client.
	 */
	public XmlRpcClient getClient() { return client; }
}
