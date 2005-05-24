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
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcStreamServer;


/** Another local transport for debugging and testing. This one is
 * similar to the {@link org.apache.xmlrpc.client.XmlRpcLocalTransport},
 * except that it adds request serialization. In other words, it is
 * particularly well suited for development and testing of XML serialization
 * and parsing.
 */
public class XmlRpcLocalStreamTransport extends XmlRpcStreamTransport {
	private class LocalStreamConnection {
		ByteArrayOutputStream ostream, istream;
	}
	private class LocalServer extends XmlRpcStreamServer {
		public Object execute(XmlRpcRequest pRequest) throws XmlRpcException {
			XmlRpcServer server = ((XmlRpcLocalClientConfig) pRequest.getConfig()).getXmlRpcServer();
			return server.execute(pRequest);
		}
		protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws IOException {
			LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
			return new ByteArrayInputStream(lsc.ostream.toByteArray());
		}
		protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws IOException {
			LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
			lsc.istream = new ByteArrayOutputStream();
			return lsc.istream;
		}
		protected void closeConnection(Object pConnection) throws IOException {
			LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
			if (lsc.istream != null) {
				try { lsc.istream.close(); } catch (Throwable ignore) {}
			}
		}
		
	}

	private final XmlRpcStreamServer localServer = new LocalServer();

	/** Creates a new instance.
	 * @param pClient The client, which is controlling the transport.
	 */
	public XmlRpcLocalStreamTransport(XmlRpcClient pClient) {
		super(pClient);
	}

	protected Object newConnection(XmlRpcStreamRequestConfig pConfig) throws XmlRpcClientException {
		return new LocalStreamConnection();
	}

	protected void closeConnection(Object pConnection) throws XmlRpcClientException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		if (lsc.ostream != null) {
			try { lsc.ostream.close(); } catch (Throwable ignore) {}
		}
	}

	protected OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws XmlRpcClientException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		lsc.ostream = new ByteArrayOutputStream();
		return lsc.ostream;
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection)
			throws XmlRpcException {
		LocalStreamConnection lsc = (LocalStreamConnection) pConnection;
		try {
			localServer.execute(pConfig, pConnection);
		} catch (IOException e) {
			throw new XmlRpcException(e.getMessage(), e);
		}
		return new ByteArrayInputStream(lsc.istream.toByteArray());
	}

	protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig, Object pConnection) {
		return pConfig.isGzipRequesting();
	}

	protected InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection, byte[] pContent) throws XmlRpcException {
		throw new IllegalStateException("Not implemented");
	}
}
