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
package org.apache.xmlrpc.jaxb;

import org.apache.xmlrpc.parser.ParserHandler;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;

public class UnmarshallerParserHandler implements UnmarshallerHandler, ParserHandler {

    private UnmarshallerHandler handler;

    public UnmarshallerParserHandler(final UnmarshallerHandler handler) {
        this.handler = handler;
    }

    public void startDocument() throws SAXException {
        handler.startDocument();
    }

    public void endDocument() throws SAXException {
        handler.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        handler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        handler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        handler.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        handler.endElement(uri, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        handler.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        handler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        handler.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        handler.setDocumentLocator(locator);
    }

    public void skippedEntity(String name) throws SAXException {
        handler.skippedEntity(name);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void startEntity(String name) throws SAXException {
    }

    public void endEntity(String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public Object getResult() throws JAXBException, IllegalStateException {
        return handler.getResult();
    }
}
