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
package org.apache.xmlrpc.serializer;

import org.apache.ws.commons.serialize.DOMSerializer;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/** The node serializer is serializing a DOM node.
 */
public class NodeSerializer extends ExtSerializer {
	private static final DOMSerializer ser = new ExtendedDOMSerializer();
	static {
		ser.setStartingDocument(false);
	}

	/** The local name of a dom tag.
	 */
	public static final String DOM_TAG = "dom";

    public static final String NS_DOM = XmlRpcWriter.EXTENSIONS_URI + "/" + DOM_TAG;
    public static final String PREFIX_DOM = DOM_TAG;
    public static final String TYPE_ATTR_NAME = "type";

	protected String getTagName() { return DOM_TAG; }

    public void write(SerializerHandler pHandler, Object pObject)
        throws SAXException {
        final String tag = getTagName();
        final String exTag = "ex:" + getTagName();

        pHandler.startPrefixMapping(PREFIX_DOM, NS_DOM);

        pHandler.startElement("", TypeSerializerImpl.VALUE_TAG, TypeSerializerImpl.VALUE_TAG, TypeSerializerImpl.ZERO_ATTRIBUTES);

        final AttributesImpl attrs = new AttributesImpl();
        attrs.addAttribute(NS_DOM, TYPE_ATTR_NAME, PREFIX_DOM + ":" + TYPE_ATTR_NAME, "PCDATA", Short.toString(((Node) pObject).getNodeType()));
        pHandler.startElement(XmlRpcWriter.EXTENSIONS_URI, tag, exTag, attrs);
        serialize(pHandler, pObject);
        pHandler.endElement(XmlRpcWriter.EXTENSIONS_URI, tag, exTag);

        pHandler.endElement("", TypeSerializerImpl.VALUE_TAG, TypeSerializerImpl.VALUE_TAG);

        pHandler.endPrefixMapping(PREFIX_DOM);
    }

	protected void serialize(SerializerHandler pHandler, Object pObject) throws SAXException {
		ser.serialize((Node) pObject, pHandler);
	}
}
