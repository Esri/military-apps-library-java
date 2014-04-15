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
package com.esri.militaryapps.model.test;

import com.esri.militaryapps.model.BasemapLayerInfo;
import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.LayerType;
import com.esri.militaryapps.model.RestServiceReader;
import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * A test for the RestServiceReader class.
 * @see com.esri.militaryapps.model.RestServiceReader
 */
public class RestServiceReaderTest {
    
    public RestServiceReaderTest() {
    }
    
    /**
     * Test of readService method, of class RestServiceReader.
     */
    @Test
    public void testReadService() throws Exception {
        String urlString;
        URL url;
        boolean useAsBasemap;
        LayerInfo[] result;
        
        urlString = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer";
        for (int i = 0; i < 2; i++) {
            String sep = 0 == i ? "" : "/";
            urlString += sep;
            url = new URL(urlString);
            useAsBasemap = true;
            result = RestServiceReader.readService(url, useAsBasemap);
            assertEquals(1, result.length);
            assertEquals(urlString, result[0].getDatasetPath());
            assertEquals(LayerType.TILED_MAP_SERVICE, result[0].getLayerType());
            assertTrue(result[0] instanceof BasemapLayerInfo);
            assertEquals("World Topographic Map", result[0].getName());
        }
        
        urlString = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer";
        for (int i = 0; i < 2; i++) {
            String sep = 0 == i ? "" : "/";
            urlString += sep;
            url = new URL(urlString);
            useAsBasemap = false;
            result = RestServiceReader.readService(url, useAsBasemap);
            assertEquals(1, result.length);
            assertEquals(urlString, result[0].getDatasetPath());
            assertEquals(LayerType.TILED_MAP_SERVICE, result[0].getLayerType());
            assertFalse(result[0] instanceof BasemapLayerInfo);
            assertEquals("World Topographic Map", result[0].getName());
        }
        
        urlString = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Specialty/ESRI_StateCityHighway_USA/MapServer";
        for (int i = 0; i < 2; i++) {
            String sep = 0 == i ? "" : "/";
            urlString += sep;
            url = new URL(urlString);
            useAsBasemap = true;
            result = RestServiceReader.readService(url, useAsBasemap);
            assertEquals(1, result.length);
            assertEquals(urlString, result[0].getDatasetPath());
            assertEquals(LayerType.DYNAMIC_MAP_SERVICE, result[0].getLayerType());
            assertTrue(result[0] instanceof BasemapLayerInfo);
            assertEquals("USA_Data", result[0].getName());
        }

        urlString = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/HomelandSecurity/operations/FeatureServer/2";
        for (int i = 0; i < 2; i++) {
            String sep = 0 == i ? "" : "/";
            urlString += sep;
            url = new URL(urlString);
            useAsBasemap = false;
            result = RestServiceReader.readService(url, useAsBasemap);
            assertEquals(1, result.length);
            assertEquals(urlString, result[0].getDatasetPath());
            assertEquals(LayerType.FEATURE_SERVICE, result[0].getLayerType());
            assertFalse(result[0] instanceof BasemapLayerInfo);
            assertEquals("Incident Areas", result[0].getName());
        }

        urlString = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/HomelandSecurity/operations/FeatureServer";
        for (int i = 0; i < 2; i++) {
            String sep = 0 == i ? "" : "/";
            String notSep = 1 == i ? "" : "/";
            urlString += sep;
            url = new URL(urlString);
            useAsBasemap = false;
            result = RestServiceReader.readService(url, useAsBasemap);
            assertEquals(3, result.length);
            assertEquals(urlString + notSep + "0", result[0].getDatasetPath());
            assertEquals(LayerType.FEATURE_SERVICE, result[0].getLayerType());
            assertFalse(result[0] instanceof BasemapLayerInfo);
            assertEquals("Incident Points", result[0].getName());        
            assertEquals(urlString + notSep + "1", result[1].getDatasetPath());
            assertEquals(LayerType.FEATURE_SERVICE, result[1].getLayerType());
            assertFalse(result[0] instanceof BasemapLayerInfo);
            assertEquals("Incident Lines", result[1].getName());        
            assertEquals(urlString + notSep + "2", result[2].getDatasetPath());
            assertEquals(LayerType.FEATURE_SERVICE, result[2].getLayerType());
            assertFalse(result[2] instanceof BasemapLayerInfo);
            assertEquals("Incident Areas", result[2].getName());
        }
        
        urlString = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Earthquakes/CaliforniaDEM/ImageServer";
        for (int i = 0; i < 2; i++) {
            String sep = 0 == i ? "" : "/";
            urlString += sep;
            url = new URL(urlString);
            useAsBasemap = false;
            result = RestServiceReader.readService(url, useAsBasemap);
            assertEquals(1, result.length);
            assertEquals(urlString, result[0].getDatasetPath());
            assertEquals(LayerType.IMAGE_SERVICE, result[0].getLayerType());
            assertFalse(result[0] instanceof BasemapLayerInfo);
            assertEquals("SRTMCalifornia.tif", result[0].getName());
        }

    }
    
}