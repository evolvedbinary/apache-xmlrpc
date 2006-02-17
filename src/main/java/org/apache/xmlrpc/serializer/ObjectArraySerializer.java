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

import org.apache.xmlrpc.common.TypeFactory;
import org.apache.xmlrpc.common.XmlRpcStreamConfig;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A {@link TypeSerializer} for object arrays.
 */
public class ObjectArraySerializer extends TypeSerializerImpl {
	/** Tag name of an array value.
	 */
	public static final String ARRAY_TAG = "array";
	/** Tag name of an arrays data.
	 */
	public static final String DATA_TAG = "data";

	private final XmlRpcStreamConfig config;
	private final TypeFactory typeFactory;

	/** Creates a new instance.
	 * @param pTypeFactory The factory being used for creating serializers.
	 * @param pConfig The configuration being used for creating serializers.
	 */
	public ObjectArraySerializer(TypeFactory pTypeFactory, XmlRpcStreamConfig pConfig) {
		typeFactory = pTypeFactory;
		config = pConfig;
	}
	protected void writeObject(ContentHandler pHandler, Object pObject) throws SAXException {
		TypeSerializer ts = typeFactory.getSerializer(config, pObject);
		if (ts == null) {
			throw new SAXException("Unsupported Java type: " + pObject.getClass().getName());
		}
		ts.write(pHandler, pObject);
	}
	protected void writeData(ContentHandler pHandler, Object pObject) throws SAXException {
		Object[] data = (Object[]) pObject;
		for (int i = 0;  i < data.length;  i++) {
			writeObject(pHandler, data[i]);
		}
	}
	public void write(final ContentHandler pHandler, Object pObject) throws SAXException {
		pHandler.startElement("", VALUE_TAG, VALUE_TAG, ZERO_ATTRIBUTES);
		pHandler.startElement("", ARRAY_TAG, ARRAY_TAG, ZERO_ATTRIBUTES);
		pHandler.startElement("", DATA_TAG, DATA_TAG, ZERO_ATTRIBUTES);
		writeData(pHandler, pObject);
		pHandler.endElement("", DATA_TAG, DATA_TAG);
		pHandler.endElement("", ARRAY_TAG, ARRAY_TAG);
		pHandler.endElement("", VALUE_TAG, VALUE_TAG);
	}
}