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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for booleans.
 */
public class BooleanSerializer extends TypeSerializerImpl {
	/** Tag name of a boolean value.
	 */
	public static final String BOOLEAN_TAG = "boolean";
	private static final char[] TRUE = new char[]{'1'};
	private static final char[] FALSE = new char[]{'0'};
	public void write(ContentHandler pHandler, Object pObject) throws SAXException {
		write(pHandler, BOOLEAN_TAG, ((Boolean) pObject).booleanValue() ? TRUE : FALSE);
	}
}