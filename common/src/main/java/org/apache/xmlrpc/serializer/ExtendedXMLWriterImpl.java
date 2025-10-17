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

import org.apache.ws.commons.serialize.XMLWriter;
import org.apache.ws.commons.serialize.XMLWriterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExtendedXMLWriterImpl implements ExtendedXMLWriter {

    private final XMLWriter basicXmlWriter;
    private Method methodStopTerminator = null;

    public ExtendedXMLWriterImpl() {
        this.basicXmlWriter = new XMLWriterImpl();
    }

    public void setEncoding(String pEncoding) {
        this.basicXmlWriter.setEncoding(pEncoding);
    }

    public String getEncoding() {
        return this.basicXmlWriter.getEncoding();
    }

    public void setDeclarating(boolean pDeclarating) {
        this.basicXmlWriter.setDeclarating(pDeclarating);
    }

    public boolean isDeclarating() {
        return this.basicXmlWriter.isDeclarating();
    }

    public void setWriter(Writer writer) {
        this.basicXmlWriter.setWriter(writer);

    }

    public Writer getWriter() {
        return this.basicXmlWriter.getWriter();
    }

    public boolean canEncode(char pChar) {
        return this.basicXmlWriter.canEncode(pChar);
    }

    public void setIndenting(boolean pIndenting) {
        this.basicXmlWriter.setIndenting(pIndenting);
    }

    public boolean isIndenting() {
        return this.basicXmlWriter.isIndenting();
    }

    public void setIndentString(String pIndentString) {
        this.basicXmlWriter.setIndentString(pIndentString);
    }

    public String getIndentString() {
        return this.basicXmlWriter.getIndentString();
    }

    public void setLineFeed(String pLineFeed) {
        this.basicXmlWriter.setLineFeed(pLineFeed);
    }

    public String getLineFeed() {
        return this.basicXmlWriter.getLineFeed();
    }

    public void setFlushing(boolean pFlushing) {
        this.basicXmlWriter.setFlushing(pFlushing);
    }

    public boolean isFlushing() {
        return this.basicXmlWriter.isFlushing();
    }

    public void setDocumentLocator(Locator locator) {
        this.basicXmlWriter.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        this.basicXmlWriter.startDocument();
    }

    public void endDocument() throws SAXException {
        this.basicXmlWriter.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.basicXmlWriter.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        this.basicXmlWriter.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        this.basicXmlWriter.startElement(uri, localName, qName, atts);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        this.basicXmlWriter.endElement(uri, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.basicXmlWriter.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.basicXmlWriter.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.basicXmlWriter.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        this.basicXmlWriter.skippedEntity(name);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        final Writer w = this.basicXmlWriter.getWriter();
        try {
            stopTerminator();
            if (w != null) {
                w.write("<!--");
                w.write(ch, start, length);
                w.write("-->");
            }
        } catch (java.io.IOException e) {
            throw new SAXException(e);
        }
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
        final Writer w = this.basicXmlWriter.getWriter();
        try {
            stopTerminator();
            if (w != null) {
                w.write("<![CDATA[");
            }
        } catch (java.io.IOException e) {
            throw new SAXException(e);
        }
    }

    public void endCDATA() throws SAXException {
        final Writer w = this.basicXmlWriter.getWriter();
        try {
            w.write("]]>");
        } catch (java.io.IOException e) {
            throw new SAXException(e);
        }
    }

    private void stopTerminator() throws SAXException {
        try {
            if (methodStopTerminator == null) {
                methodStopTerminator = XMLWriterImpl.class.getDeclaredMethod("stopTerminator", null);
                methodStopTerminator.setAccessible(true);
            }

            methodStopTerminator.invoke(this.basicXmlWriter, null);

        } catch (final NoSuchMethodException e) {
            throw new SAXException("Unable to access stopTerminator", e);
        } catch (final IllegalAccessException e) {
            throw new SAXException("Unable to access stopTerminator", e);
        } catch (final InvocationTargetException e) {
            throw new SAXException("Unable to call stopTerminator", e);
        }
    }
}
