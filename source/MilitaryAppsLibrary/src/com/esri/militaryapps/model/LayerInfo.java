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

import java.io.Serializable;

/**
 * A bean containing metadata for a layer.
 */
@SuppressWarnings("serial")
public class LayerInfo implements Serializable {
    
    private LayerType layerType = null;
    private String datasetPath;
    private String name;
    private boolean visible;
    private boolean showVectors = false;
    private boolean showRasters = false;
    private boolean editable = false;

    /**
     * True if vector sub-layers should be shown. Only relevant to certain layer types, such as GeoPackages.
     */
    public boolean isShowVectors() {
        return showVectors;
    }

    /**
     * True if vector sub-layers should be shown. Only relevant to certain layer types, such as GeoPackages.
     */
    public void setShowVectors(boolean showVectors) {
        this.showVectors = showVectors;
    }

    /**
     * True if raster sub-layers should be shown. Only relevant to certain layer types, such as GeoPackages.
     */
    public boolean isShowRasters() {
        return showRasters;
    }

    /**
     * True if raster sub-layers should be shown. Only relevant to certain layer types, such as GeoPackages.
     */
    public void setShowRasters(boolean showRasters) {
        this.showRasters = showRasters;
    }

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

    /**
     * Returns true if and only if the layer can be edited. The default is false.
     * @return true if and only if the layer can be edited.
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets whether the layer can be edited. The default is false.
     * @param editable true if and only if the layer can be edited.
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
}
