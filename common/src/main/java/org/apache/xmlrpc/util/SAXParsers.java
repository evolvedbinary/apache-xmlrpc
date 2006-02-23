package org.apache.xmlrpc.util;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.xmlrpc.XmlRpcException;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/** Utility class for working with SAX parsers.
 */
public class SAXParsers {
	private static final SAXParserFactory spf;
	static {
		spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(false);
	}

	/** Creates a new instance of {@link XMLReader}.
	 */
	public static XMLReader newXMLReader() throws XmlRpcException {
		try {
			return spf.newSAXParser().getXMLReader();
		} catch (ParserConfigurationException e) {
			throw new XmlRpcException("Unable to create XML parser: " + e.getMessage(), e);
		} catch (SAXException e) {
			throw new XmlRpcException("Unable to create XML parser: " + e.getMessage(), e);
		}
	}
}
