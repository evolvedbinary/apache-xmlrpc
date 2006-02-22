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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.common.ClientStreamConnection;
import org.apache.xmlrpc.common.LocalStreamConnection;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestProcessor;


/** Another local transport for debugging and testing. This one is
 * similar to the {@link org.apache.xmlrpc.client.XmlRpcLocalTransport},
 * except that it adds request serialization. In other words, it is
 * particularly well suited for development and testing of XML serialization
 * and parsing.
 */
public class XmlRpcLocalStreamTransport extends XmlRpcStreamTransport {
	private final XmlRpcStreamRequestProcessor localServer;
	
	/** Creates a new instance.
	 * @param pClient The client, which is controlling the transport.
	 * @param pServer An instance of {@link XmlRpcLocalStreamServer}.
	 */
	public XmlRpcLocalStreamTransport(XmlRpcClient pClient,
			XmlRpcStreamRequestProcessor pServer) {
		super(pClient);
		localServer = pServer;
	}

	protected ClientStreamConnection newConnection(XmlRpcStreamRequestConfig pConfig) throws XmlRpcClientException {
		return new LocalStreamConnection();
	}

	protected void closeConnection(ClientStreamConnection pConnection) throws XmlRpcClientException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		final ByteArrayOutputStream ostream = lsc.getOstream();
		if (ostream != null) {
			try { ostream.close(); } catch (Throwable ignore) {}
		}
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection) throws XmlRpcClientException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		ByteArrayOutputStream ostream = new ByteArrayOutputStream();
		lsc.setOstream(ostream);
		return ostream;
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection)
			throws XmlRpcException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		try {
			localServer.execute(pConfig, lsc);
		} catch (IOException e) {
			throw new XmlRpcException(e.getMessage(), e);
		}
		return new ByteArrayInputStream(lsc.getIstream().toByteArray());
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection) {
		return pConfig.isGzipRequesting();
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, ClientStreamConnection pConnection, byte[] pContent) throws XmlRpcException {
		throw new IllegalStateException("Not implemented");
	}
}
