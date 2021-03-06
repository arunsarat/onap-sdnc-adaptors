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

package org.openecomp.sdnc.sli.aai;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.openecomp.sdnc.sli.aai.data.AAIDatum;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openecomp.aai.inventory.v10.PInterface;

public class PInterfacePnfRequest extends AAIRequest {

	// tenant (1602)
	public static final String PINTERFACE_PATH			= "org.openecomp.sdnc.sli.aai.path.pserver.pinterface";
	public static final String PINTERFACE_QUERY_PATH	= "org.openecomp.sdnc.sli.aai.path.pserver.pinterface.query";
	public static final String PNF_PATH		  = "org.openecomp.sdnc.sli.aai.path.pnf";

	private final String pinterface_path;
	private final String pinterface_query_path;

	public static final String INTERFACE_NAME = "interface-name";
	public static final String PINTERFACE_INTERFACE_NAME = "p-interface.interface-name";
	public static final String PNF_NAME	= "pnf-name";
	public static final String PNF_PNF_NAME	= "pnf.pnf-name";


	public PInterfacePnfRequest() {
		pinterface_path = configProperties.getProperty(PNF_PATH) + "/p-interfaces/p-interface/{interface-name}";
		pinterface_query_path = configProperties.getProperty(PINTERFACE_QUERY_PATH);
		LoggerFactory.getLogger(PInterfacePnfRequest.class).debug("org.openecomp.sdnc.sli.aai.path.pserver.pinterface=\t" + pinterface_path);
		LoggerFactory.getLogger(PInterfacePnfRequest.class).debug("org.openecomp.sdnc.sli.aai.path.pserver.pinterface.query=\t" + pinterface_query_path);
		if(pinterface_path == null) {
			LoggerFactory.getLogger(PInterfacePnfRequest.class).warn("org.openecomp.sdnc.sli.aai.path.pserver.pinterface PATH not found in aaiclient.properties");
		}
	}


	@Override
	public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException {

		String request_url = target_uri + pinterface_path;
		String encoded_vnf = null;

		String interfaceName = null;

		if(requestProperties.containsKey(INTERFACE_NAME)) {
			interfaceName = requestProperties.getProperty(INTERFACE_NAME);
		} else
		if(requestProperties.containsKey(PINTERFACE_INTERFACE_NAME)) {
			interfaceName = requestProperties.getProperty(PINTERFACE_INTERFACE_NAME);
		}

		encoded_vnf = encodeQuery(interfaceName);
		request_url = request_url.replace("{interface-name}", encoded_vnf) ;

		request_url = processPnfRequestPathData(request_url,requestProperties);

		if(resourceVersion != null) {
			request_url = request_url +"?resource-version="+resourceVersion;
		}
		URL http_req_url =	new URL(request_url);

		aaiService.LOGwriteFirstTrace(method, http_req_url.toString());
		aaiService.LOGwriteDateTrace("interface-name", interfaceName);

		return http_req_url;
	}

	public static String processPnfRequestPathData(String request_url, Properties requestProperties) throws UnsupportedEncodingException {

		String key = PNF_NAME;
		if(requestProperties.containsKey(PNF_PNF_NAME)) {
			key = PNF_PNF_NAME;
		}

		if(!requestProperties.containsKey(key)) {
			aaiService.logKeyError(String.format("%s,%s", PNF_NAME, PNF_PNF_NAME));
		}

		String encoded_vnf = encodeQuery(requestProperties.getProperty(key));
		request_url = request_url.replace("{pnf-name}", encoded_vnf) ;

		return request_url;
	}

	@Override
	public URL getRequestQueryUrl(String method) throws UnsupportedEncodingException, MalformedURLException {
		return this.getRequestUrl(method, null);
	}


	@Override
	public String toJSONString() {
		ObjectMapper mapper = getObjectMapper();
		PInterface vnfc = (PInterface)requestDatum;
		String json_text = null;
		try {
			json_text = mapper.writeValueAsString(vnfc);
		} catch (JsonProcessingException exc) {
			handleException(this, exc);
			return null;
		}
		return json_text;
	}

	@Override
	public String[] getArgsList() {
		String[] args = {INTERFACE_NAME, PINTERFACE_INTERFACE_NAME, PNF_NAME, PNF_PNF_NAME};

		return args;
	}

	@Override
	public Class<? extends AAIDatum> getModelClass() {
		return PInterface.class;
	}
}
