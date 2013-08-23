/*******************************************************************************
 * Copyright 2013 Esri
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

/**
 * A bean containing metadata for a layer.
 */
public class LayerInfo {
    
    private LayerType layerType = null;
    private String datasetPath;
    private String name;
    private boolean visible;

    /**
     * @return the layerType
     */
    public LayerType getLayerType() {
        return layerType;
    }

    /**
     * @param layerType the layerType to set
     */
    public void setLayerType(LayerType layerType) {
        this.layerType = layerType;
    }

    /**
     * @return the datasetPath
     */
    public String getDatasetPath() {
        return datasetPath;
    }

    /**
     * @param datasetPath the datasetPath to set
     */
    public void setDatasetPath(String datasetPath) {
        this.datasetPath = datasetPath;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
}
