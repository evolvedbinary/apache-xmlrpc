package org.apache.xmlrpc.parser;

import org.xml.sax.SAXException;


/** Parser implementation for parsing a string.
 */
public class StringParser extends AtomicParser {
	protected void setResult(String pResult) throws SAXException {
		super.setResult((Object) pResult);
	}

}
