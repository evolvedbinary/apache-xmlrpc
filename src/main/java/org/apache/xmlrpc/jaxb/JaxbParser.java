package org.apache.xmlrpc.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.UnmarshallerHandler;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.parser.ExtParser;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A parser for JAXB objects.
 */
public class JaxbParser extends ExtParser {
	private final JAXBContext context;
	private UnmarshallerHandler handler;

	/** Creates a new instance with the given context.
	 * @param pContext The context being used for creating unmarshallers.
	 */
	public JaxbParser(JAXBContext pContext) {
		context = pContext;
	}

	protected ContentHandler getExtHandler() throws SAXException {
		try {
			handler = context.createUnmarshaller().getUnmarshallerHandler();
		} catch (JAXBException e) {
			throw new SAXException(e);
		}
		return handler;
	}

	protected String getTagName() { return JaxbSerializer.JAXB_TAG; }

	public Object getResult() throws XmlRpcException {
		try {
			return handler.getResult();
		} catch (JAXBException e) {
			throw new XmlRpcException("Failed to create result object: " + e.getMessage(), e);
		}
	}
}
