/*******************************************************************************
 * Copyright 2013-2014 Esri
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

import java.util.HashMap;
import java.util.Map;

/**
 * A Geomessage bean.
 */
public class Geomessage implements Cloneable {
    
    /**
     * The Geomessage ID field name.
     */
    public static final String ID_FIELD_NAME = "_id";

    /**
     * The Geomessage type field name.
     */
    public static final String TYPE_FIELD_NAME = "_type";
    
    /**
     * The Geomessage WKID field name.
     */
    public static final String WKID_FIELD_NAME = "_wkid";
    
    /**
     * The Geomessage control points field name.
     */
    public static final String CONTROL_POINTS_FIELD_NAME = "_control_points";
    
    /**
     * The Geomessage action field name.
     */
    public static final String ACTION_FIELD_NAME = "_action";
    
    /**
     * The Geomessage symbol ID code (SIC or SIDC) field name.
     */
    public static final String SIC_FIELD_NAME = "sic";
    
    private final HashMap<String, Object> properties = new HashMap<String, Object>();
    
    private String id;
    
    @Override
    public Geomessage clone() {
        Geomessage clone = new Geomessage();
        clone.setId(id);
        clone.properties.putAll(properties);
        return clone;
    }
    
    /**
     * Gets the Geomessage's unique ID.
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the Geomessage's unique ID.
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Gets a property (other than unique ID) of this Geomessage.
     * @param name the property name (not null).
     * @return the property value, or null if it does not exist.
     */
    public Object getProperty(String name) {
        return properties.get(name);
    }
    
    /**
     * Sets a property of this Geomessage.
     * @param name the property name (not null).
     * @param value the property value (can be null).
     */
    public void setProperty(String name, Object value) {
        properties.put(name, value);
    }
    
    /**
     * Gets all properties (other than unique ID) of this Geomessage.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }
    
}
