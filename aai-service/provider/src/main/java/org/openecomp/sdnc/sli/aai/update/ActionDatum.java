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

package org.openecomp.sdnc.sli.aai.update;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "property-name",
    "property-value"
})
public class ActionDatum {

    @JsonProperty("property-name")
    private String propertyName;
    @JsonProperty("property-value")
    private String propertyValue;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The propertyName
     */
    @JsonProperty("property-name")
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * 
     * @param propertyName
     *     The property-name
     */
    @JsonProperty("property-name")
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * 
     * @return
     *     The propertyValue
     */
    @JsonProperty("property-value")
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * 
     * @param propertyValue
     *     The property-value
     */
    @JsonProperty("property-value")
    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
