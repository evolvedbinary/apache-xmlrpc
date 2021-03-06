/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.xmlrpc.common;


/** Extension of org.apache.xmlrpc.client.XmlRpcClientConfig
 * for HTTP based transport. Provides details like server URL,
 * user credentials, and so on.
 */
public interface XmlRpcHttpRequestConfig extends XmlRpcStreamRequestConfig, XmlRpcHttpConfig {
	/** Returns the user name being used for basic HTTP authentication.
	 * @return User name or null, if no basic HTTP authentication is being used.
	 */
	String getBasicUserName();
	/** Returns the password being used for basic HTTP authentication.
	 * @return Password or null, if no basic HTTP authentication is beind used.
	 * @throws IllegalStateException A user name is configured, but no password.
	 */ 
	String getBasicPassword();
    
    /** Returns the connection timeout in milliseconds. Note, that this value
     * may or may not be used, depending on the transport factory. Transport factories,
     * which are known to use this value:
     * org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory,
     * and org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory.
     * On the other hand, transport
     * factories which are known <em>not</em> to use this value:
     * org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory,
     * org.apache.xmlrpc.client.XmlRpcSun14HttpTransportFactory,
     * org.apache.xmlrpc.client.XmlRpcLiteHttpTransportFactory,
     * and org.apache.xmlrpc.client.XmlRpcLite14HttpTransport.
     * 
     * @return connection timeout in milliseconds or 0 if no set
     */
    int getConnectionTimeout();

    /** Return the reply timeout in milliseconds.  Note, that this value
     * may or may not be used, depending on the transport factory. Transport factories,
     * which are known to use this value:
     * org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory,
     * and org.apache.xmlrpc.client.XmlRpcSun15HttpTransportFactory.
     * On the other hand, transport
     * factories which are known <em>not</em> to use this value:
     * org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory,
     * org.apache.xmlrpc.client.XmlRpcSun14HttpTransportFactory,
     * org.apache.xmlrpc.client.XmlRpcLiteHttpTransportFactory,
     * and org.apache.xmlrpc.client.XmlRpcLite14HttpTransport.
     * @return reply timeout in milliseconds or 0 if no set
     */
    int getReplyTimeout();
}
