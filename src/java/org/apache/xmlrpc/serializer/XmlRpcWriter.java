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
package org.apache.xmlrpc.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/** This class is responsible for writing an XmlRpc request or an
 * XmlRpc response to an output stream.
 */
public class XmlRpcWriter {
	/** The namespace URI for proprietary XML-RPC extensions.
	 */
	public static final String EXTENSIONS_URI = "http://ws.apache.org/xmlrpc/namespaces/extensions";
	private static final Attributes ZERO_ATTRIBUTES = new AttributesImpl();
	private final XmlRpcStreamConfig config;
	private final TypeFactory typeFactory;
	private final ContentHandler handler;

	/** Creates a new instance.
	 * @param pConfig The clients configuration.
	 * @param pHandler The target SAX handler.
	 * @param pTypeFactory The type factory being used to create serializers.
	 */
	public XmlRpcWriter(XmlRpcStreamConfig pConfig, ContentHandler pHandler,
					    TypeFactory pTypeFactory) {
		config = pConfig;
		handler = pHandler;
		typeFactory = pTypeFactory;
	}

	/** Writes a clients request to the output stream.
	 * @param pRequest The request being written.
	 * @throws SAXException Writing the request failed.
	 */
	public void write(XmlRpcRequest pRequest) throws SAXException {
		handler.startElement("", "methodCall", "methodCall", ZERO_ATTRIBUTES);
		handler.startElement("", "methodName", "methodName", ZERO_ATTRIBUTES);
		String s = pRequest.getMethodName();
		handler.characters(s.toCharArray(), 0, s.length());
		handler.endElement("", "methodName", "methodName");
		handler.startElement("", "params", "params", ZERO_ATTRIBUTES);
		int num = pRequest.getParameterCount();
		for (int i = 0;  i < num;  i++) {
			handler.startElement("", "param", "param", ZERO_ATTRIBUTES);
			writeValue(pRequest.getParameter(i));
			handler.endElement("", "param", "param");
		}
		handler.endElement("", "params", "params");
        handler.endElement("", "methodCall", "methodCall");
	}

	/** Writes a servers response to the output stream.
	 * @param pResult The result object.
	 * @throws SAXException Writing the response failed.
	 */
	public void write(Object pResult) throws SAXException {
		handler.startElement("", "methodResponse", "methodResponse", ZERO_ATTRIBUTES);
		handler.startElement("", "params", "params", ZERO_ATTRIBUTES);
		handler.startElement("", "param", "param", ZERO_ATTRIBUTES);
		writeValue(pResult);
		handler.endElement("", "param", "param");
		handler.endElement("", "params", "params");
		handler.endElement("", "methodResponse", "methodResponse");
	}

	/** Writes a servers error message to the output stream.
	 * @param pCode The error code
	 * @param pMessage The error message
	 * @throws SAXException Writing the error message failed.
	 */
	public void write(int pCode, String pMessage) throws SAXException {
		handler.startElement("", "methodResponse", "methodResponse", ZERO_ATTRIBUTES);
		handler.startElement("", "fault", "fault", ZERO_ATTRIBUTES);
		Map map = new HashMap();
        map.put("faultCode", new Integer(pCode));
        map.put("faultString", pMessage);
		writeValue(map);
		handler.endElement("", "fault", "fault");
		handler.endElement("", "methodResponse", "methodResponse");
	}

	/** Writes the XML representation of a Java object.
	 * @param pObject The object being written.
	 * @throws SAXException Writing the object failed.
	 */
	protected void writeValue(Object pObject) throws SAXException {
		TypeSerializer serializer = typeFactory.getSerializer(config, pObject);
		serializer.write(handler, pObject);
	}
}
