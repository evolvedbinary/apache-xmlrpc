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

import java.util.Map;

import org.apache.xmlrpc.XmlRpcConfig;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcExtensionException;

/** The default implementation of a local transport.
 */
public class XmlRpcLocalTransport extends XmlRpcTransportImpl {
	/** Creates a new instance.
	 * @param pClient The client, which creates the transport.
	 */
	public XmlRpcLocalTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	private boolean isExtensionType(Object pObject) {
		if (pObject == null) {
			return true;
		} else {
			return !(pObject instanceof Integer
					 ||  pObject instanceof String
					 ||  pObject instanceof byte[]
					 ||  pObject instanceof Object[]
					 ||  pObject instanceof Double
					 ||  pObject instanceof Map);
		}
	}

	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcConfig config = pRequest.getConfig();
		if (!config.isEnabledForExtensions()) {
			for (int i = 0;  i < pRequest.getParameterCount();  i++) {
				if (isExtensionType(pRequest.getParameter(i))) {
					throw new XmlRpcExtensionException("Parameter " + i + " has invalid type, if isEnabledForExtensions() == false");
				}
			}
		}
		Object result;
		try {
			result = ((XmlRpcLocalClientConfig) config).getXmlRpcServer().execute(pRequest);
		} catch (Throwable t) {
			if (t instanceof XmlRpcClientException) {
				throw (XmlRpcClientException) t;
			} else {
				throw new XmlRpcClientException("Failed to invoke method " + pRequest.getMethodName()
												+ ": " + t.getMessage(), t);
			}
		}
		if (!config.isEnabledForExtensions()) {
			if (isExtensionType(result)) {
				throw new XmlRpcExtensionException("Result has invalid type, if isEnabledForExtensions() == false");
			}
		}
		return result;
	}
}