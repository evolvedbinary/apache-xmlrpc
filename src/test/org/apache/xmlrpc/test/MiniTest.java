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
import org.apache.xmlrpc.common.XmlRpcExtensionException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;

import junit.framework.TestCase;


/** An abstract test case, to be implemented for the various
 * transport classes.
 */
public class MiniTest extends TestCase {
	private ClientProvider[] providers;

	public void setUp() throws Exception {
		if (providers == null) {
			XmlRpcHandlerMapping mapping = getHandlerMapping();
			providers = new ClientProvider[]{
//				new LocalTransportProvider(mapping),
//				new LocalStreamTransportProvider(mapping),
//				new LiteTransportProvider(mapping, true),
				new LiteTransportProvider(mapping, false),
//				new SunHttpTransportProvider(mapping, true),
//				new SunHttpTransportProvider(mapping, false),
//				new CommonsProvider(mapping)
			};
		}
	}

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

	protected XmlRpcHandlerMapping getHandlerMapping() throws IOException, XmlRpcException {
		return new PropertyHandlerMapping(getClass().getClassLoader(),
										  getClass().getResource("BaseTest.properties"));
	}

	protected XmlRpcClientConfigImpl getConfig(ClientProvider pProvider) throws Exception {
		return pProvider.getConfig();
	}

	protected XmlRpcClientConfig getExConfig(ClientProvider pProvider) throws Exception {
		XmlRpcClientConfigImpl config = getConfig(pProvider);
		config.setEnabledForExtensions(true);
		return config;
	}

	/** Test, whether we can invoke a method, passing a byte value.
	 * @throws Exception The test failed.
	 */
	public void testByteParam() throws Exception {
		for (int i = 0;  i < providers.length;  i++) {
			testByteParam(providers[i]);
		}
	}

	private void testByteParam(ClientProvider pProvider) throws Exception {
		final String methodName = "Remote.byteParam";
		final Object[] params = new Object[]{new Byte((byte) 3)};
		XmlRpcClient client = pProvider.getClient();
		Object result = client.execute(getExConfig(pProvider), methodName, params);
		assertEquals(new Integer(6), result);
		boolean ok = false;
		try {
			client.execute(getConfig(pProvider), methodName, params);
		} catch (XmlRpcExtensionException e) {
			ok = true;
		}
		assertTrue(ok);
	}
}
