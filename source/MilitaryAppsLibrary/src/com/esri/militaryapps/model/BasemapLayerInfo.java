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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A bean to contain a Layer object and other information, such as a thumbnail image,
 * that would go with a basemap layer.
 */
@SuppressWarnings("serial")
public class BasemapLayerInfo extends LayerInfo {
    
    private final URL thumbnailUrl;

    /**
     * Constructs a new BasemapLayer bean.
     * @param thumbnailUrl the URL (file:, http:, etc.) for a thumbnail icon for the basemap
     *                          layer.
     */
    public BasemapLayerInfo(URL thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    /**
     * Constructs a new BasemapLayer bean.
     * @param thumbnailPath the URL or filename for a thumbnail icon for the basemap layer.
     */
    public BasemapLayerInfo(String thumbnailPath) {
        if (null == thumbnailPath) {
            thumbnailUrl = null;
        } else {
            URL url = null;
            try {
                url = new URL(thumbnailPath);
            } catch (MalformedURLException ex) {
                try {
                    url = new File(thumbnailPath).toURI().toURL();
                } catch (MalformedURLException ex1) {
                    Logger.getLogger(BasemapLayerInfo.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            thumbnailUrl = url;
        }
    }

    /**
     * @return the thumbnail
     */
    public URL getThumbnailUrl() {
        return thumbnailUrl;
    }
    
}
