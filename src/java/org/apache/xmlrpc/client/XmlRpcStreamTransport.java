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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.XmlRpcResponseParser;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/** Implementation of a transport class, which is based on an output
 * stream for sending the request and an input stream for receiving
 * the response,
 */
public abstract class XmlRpcStreamTransport extends XmlRpcTransportImpl {
	private final XmlRpcTransportFactory factory;
	private static final SAXParserFactory spf;
	static {
		spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(false);
	}

	/** Creates a new instance on behalf of the given client.
	 */
	protected XmlRpcStreamTransport(XmlRpcClient pClient, XmlRpcTransportFactory pFactory) {
		super(pClient);
		factory = pFactory;
	}

	/** Returns the factory, which created this transport.
	 * @return The transport factory.
	 */
	public XmlRpcTransportFactory getFactory() {
		return factory;
	}

	/** Creates the connection object. The connection object is a
	 * factory for output and input stream.
	 */
	protected abstract Object newConnection(XmlRpcStreamRequestConfig pConfig) throws XmlRpcClientException;

	/** Closes the connection object.
	 */
	protected abstract void closeConnection(Object pConnection) throws XmlRpcClientException;

	/** Initializes the newly created connection. For example, the HTTP transport
	 * will use this to set headers.
	 * @param pConfig The clients configuration.
	 * @param pConnection The connection being initialized.
	 * @throws XmlRpcClientException A local error on the client occurred.
	 */
	protected void initConnection(XmlRpcStreamRequestConfig pConfig, Object pConnection) throws XmlRpcClientException {
	}

	/** Creates a new output stream, to which the request may be written.
	 * @param pConfig Client configuration.
	 * @param pConnection Connection being used to send request data.
	 * @return Opened output stream.
	 * @throws XmlRpcClientException An error occurred on the client.
	 */
	protected abstract OutputStream newOutputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection)
			throws XmlRpcClientException;

	/** Closes the opened output stream, indicating that no more data is being
	 * sent.
	 * @param pStream The stream being closed.
	 * @throws XmlRpcClientException An error occurred on the client.
	 */
	protected void closeOutputStream(OutputStream pStream) throws XmlRpcClientException {
		try {
			pStream.close();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to close output stream.", e);
		}
	}

	protected OutputStream getOutputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection)
			throws XmlRpcClientException {
		OutputStream result = newOutputStream(pConfig, pConnection);
		if (pConfig.isGzipCompressing()) {
			try {
				result = new GZIPOutputStream(result);
			} catch (IOException e) {
				throw new XmlRpcClientException("Failed to attach gzip encoding to output stream", e);
			}
		}
		return result;
	}

	/** Creates a new input stream for reading the response.
	 * @param pConfig The clients configuration.
	 * @param pConnection The connection object.
	 * @return Opened input stream for reading data.
	 * @throws XmlRpcException Creating the input stream failed.
	 */
	protected abstract InputStream newInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection)
			throws XmlRpcException;

	/** Closes the opened input stream, indicating that no more data is being
	 * read.
	 * @param pStream The stream being closed.
	 * @throws XmlRpcClientException An error occurred on the client.
	 */
	protected void closeInputStream(InputStream pStream) throws XmlRpcClientException {
		try {
			pStream.close();
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to close output stream.", e);
		}
	}

	/** Returns, whether the response is gzip compressed.
	 * @param pConfig The clients configuration.
	 * @param pConnection The connection object.
	 * @return Whether the response stream is gzip compressed.
	 */
	protected abstract boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig, Object pConnection);

	protected InputStream getInputStream(XmlRpcStreamRequestConfig pConfig, Object pConnection)
			throws XmlRpcException {
		InputStream istream = newInputStream(pConfig, pConnection);
		if (isResponseGzipCompressed(pConfig, pConnection)) {
			try {
				istream = new GZIPInputStream(istream);
			} catch (IOException e) {
				throw new XmlRpcClientException("Failed to attach gzip decompression to the response stream", e);
			}
		}
		return istream;
	}

	public Object sendRequest(XmlRpcRequest pRequest) throws XmlRpcException {
		XmlRpcStreamRequestConfig config = (XmlRpcStreamRequestConfig) pRequest.getConfig();
		Object connection = newConnection(config);
		try {
			initConnection(config, pRequest);
			OutputStream ostream = getOutputStream(config, connection);
			try {
				writeRequest(config, ostream, pRequest);
				closeOutputStream(ostream);
				ostream = null;
			} finally {
				if (ostream != null) { try { closeOutputStream(ostream); } catch (Throwable ignore) {} }
			}
			InputStream istream = getInputStream(config, connection);
			Object result;
			try {
				result = readResponse(config, istream);
				closeInputStream(istream);
				istream = null;
			} finally {
				if (istream != null) { try { closeInputStream(istream); } catch (Throwable ignore) {} }
			}
			closeConnection(connection);
			connection = null;
			return result;
		} finally {
			if (connection != null) { try { closeConnection(connection); } catch (Throwable ignore) {} }
		}
	}

	protected XMLReader newXMLReader() throws XmlRpcClientException {
		try {
			return spf.newSAXParser().getXMLReader();
		} catch (ParserConfigurationException e) {
			throw new XmlRpcClientException("Failed to create XMLReader: " + e.getMessage(), e);
		} catch (SAXException e) {
			throw new XmlRpcClientException("Failed to create XMLReader: " + e.getMessage(), e);
		}
	}

	protected Object readResponse(XmlRpcStreamRequestConfig pConfig, InputStream pStream) throws XmlRpcException {
		InputSource isource = new InputSource(pStream);
		XMLReader xr = newXMLReader();
		try {
			XmlRpcResponseParser xp = new XmlRpcResponseParser(pConfig, getClient().getTypeFactory());
			xr.setContentHandler(xp);
			xr.parse(isource);
			return xp.getResult();
		} catch (SAXException e) {
			throw new XmlRpcClientException("Failed to parse servers response: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new XmlRpcClientException("Failed to read servers response: " + e.getMessage(), e);
		}
	}

	protected void writeRequest(XmlRpcStreamRequestConfig pConfig, OutputStream pStream, XmlRpcRequest pRequest)
			throws XmlRpcException {
		ContentHandler h = getClient().getXmlWriterFactory().getXmlWriter(pConfig, pStream);
		XmlRpcWriter xw = new XmlRpcWriter(pConfig, h, getClient().getTypeFactory());
		try {
			xw.write(pRequest);
		} catch (SAXException e) {
			throw new XmlRpcClientException("Failed to send request: " + e.getMessage(), e);
		}
	}
}
