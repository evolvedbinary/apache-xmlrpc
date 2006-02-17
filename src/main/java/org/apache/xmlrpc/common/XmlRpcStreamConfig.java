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
package org.apache.xmlrpc.common;

import org.apache.xmlrpc.XmlRpcConfig;


/** Interface of a configuration for a stream based transport.
 */
public interface XmlRpcStreamConfig extends XmlRpcConfig {
	/** Default encoding (UTF-8).
	 */
	public static final String UTF8_ENCODING = "UTF-8";

	/** Returns the encoding being used for data encoding, when writing
	 * to a stream.
	 * @return Suggested encoding, or null, if the {@link #UTF8_ENCODING}
	 * is being used.
	 */
	String getEncoding();
}
