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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class ExtendedDOMSerializer extends DOMSerializer {

    private static final String ATTRIBUTE_ELEM_NAME = "attribute";

    /**
     * Overridden so that we can serialize a raw XML Attribute to {@code <dom:attribute attr-name="attr-value"/>}.
     *
     * @param pNode The node being serialized.
     * @param pHandler The target handler.
     * @throws SAXException The target handler reported an error.
     */
    protected void doSerialize(Node pNode, ContentHandler pHandler) throws SAXException {
        if (pNode.getNodeType() == Node.ATTRIBUTE_NODE) {
            final AttributesImpl attrs = new AttributesImpl();

            if (pNode.getNamespaceURI() != null && pNode.getPrefix() != null) {
                pHandler.startPrefixMapping(pNode.getPrefix(), pNode.getNamespaceURI());
            }

            attrs.addAttribute(pNode.getNamespaceURI(), pNode.getLocalName(), pNode.getNodeName(), "PCDATA",pNode.getTextContent());
            pHandler.startElement(NodeSerializer.NS_DOM, ATTRIBUTE_ELEM_NAME, NodeSerializer.PREFIX_DOM + ':' + ATTRIBUTE_ELEM_NAME, attrs);
            pHandler.endElement(NodeSerializer.NS_DOM, ATTRIBUTE_ELEM_NAME, NodeSerializer.PREFIX_DOM + ':' + ATTRIBUTE_ELEM_NAME);

            if (pNode.getNamespaceURI() != null && pNode.getPrefix() != null) {
                pHandler.endPrefixMapping(pNode.getPrefix());
            }

        } else {
            super.doSerialize(pNode, pHandler);
        }
    }
}
