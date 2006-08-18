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


/** An HTTP transport factory, which is based on the Jakarta Commons
 * HTTP Client.
 */
public class XmlRpcCommonsTransportFactory extends XmlRpcTransportFactoryImpl {
	/** Creates a new instance.
	 * @param pClient The client, which is controlling the factory.
	 */
	public XmlRpcCommonsTransportFactory(XmlRpcClient pClient) {
		super(pClient);
	}

	public XmlRpcTransport getTransport() {
		return new XmlRpcCommonsTransport(getClient());
	}
}
