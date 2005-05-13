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
package org.apache.xmlrpc.test;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfig;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcTransportFactory;
import org.apache.xmlrpc.common.XmlRpcExtensionException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;

import junit.framework.TestCase;


/** An abstract test case, to be implemented for the various
 * transport classes.
 */
public abstract class BaseTestCase extends TestCase {
	protected abstract XmlRpcTransportFactory getTransportFactory(XmlRpcClient pClient);

	/** The remote class being invoked by the test case.
	 */
	public static class Remote {
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int byteParam(byte pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public byte byteResult(byte pArg) { return (byte) (pArg*2); }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int shortParam(short pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public short shortResult(short pArg) { return (short) (pArg*2); }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int intParam(int pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public int longParam(long pArg) { return (int) (pArg*2); }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public long longResult(long pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public double floatParam(float pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public float floatResult(float pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public double doubleParam(double pArg) { return pArg*2; }
		/** Returns the argument, multiplied by two.
		 * @param pArg The argument being doubled.
		 * @return The argument, multiplied by two.
		 */
		public double doubleResult(double pArg) { return pArg*2; }
		/** Returns the argument, concatenated with itself.
		 * @param pArg The argument being concatenated.
		 * @return The argument, concatenated with itself.
		 */
		public String stringParam(String pArg) { return pArg+pArg; }
		/** Returns the argument, concatenated with itself.
		 * @param pArg The argument being concatenated.
		 * @return The argument, concatenated with itself.
		 */
		public String nullableStringParam(String pArg) {
			if (pArg == null) {
				pArg = "";
			}
			return pArg+pArg;
		}
		/** Returns the argument, concatenated with itself.
		 * @param pArg The argument being concatenated.
		 * @return The argument, concatenated with itself.
		 */
		public String nullableStringResult(String pArg) {
			if (pArg == null) {
				return null;
			}
			return pArg+pArg;
		}
		/** Returns the sum of the bytes in the given byte array.
		 * @param pArg The array of bytes being added.
		 * @return Sum over the bytes in the array.
		 */
		public int byteArrayParam(byte[] pArg) {
			int sum = 0;
			for (int i = 0;  i < pArg.length;  i++) {
				sum += pArg[i];
			}
			return sum;
		}
		/** Returns an array with the bytes 0..pArg.
		 * @param pArg Requestes byte array length.
		 * @return Byte array with 0..pArg.
		 */
		public byte[] byteArrayResult(int pArg) {
			byte[] result = new byte[pArg];
			for (int i = 0;  i < result.length;  i++) {
				result[i] = (byte) i;
			}
			return result;
		}
		/** Returns the sum over the objects in the array.
		 * @param pArg Object array being added
		 * @return Sum over the objects in the array
		 */
		public int objectArrayParam(Object[] pArg) {
			int sum = 0;
			for (int i = 0;  i < pArg.length;  i++) {
				if (pArg[i] instanceof Number) {
					sum += ((Number) pArg[i]).intValue();
				} else {
					sum += Integer.parseInt((String) pArg[i]);
				}
			}
			return sum;
		}
		/** Returns an array of integers with the values
		 * 0..pArg.
		 * @param pArg Requested array length.
		 * @return Array of integers with the values 0..pArg
		 */
		public Object[] objectArrayResult(int pArg) {
			Object[] result = new Object[pArg];
			for (int i = 0;  i < result.length;  i++) {
				result[i] = new Integer(i);
			}
			return result;
		}
		/** Returns a sum over the entries in the map. Each
		 * key is multiplied with its value.
		 * @param pArg The map being iterated.
		 * @return Sum of keys, multiplied by their values.
		 */
		public int mapParam(Map pArg) {
			int sum = 0;
			for (Iterator iter = pArg.entrySet().iterator();  iter.hasNext();  ) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				Integer value = (Integer) entry.getValue();
				sum += Integer.parseInt(key) * value.intValue();
			}
			return sum;
		}
		/** Returns a map with the stringified values 0..pArg as
		 * keys and the corresponding integers as values.
		 * @param pArg Requested map size.
		 * @return Map with the keys "0".."pArg" and
		 * 0..pArg as values.
		 */
		public Map mapResult(int pArg) {
			Map result = new HashMap();
			for (int i = 0;  i < pArg;  i++) {
				result.put(Integer.toString(i), new Integer(i));
			}
			return result;
		}
	}

	protected XmlRpcClient getClient() {
		XmlRpcClient client = new XmlRpcClient();
		client.setTransportFactory(getTransportFactory(client));
		return client;
	}

	protected XmlRpcClientConfigImpl getConfig() throws Exception {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		return config;
	}

	protected XmlRpcHandlerMapping getHandlerMapping() throws IOException, XmlRpcException {
		return new PropertyHandlerMapping(getClass().getClassLoader(),
										  getClass().getResource("BaseTestCase.properties"));
	}

	protected XmlRpcServer getXmlRpcServer() throws Exception {
		XmlRpcServer server = new XmlRpcServer();
		server.setHandlerMapping(getHandlerMapping());
		return server;
	}

	protected XmlRpcClientConfig getExConfig() throws Exception {
		XmlRpcClientConfigImpl config = getConfig();
		config.setEnabledForExtensions(true);
		return config;
	}

	/** Test, whether we can invoke a method, passing a byte value.
	 * @throws Exception The test failed.
	 */
	public void testByteParam() throws Exception {
		final String methodName = "Remote.byteParam";
		final Object[] params = new Object[]{new Byte((byte) 3)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Integer(6), result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a byte.
	 * @throws Exception The test failed.
	 */
	public void testByteResult() throws Exception {
		final String methodName = "Remote.byteResult";
		final Object[] params = new Object[]{new Byte((byte) 3)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Byte((byte) 6), result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a short value.
	 * @throws Exception The test failed.
	 */
	public void testShortParam() throws Exception {
		final String methodName = "Remote.shortParam";
		final Object[] params = new Object[]{new Short((short) 4)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Integer(8), result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a short value.
	 * @throws Exception The test failed.
	 */
	public void testShortResult() throws Exception {
		final String methodName = "Remote.shortResult";
		final Object[] params = new Object[]{new Short((short) 4)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Short((short) 8), result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing an
	 * integer value.
	 * @throws Exception The test failed.
	 */
	public void testIntParam() throws Exception {
		final String methodName = "Remote.intParam";
		final Object[] params = new Object[]{new Integer(5)};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals(new Integer(10), result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Integer(10), result);
	}

	/** Test, whether we can invoke a method, passing a long value.
	 * @throws Exception The test failed.
	 */
	public void testLongParam() throws Exception {
		final String methodName = "Remote.longParam";
		final Object[] params = new Object[]{new Long(6L)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Integer(12), result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a long value.
	 * @throws Exception The test failed.
	 */
	public void testLongResult() throws Exception {
		final String methodName = "Remote.longResult";
		final Object[] params = new Object[]{new Long(6L)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Long(12L), result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a
	 * string value.
	 * @throws Exception The test failed.
	 */
	public void testStringParam() throws Exception {
		final String methodName = "Remote.stringParam";
		final Object[] params = new Object[]{"abc"};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals("abcabc", result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals("abcabc", result);
	}

	/** Test, whether we can invoke a method, passing a
	 * string value or null.
	 * @throws Exception The test failed.
	 */
	public void testNullableStringParam() throws Exception {
		final String methodName = "Remote.nullableStringParam";
		final Object[] params = new Object[]{"abc"};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals("abcabc", result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals("abcabc", result);
		final Object[] nullParams = new Object[]{null};
		result = getClient().execute(getExConfig(), methodName, nullParams);
		assertEquals("", result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, nullParams);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a
	 * string value or null.
	 * @throws Exception The test failed.
	 */
	public void testNullableStringResult() throws Exception {
		final String methodName = "Remote.nullableStringResult";
		final Object[] params = new Object[]{"abc"};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals("abcabc", result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals("abcabc", result);
		final Object[] nullParams = new Object[]{null};
		result = getClient().execute(getExConfig(), methodName, nullParams);
		assertEquals(null, result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, nullParams);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a float value.
	 * @throws Exception The test failed.
	 */
	public void testFloatParam() throws Exception {
		final String methodName = "Remote.floatParam";
		final Object[] params = new Object[]{new Float(0.4)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		NumberFormat nf = new DecimalFormat("0.00");
		assertEquals("0,80", nf.format(((Double) result).doubleValue()));
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, returning a float value.
	 * @throws Exception The test failed.
	 */
	public void testFloatResult() throws Exception {
		final String methodName = "Remote.floatResult";
		final Object[] params = new Object[]{new Float(0.4)};
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Float(0.8), result);
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}

	/** Test, whether we can invoke a method, passing a
	 * double value.
	 * @throws Exception The test failed.
	 */
	public void testDoubleParam() throws Exception {
		final String methodName = "Remote.doubleParam";
		final Object[] params = new Object[]{new Double(0.6)};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals(new Double(1.2), result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Double(1.2), result);
	}

	/** Test, whether we can invoke a method, returning a
	 * double value.
	 * @throws Exception The test failed.
	 */
	public void testDoubleResult() throws Exception {
		final String methodName = "Remote.doubleResult";
		final Object[] params = new Object[]{new Double(0.6)};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals(new Double(1.2), result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Double(1.2), result);
	}

	/** Test, whether we can invoke a method, passing a
	 * byte array.
	 * @throws Exception The test failed.
	 */
	public void testByteArrayParam() throws Exception {
		final byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
		final String methodName = "Remote.byteArrayParam";
		final Object[] params = new Object[]{bytes};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals(new Integer(0+1+2+3+4+5+6+7+8+9), result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Integer(0+1+2+3+4+5+6+7+8+9), result);
	}

	/** Test, whether we can invoke a method, returning a
	 * byte array.
	 * @throws Exception The test failed.
	 */
	public void testByteArrayResult() throws Exception {
		final byte[] bytes = new byte[]{0, 1, 2, 3, 4, 5, 6, 7};
		final String methodName = "Remote.byteArrayResult";
		final Object[] params = new Object[]{new Integer(8)};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertTrue(Arrays.equals(bytes, (byte[]) result));
		result = getClient().execute(getExConfig(), methodName, params);
		assertTrue(Arrays.equals(bytes, (byte[]) result));
	}

	/** Test, whether we can invoke a method, passing an
	 * object array.
	 * @throws Exception The test failed.
	 */
	public void testObjectArrayParam() throws Exception {
		final Object[] objects = new Object[]{new Byte((byte) 1), new Short((short) 2),
											  new Integer(3), new Long(4), "5"};
		final String methodName = "Remote.objectArrayParam";
		final Object[] params = new Object[]{objects};
		boolean ok = false;
		try {
			getClient().execute(getConfig(), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
		Object result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Integer(15), result);
	}

	/** Test, whether we can invoke a method, returning an
	 * object array.
	 * @throws Exception The test failed.
	 */
	public void testObjectArrayResult() throws Exception {
		final Object[] objects = new Object[]{new Integer(0), new Integer(1),
											  new Integer(2), new Integer(3)};
		final String methodName = "Remote.objectArrayResult";
		final Object[] params = new Object[]{new Integer(4)};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertTrue(Arrays.equals(objects, (Object[]) result));
		result = getClient().execute(getExConfig(), methodName, params);
		assertTrue(Arrays.equals(objects, (Object[]) result));
	}

	/** Test, whether we can invoke a method, passing a map.
	 * @throws Exception The test failed.
	 */
	public void testMapParam() throws Exception {
		final Map map = new HashMap();
		map.put("2", new Integer(3));
		map.put("3", new Integer(5));
		final String methodName = "Remote.mapParam";
		final Object[] params = new Object[]{map};
		Object result = getClient().execute(getConfig(), methodName, params);
		assertEquals(new Integer(21), result);
		result = getClient().execute(getExConfig(), methodName, params);
		assertEquals(new Integer(21), result);
	}

	private void checkMap(Map pResult) {
		assertEquals(4, pResult.size());
		assertEquals(new Integer(0), pResult.get("0"));
		assertEquals(new Integer(1), pResult.get("1"));
		assertEquals(new Integer(2), pResult.get("2"));
		assertEquals(new Integer(3), pResult.get("3"));
	}

	/** Test, whether we can invoke a method, returning a map.
	 * @throws Exception The test failed.
	 */
	public void testMapResult() throws Exception {
		final String methodName = "Remote.mapResult";
		final Object[] params = new Object[]{new Integer(4)};
		Object result = getClient().execute(getConfig(), methodName, params);
		checkMap((Map) result);
		result = getClient().execute(getExConfig(), methodName, params);
		checkMap((Map) result);
	}
}
