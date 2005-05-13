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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.XmlRpcRequestConfig;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.XmlRpcRequestParser;
import org.apache.xmlrpc.serializer.DefaultXMLWriterFactory;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.apache.xmlrpc.serializer.XmlWriterFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/** Extension of {@link XmlRpcServer} with support for reading
 * requests from a stream and writing the response to another
 * stream.
 */
public abstract class XmlRpcStreamServer extends XmlRpcServer {
	private static final SAXParserFactory spf;
	private XmlWriterFactory writerFactory = new DefaultXMLWriterFactory();
	static {
		spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(false);
	}

	protected XmlRpcRequest getRequest(final XmlRpcStreamRequestConfig pConfig,
									   InputStream pStream) throws XmlRpcException {
		final XmlRpcRequestParser parser = new XmlRpcRequestParser(pConfig, getTypeFactory());
		final XMLReader xr;
		try {
			xr = spf.newSAXParser().getXMLReader();
		} catch (ParserConfigurationException e) {
			throw new XmlRpcException("Unable to create XML parser: " + e.getMessage(), e);
		} catch (SAXException e) {
			throw new XmlRpcException("Unable to create XML parser: " + e.getMessage(), e);
		}
		xr.setContentHandler(parser);
		try {
			xr.parse(new InputSource(pStream));
		} catch (SAXException e) {
			Exception ex = e.getException();
			if (ex != null  &&  ex instanceof XmlRpcException) {
				throw (XmlRpcException) ex;
			}
			throw new XmlRpcException("Failed to parse XML-RPC request: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new XmlRpcException("Failed to read XML-RPC request: " + e.getMessage(), e);
		}
		final List params = parser.getParams();
		return new XmlRpcRequest(){
			public XmlRpcRequestConfig getConfig() { return pConfig; }
			public String getMethodName() { return parser.getMethodName(); }
			public int getParameterCount() { return params.size(); }
			public Object getParameter(int pIndex) { return params.get(pIndex); }
		};
	}

	protected XmlRpcWriter getXmlRpcWriter(XmlRpcStreamRequestConfig pConfig,
										   OutputStream pStream)
			throws XmlRpcException {
		ContentHandler w = getXMLWriterFactory().getXmlWriter(pConfig, pStream);
		return new XmlRpcWriter(pConfig, w, getTypeFactory());
	}

	protected void writeResponse(XmlRpcStreamRequestConfig pConfig, OutputStream pStream,
								 Object pResult) throws XmlRpcException {
		try {
			getXmlRpcWriter(pConfig, pStream).write(pConfig, pResult);
		} catch (SAXException e) {
			throw new XmlRpcException("Failed to write XML-RPC response: " + e.getMessage(), e);
		}
	}

	protected void writeError(XmlRpcStreamRequestConfig pConfig, OutputStream pStream,
							  Throwable pError)
			throws XmlRpcException {
		final int code;
		final String message;
		if (pError instanceof XmlRpcException) {
			XmlRpcException ex = (XmlRpcException) pError;
			code = ex.code;
		} else {
			code = 0;
		}
		message = pError.getMessage();
		try {
			pError.printStackTrace();
			getXmlRpcWriter(pConfig, pStream).write(pConfig, code, message);
		} catch (SAXException e) {
			throw new XmlRpcException("Failed to write XML-RPC response: " + e.getMessage(), e);
		}
	}

	/** Sets the XML Writer factory.
	 * @param pFactory The XML Writer factory.
	 */
	public void setXMLWriterFactory(XmlWriterFactory pFactory) {
		writerFactory = pFactory;
	}

	/** Returns the XML Writer factory.
	 * @return The XML Writer factory.
	 */
	public XmlWriterFactory getXMLWriterFactory() {
		return writerFactory;
	}

	/** Returns the connections input stream.
	 */
	protected abstract InputStream getInputStream(XmlRpcStreamRequestConfig pConfig,
												  Object pConnection) throws IOException;

	/** Returns the connections output stream.
	 */
	protected abstract OutputStream getOutputStream(XmlRpcStreamRequestConfig pConfig,
												    Object pConnection) throws IOException;

	/** Closes the connection, releasing all resources.
	 */
	protected abstract void closeConnection(Object pConnection) throws IOException;

	/** Processes a "connection". The "connection" is an opaque object, which is
	 * being handled by the subclasses.
	 * @param pConfig The request configuration.
	 * @param pConnection The "connection" being processed.
	 * @throws XmlRpcException Processing the request failed.
	 * @throws IOException An I/O error occurred.
	 */
	public void execute(XmlRpcStreamRequestConfig pConfig,
						Object pConnection)
			throws IOException, XmlRpcException {
		try {
			Object result;
			Throwable error;
			try {
				InputStream istream = getInputStream(pConfig, pConnection);
				XmlRpcRequest request = getRequest(pConfig, istream);
				result = execute(request);
				error = null;
			} catch (Throwable t) {
				result = null;
				error = t;
			}
			OutputStream ostream = getOutputStream(pConfig, pConnection);
			if (error == null) {
				writeResponse(pConfig, ostream, result);
			} else {
				writeError(pConfig, ostream, error);
			}
			closeConnection(pConnection);
			pConnection = null;
		} finally {
			if (pConnection != null) { try { closeConnection(pConnection); } catch (Throwable ignore) {} }
		}
	}
}
