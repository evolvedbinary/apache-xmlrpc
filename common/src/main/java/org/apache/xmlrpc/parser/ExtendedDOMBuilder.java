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

import org.apache.ws.commons.serialize.DOMBuilder;
import org.apache.xmlrpc.serializer.NodeSerializer;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.lang.reflect.Field;

public class ExtendedDOMBuilder extends DOMBuilder implements ParserHandler {

    private static final String STUB_ELEM_NAME = "stub-element";

    private final boolean wrapInElement;
    private Field fldCurrentNode = null;
    private CDATASection cdataSection = null;

    public ExtendedDOMBuilder() {
        this(false);
    }

    public ExtendedDOMBuilder(final boolean wrapInElement) {
        super();
        this.wrapInElement = wrapInElement;
    }

    public void startDocument() throws SAXException {
        super.startDocument();
        if (wrapInElement) {
            startElement(NodeSerializer.NS_DOM, STUB_ELEM_NAME, NodeSerializer.PREFIX_DOM + ':' + STUB_ELEM_NAME, TypeParserImpl.ZERO_ATTRIBUTES);
        }
    }

    public void endDocument() throws SAXException {
        if (wrapInElement) {
            endElement(NodeSerializer.NS_DOM, STUB_ELEM_NAME, NodeSerializer.PREFIX_DOM + ':' + STUB_ELEM_NAME);
        }
        super.endDocument();
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        if (cdataSection != null) {
            cdataSection.appendData(new String(ch, start, length));
        } else {
            super.characters(ch, start, length);
        }
    }

    public void startCDATA() throws SAXException {
        this.cdataSection = getDocument().createCDATASection("");
    }

    public void endCDATA() throws SAXException {
        getCurrentNode().appendChild(cdataSection);
        this.cdataSection = null;
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        Comment comment = getDocument().createComment(new String(ch, start, length));
        getCurrentNode().appendChild(comment);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void endDTD() throws SAXException {

    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {

    }

    private Node getCurrentNode() throws SAXException {
        try {
            if (fldCurrentNode == null) {
                this.fldCurrentNode = DOMBuilder.class.getDeclaredField("currentNode");
                this.fldCurrentNode.setAccessible(true);
            }

            return (Node) fldCurrentNode.get(this);

        } catch (final NoSuchFieldException e) {
            throw new SAXException("Unable to access currentNode", e);
        } catch (final IllegalAccessException e) {
            throw new SAXException("Unable to access currentNode", e);
        }
    }
}
