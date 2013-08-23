/*******************************************************************************
 * Copyright 2012 Esri
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

import java.awt.MediaTracker;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A bean to contain a Layer object and other information, such as a thumbnail image,
 * that would go with a basemap layer.
 */
public class BasemapLayerInfo extends LayerInfo {
    
    private final Icon thumbnail;
    private Object layer;

    /**
     * Constructs a new BasemapLayerInfo bean.
     * @param layer the layer object for the basemap layer. This parameter's class
     *              is Layer (or a child of Layer) in the SDK you're using.
     * @param thumbnail a thumbnail icon for the basemap layer.
     */
    public BasemapLayerInfo(Icon thumbnail) {
        this.thumbnail = thumbnail;
    }
    
    /**
     * Constructs a new BasemapLayer bean.
     * @param thumbnailFilename the filename for a thumbnail icon for the basemap
     *                          layer.
     */
    public BasemapLayerInfo(String thumbnailFilename) {
        if (null == thumbnailFilename) {
            thumbnail = null;
        } else {
            ImageIcon imageIcon = new ImageIcon(thumbnailFilename);
            thumbnail = MediaTracker.COMPLETE == imageIcon.getImageLoadStatus() ? imageIcon : null;
        }
    }

    /**
     * @return the layer. Its class is Layer (or a child of Layer) in the ArcGIS
     *         SDK you're using.
     */
    public Object getLayer() {
        return layer;
    }
    
    /**
     * Sets the layer. Its class is Layer (or a child of Layer) in the ArcGIS SDK
     * you're using.
     * @param layer 
     */
    public void setLayer(Object layer) {
        this.layer = layer;
    }

    /**
     * @return the thumbnail
     */
    public Icon getThumbnail() {
        return thumbnail;
    }
    
}
