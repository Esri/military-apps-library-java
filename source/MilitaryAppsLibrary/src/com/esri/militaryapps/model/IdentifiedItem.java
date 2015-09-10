/*******************************************************************************
 * Copyright 2012-2015 Esri
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.militaryapps.model;

import java.util.Map;

/**
 * An identified item with a geometry and attributes. Implementers should add a way to store and retrieve
 * the geometry for their specific platform.
 */
public abstract class IdentifiedItem {

    private final int layerId;
    private final Map<String, Object> attributes;
    private final Object value;

    public IdentifiedItem(int layerId, Map<String, Object> attributes, Object value) {
        this.layerId = layerId;
        this.attributes = attributes;
        this.value = value;
    }

    /**
     * @return the layerId
     */
    public int getLayerId() {
        return layerId;
    }

    /**
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    public abstract Object getGeometry();

}
