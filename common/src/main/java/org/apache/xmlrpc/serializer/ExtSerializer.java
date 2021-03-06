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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** Base class for external XML representations, like DOM,
 * or JAXB.
 */
public abstract class ExtSerializer implements TypeSerializer {
	/** Returns the unqualified tag name.
	 * @return the unqualified tag name
	 */
	protected abstract String getTagName();
	/** Performs the actual serialization.
	 * @param pHandler the content handler
	 * @param pObject the object to serialize
	 * @throws SAXException if an error occurs during serialization
	 */
	protected abstract void serialize(ContentHandler pHandler, Object pObject) throws SAXException;

	public void write(ContentHandler pHandler, Object pObject)
			throws SAXException {
		final String tag = getTagName();
		final String exTag = "ex:" + getTagName();
		pHandler.startElement("", TypeSerializerImpl.VALUE_TAG, TypeSerializerImpl.VALUE_TAG, TypeSerializerImpl.ZERO_ATTRIBUTES); 
		pHandler.startElement(XmlRpcWriter.EXTENSIONS_URI, tag, exTag, TypeSerializerImpl.ZERO_ATTRIBUTES);
		serialize(pHandler, pObject);
		pHandler.endElement(XmlRpcWriter.EXTENSIONS_URI, tag, exTag);
		pHandler.endElement("", TypeSerializerImpl.VALUE_TAG, TypeSerializerImpl.VALUE_TAG); 
	}
}
