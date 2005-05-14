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

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.ws.commons.serialize.XMLWriterImpl;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientRequestImpl;
import org.apache.xmlrpc.client.XmlRpcSunHttpTransportFactory;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.serializer.XmlRpcWriter;
import org.apache.xmlrpc.util.Base64;
import org.xml.sax.SAXException;

import junit.framework.TestCase;


/** A test case for the various serializers.
 */
public class SerializerTest extends TestCase {
	private final XmlRpcClient client;

	/** Creates a new instance.
	 */
	public SerializerTest() {
		client = new XmlRpcClient();
		client.setTransportFactory(new XmlRpcSunHttpTransportFactory(client));
	}

	protected XmlRpcClientConfigImpl getConfig() {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		return config;
	}

	protected XmlRpcStreamRequestConfig getExConfig() {
		XmlRpcClientConfigImpl config = getConfig();
		config.setEnabledForExtensions(true);
		return config;
	}

	protected String writeRequest(XmlRpcStreamRequestConfig pConfig, XmlRpcRequest pRequest)
			throws XmlRpcException, SAXException {
		StringWriter sw = new StringWriter();
		XMLWriter xw = new XMLWriterImpl();
		xw.setEncoding("US-ASCII");
		xw.setDeclarating(true);
		xw.setIndenting(false);
		xw.setWriter(sw);
		XmlRpcWriter xrw = new XmlRpcWriter(pConfig, xw, client.getTypeFactory());
		xrw.write(pRequest);
		return sw.toString();
	}

	/** Test for the base64 decoder/encoder.
	 * @throws Exception The test failed.
	 */
	public void testBase64() throws Exception {
		for (int i = 0;  i <= 256;  i++) {
			byte[] bytes = new byte[i];
			for (int j = 0;  j < i;  j++) {
				bytes[j] = (byte) j;
			}
			char[] chars = Base64.encode(bytes);
			byte[] result = Base64.decode(chars);
			assertTrue(Arrays.equals(bytes, result));
		}
	}

	/** Test serialization of a byte parameter.
	 * @throws Exception The test failed.
	 */
	public void testByteParam() throws Exception {
		XmlRpcStreamRequestConfig config = getExConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "byteParam", new Object[]{new Byte((byte)3)});
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall xmlns:ex=\"http://ws.apache.org/xmlrpc/namespaces/extensions\">"
			+ "<methodName>byteParam</methodName><params><param><value><ex:i1>3</ex:i1></value></param></params></methodCall>";
		assertEquals(expect, got);
	}

	/** Test serialization of an integer parameter.
	 * @throws Exception The test failed.
	 */
	public void testIntParam() throws Exception {
		XmlRpcStreamRequestConfig config = getConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "intParam", new Object[]{new Integer(3)});
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall>"
			+ "<methodName>intParam</methodName><params><param><value><int>3</int></value></param></params></methodCall>";
		assertEquals(expect, got);
	}

	/** Test serialization of a byte array.
	 * @throws Exception The test failed.
	 */
	public void testByteArrayParam() throws Exception {
		byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		XmlRpcStreamRequestConfig config = getConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "byteArrayParam", new Object[]{bytes});
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall>"
			+ "<methodName>byteArrayParam</methodName><params><param><value><base64>AAECAwQFBgcICQ==</base64></value></param></params></methodCall>";
		assertEquals(expect, got);
	}

	/** Test serialization of a map.
	 * @throws Exception The test failed.
	 */
	public void testMapParam() throws Exception {
		final Map map = new HashMap();
		map.put("2", new Integer(3));
		map.put("3", new Integer(5));
		final Object[] params = new Object[]{map};
		XmlRpcStreamRequestConfig config = getConfig();
		XmlRpcRequest request = new XmlRpcClientRequestImpl(config, "mapParam", params);
		String got = writeRequest(config, request);
		String expect =
			"<?xml version=\"1.0\" encoding=\"US-ASCII\"?>"
			+ "<methodCall><methodName>mapParam</methodName>"
			+ "<params><param><value><struct>"
			+ "<member><name>3</name><value><int>5</int></value></member>"
			+ "<member><name>2</name><value><int>3</int></value></member>"
			+ "</struct></value></param></params></methodCall>";
		assertEquals(expect, got);
	}
}