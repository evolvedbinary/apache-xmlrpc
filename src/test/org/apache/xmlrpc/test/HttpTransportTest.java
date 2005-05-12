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

import java.net.URL;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcHttpTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.apache.xmlrpc.webserver.WebServer;


/** Implementation of {@link BaseTestCase} for testing the
 * {@link org.apache.xmlrpc.client.XmlRpcHttpTransport}.
 */
public class HttpTransportTest extends BaseTestCase {
	private WebServer webServer = new WebServer(0);
	private boolean isActive;

	public void setUp() throws Exception {
		if (!isActive) {
			webServer.start();
			isActive = true;
		}
	}

	protected XmlRpcTransportFactory getTransportFactory(XmlRpcClient pClient) {
		return new XmlRpcHttpTransportFactory(pClient);
	}

	protected XmlRpcClientConfigImpl getConfig() throws Exception {
		XmlRpcClientConfigImpl config = super.getConfig();
		config.setServerURL(new URL("http://127.0.0.1:" + webServer.getPort() + "/"));
		return config;
	}
}
