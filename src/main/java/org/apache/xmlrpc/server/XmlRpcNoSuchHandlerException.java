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
package org.apache.xmlrpc.server;

import org.apache.xmlrpc.XmlRpcException;


/** This exception is thrown, if an unknown handler is called.
 */
public class XmlRpcNoSuchHandlerException extends XmlRpcException {
	private static final long serialVersionUID = 3257002138218344501L;

	/** Creates a new instance with the given message.
	 * @param pMessage The error details.
	 */
	public XmlRpcNoSuchHandlerException(String pMessage) {
		super(0, pMessage);
	}
}
