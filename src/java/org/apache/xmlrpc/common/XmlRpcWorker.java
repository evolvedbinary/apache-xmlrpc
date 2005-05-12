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
package org.apache.xmlrpc.common;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;


/** An object, which executes requests on the controllers
 * behalf. These objects are mainly used for controlling the
 * clients or servers load, which is defined in terms of the
 * number of currently active workers.
 */
public interface XmlRpcWorker {
	/** Returns the workers controller.
	 * @return The controller, an instance of
	 * {@link org.apache.xmlrpc.client.XmlRpcClient}, or
	 * {@link org.apache.xmlrpc.server.XmlRpcServer}.
	 */
	XmlRpcController getController();

	/** Performs a synchronous request. The client worker extends
	 * this interface with the ability to perform asynchronous
	 * requests.
	 * @param pController The workers controller.
	 * @param pRequest The request being performed.
	 * @param pConfig The request configuration.
	 * @return The requests result.
	 * @throws XmlRpcException Performing the request failed.
	 */
	Object execute(XmlRpcRequest pRequest) throws XmlRpcException;
}
