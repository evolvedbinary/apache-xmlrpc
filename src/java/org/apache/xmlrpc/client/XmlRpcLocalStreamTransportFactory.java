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


/** Another local transport factory for debugging and testing. This one is
 * similar to the {@link org.apache.xmlrpc.client.XmlRpcLocalTransportFactory},
 * except that it adds request serialization. In other words, it is
 * particularly well suited for development and testing of XML serialization
 * and parsing.
 */
public class XmlRpcLocalStreamTransportFactory extends XmlRpcStreamTransportFactory {
	private final XmlRpcLocalStreamTransport LOCAL_STREAM_TRANSPORT;

	/** Creates a new instance.
	 * @param pClient The client controlling the factory.
	 */
	public XmlRpcLocalStreamTransportFactory(XmlRpcClient pClient) {
		super(pClient);
		LOCAL_STREAM_TRANSPORT = new XmlRpcLocalStreamTransport(pClient, this);
	}

	public XmlRpcTransport getTransport() { return LOCAL_STREAM_TRANSPORT; }
}
