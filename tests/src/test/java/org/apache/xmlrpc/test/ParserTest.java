package org.apache.xmlrpc.test;

import java.io.IOException;
import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.parser.XmlRpcResponseParser;
import org.apache.xmlrpc.util.SAXParsers;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


/** Test for the various parsers.
 */
public class ParserTest extends TestCase {
	private Object parseResponse(final String s) throws XmlRpcException, IOException, SAXException, SAXParseException {
		XmlRpcStreamRequestConfig config = new XmlRpcClientConfigImpl();
		XmlRpcClient client = new XmlRpcClient();
		XmlRpcResponseParser parser = new XmlRpcResponseParser(config, client.getTypeFactory());
		XMLReader xr = SAXParsers.newXMLReader();
		xr.setContentHandler(parser);
		try {
			xr.parse(new InputSource(new StringReader(s)));
		} catch (SAXParseException e) {
			throw e;
		}
		Object o = parser.getResult();
		return o;
	}

	/** Tests, whether strings can be parsed with,
	 * or without, the "string" tag.
	 */
	public void testStringType() throws Exception {
		final String[] strings = new String[]{
			"3", "<string>3</string>",
			"  <string>3</string>  "
		};
		for (int i = 0;  i < strings.length;  i++) {
			final String s =
				"<?xml version='1.0' encoding='UTF-8'?>\n"
				+ "<methodResponse><params><param>\n"
				+ "<value>" + strings[i] + "</value></param>\n"
				+ "</params></methodResponse>\n";
			Object o = parseResponse(s);
			assertEquals("3", o);
		}
	}

	/** Tests, whether nested arrays can be parsed.
	 */
	public void testNestedObjectArrays() throws Exception {
		final String s =
			"<?xml version='1.0' encoding='UTF-8'?>\n"
			+ "<methodResponse><params><param>\n"
			+ "<value><array><data><value><array>\n"
			+ "<data><value>array</value>\n"
			+ "<value>string</value></data></array>\n"
			+ "</value></data></array></value></param>\n"
			+ "</params></methodResponse>\n";
		Object o = parseResponse(s);
		assertTrue(o instanceof Object[]);
		Object[] outer = (Object[]) o;
		assertEquals(1, outer.length);
		o = outer[0];
		assertTrue(o instanceof Object[]);
		Object[] inner = (Object[]) o;
		assertEquals(2, inner.length);
		assertEquals("array", inner[0]);
		assertEquals("string", inner[1]);
	}
}
