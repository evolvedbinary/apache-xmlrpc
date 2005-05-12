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
package org.apache.xmlrpc.test;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcLocalTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;


/** Implementation of {@link org.apache.xmlrpc.test.BaseTestCase}
 * for testing the {@link org.apache.xmlrpc.client.XmlRpcLocalTransport}.
 */
public class LocalTransportTest extends BaseTestCase {
	protected XmlRpcTransportFactory getTransportFactory(XmlRpcClient pClient) {
		XmlRpcLocalTransportFactory factory = new XmlRpcLocalTransportFactory(pClient);
		return factory;
	}

	protected XmlRpcClientConfigImpl getConfig() throws Exception {
		XmlRpcClientConfigImpl config = super.getConfig();
		config.setXmlRpcServer(getXmlRpcServer());
		return config;
	}
}
