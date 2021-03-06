/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 							reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdnc.ra;

public enum ReleaseRequestType {
	Cancel, Activate, Disconnect;

	public static ReleaseRequestType convert(Object o) {
		if (o == null)
			return null;
		String s = o.toString();
		s = s.trim();
		if (s.length() == 0)
			return null;

		if (s.equalsIgnoreCase("Cancel"))
			return Cancel;
		if (s.equalsIgnoreCase("Activate"))
			return Activate;
		if (s.equalsIgnoreCase("Disconnect"))
			return Disconnect;

		throw new IllegalArgumentException("Invalid request-type: " + s +
		        ". Supported values are Cancel, Activate, Disconnect.");
	}
}
