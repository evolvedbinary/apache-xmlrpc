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

/** This interface allows to perform a unit test with various
 * transports. Basically, the implementation creates the client,
 * including the transport, and the server, if required.
 */
public interface ClientProvider {
	/** Returns the clients default configuration.
	 * @return The clients configuration.
	 * @throws Exception Creating the configuration failed.
	 */
	XmlRpcClientConfigImpl getConfig() throws Exception;

	/** Returns a new client instance.
	 * @return A client being used for performing the test.
	 */
	XmlRpcClient getClient();
}
