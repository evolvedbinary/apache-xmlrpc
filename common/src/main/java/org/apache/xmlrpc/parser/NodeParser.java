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
package org.apache.xmlrpc.parser;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlrpc.serializer.NodeSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/** A parser for DOM document.
 */
public class NodeParser extends ExtParser {
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private ExtendedDOMBuilder builder = null;
    private short domType = Node.DOCUMENT_TYPE_NODE;

	protected String getTagName() {
		return NodeSerializer.DOM_TAG;
	}

	protected ParserHandler getExtHandler(Attributes attrs) throws SAXException {
        final String domTypeStr = attrs.getValue(NodeSerializer.NS_DOM, NodeSerializer.TYPE_ATTR_NAME);
        if (domTypeStr.length() > 0) {
            try {
                domType = Short.parseShort(domTypeStr);
            } catch (final NumberFormatException e) {
                // no-op
            }
        }

         builder = new ExtendedDOMBuilder(domType == Node.TEXT_NODE);

		try {
			builder.setTarget(dbf.newDocumentBuilder().newDocument());
		} catch (ParserConfigurationException e) {
			throw new SAXException(e);
		}
		return builder;
	}

    public Object getResult() {
        final Document document = (Document) builder.getTarget();

        switch (domType) {
            case Node.DOCUMENT_NODE:
                return document;

            case Node.TEXT_NODE:
                return document.getDocumentElement().getFirstChild();

            case Node.ATTRIBUTE_NODE:
                // NOTE(AR) we should be expecting a <dom:attribute attr-name="attr-value"/> element, see: {@link ExtendedDOMSerializer#doSerialize(Node, ContentHandler)}
                final Element domAttrElement = document.getDocumentElement();
                final NamedNodeMap attributes = domAttrElement.getAttributes();
                return attributes.item(0);

            default:
                return document.getFirstChild();
        }
	}
}
