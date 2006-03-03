/*
 * Copyright 1999,2006 The Apache Software Foundation.
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
package org.apache.xmlrpc.metadata;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;


/** Utility class, which provides services to meta data
 * handlers and handler mappings.
 */
public class Util {
	/** This field should solve the problem, that we do not
	 * want to depend on the presence of JAXB. However, if
	 * it is available, we want to support it.
	 */
	private static final Class jaxbElementClass;
	static {
		Class c;
		try {
			c = Class.forName("javax.xml.bind.Element");
		} catch (ClassNotFoundException e) {
			c = null;
		}
		jaxbElementClass = c;
	}
	
	/** Returns a signature for the given return type or
	 * parameter class.
	 * @param pType The class for which a signature is being
	 * queried.
	 * @return Signature, if known, or null.
	 */
	public static String getSignatureType(Class type) {
		if (type == Integer.TYPE || type == Integer.class)
			return "int";
		if (type == Double.TYPE || type == Double.class)
			return "double";
		if (type == Boolean.TYPE || type == Boolean.class)
			return "boolean";
		if (type == String.class)
			return "string";
		if (Object[].class.isAssignableFrom(type)
			||  List.class.isAssignableFrom(type))
			return "array";
		if (Map.class.isAssignableFrom(type))
			return "struct";
		if (Date.class.isAssignableFrom(type)
			||  Calendar.class.isAssignableFrom(type))
			return "dateTime.iso8601";
		if (type == byte[].class)
			return "base64";

		// extension types
		if (type == void.class)
			return "ex:nil";
		if (type == Byte.TYPE || type == Byte.class)
			return "ex:i1";
		if (type == Short.TYPE || type == Short.class)
			return "ex:i2";
		if (type == Long.TYPE || type == Long.class)
			return "ex:i8";
		if (type == Float.TYPE || type == Float.class)
			return "ex:float";
		if (Node.class.isAssignableFrom(type))
			return "ex:node";
		if (jaxbElementClass != null
			&&  jaxbElementClass.isAssignableFrom(type)) {
			return "ex:jaxbElement";
		}
		if (Serializable.class.isAssignableFrom(type))
			return "base64";

		// give up
		return null;
	}

	/** Returns a signature for the given method.
	 * @param pMethod Method, for which a signature is
	 * being queried.
	 * @return Signature string, or null, if no signature
	 * is available.
	 */
	public static String[] getSignature(Method pMethod) {
		Class[] paramClasses = pMethod.getParameterTypes();
		String[] sig = new String[paramClasses.length + 1];
		String s = getSignatureType(pMethod.getReturnType());
		if (s == null) {
			return null;
		}
		sig[0] = s;
		for (int i = 0;  i < paramClasses.length;  i++) {
			s = getSignatureType(paramClasses[i]);
			if (s == null) {
				return null;
			}
			sig[i+1] = s;
		}
		return sig;
	}

	/** Returns a help string for the given method, which
	 * is applied to the given class.
	 */
	public static String getMethodHelp(Class pClass, Method pMethod) {
		StringBuffer sb = new StringBuffer();
		sb.append("Invokes the method ");
		sb.append(pMethod.getReturnType().getClass().getName());
		sb.append(".");
		sb.append(pClass.getName());
		sb.append("(");
		Class[] paramClasses = pMethod.getParameterTypes();
		for (int i = 0;  i < paramClasses.length;  i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(paramClasses[i].getName());
		}
		sb.append(").");
		return sb.toString();
	}
}
