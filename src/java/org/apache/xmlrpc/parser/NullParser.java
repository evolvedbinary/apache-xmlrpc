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
package org.apache.xmlrpc.parser;

import javax.xml.namespace.QName;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/** SAX parser for a nil element (null value).
 */
public class NullParser extends TypeParserImpl {
	public void endElement(String pURI, String pLocalName, String pQName) throws SAXException {
		throw new SAXParseException("Unexpected end tag within nil: "
									+ new QName(pURI, pLocalName),
									getDocumentLocator());
	}

	public void startElement(String pURI, String pLocalName, String pQName, Attributes pAttrs) throws SAXException {
		throw new SAXParseException("Unexpected start tag within nil: "
									+ new QName(pURI, pLocalName),
									getDocumentLocator());
	}
}
