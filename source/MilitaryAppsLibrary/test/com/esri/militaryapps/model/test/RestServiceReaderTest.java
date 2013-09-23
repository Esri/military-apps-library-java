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
        String urlString = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer";
        URL url = new URL(urlString);
        boolean useAsBasemap = true;
        LayerInfo[] result = RestServiceReader.readService(url, useAsBasemap);
        assertEquals(1, result.length);
        assertEquals(urlString, result[0].getDatasetPath());
        assertEquals(LayerType.TILED_MAP_SERVICE, result[0].getLayerType());
        assertTrue(result[0] instanceof BasemapLayerInfo);
        assertEquals("World Topographic Map", result[0].getName());
        
        urlString = "http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer";
        url = new URL(urlString);
        useAsBasemap = false;
        result = RestServiceReader.readService(url, useAsBasemap);
        assertEquals(1, result.length);
        assertEquals(urlString, result[0].getDatasetPath());
        assertEquals(LayerType.TILED_MAP_SERVICE, result[0].getLayerType());
        assertFalse(result[0] instanceof BasemapLayerInfo);
        assertEquals("World Topographic Map", result[0].getName());
        
        urlString = "http://sampleserver1.arcgisonline.com/ArcGIS/rest/services/Specialty/ESRI_StateCityHighway_USA/MapServer";
        url = new URL(urlString);
        useAsBasemap = true;
        result = RestServiceReader.readService(url, useAsBasemap);
        assertEquals(1, result.length);
        assertEquals(urlString, result[0].getDatasetPath());
        assertEquals(LayerType.DYNAMIC_MAP_SERVICE, result[0].getLayerType());
        assertTrue(result[0] instanceof BasemapLayerInfo);
        assertEquals("USA_Data", result[0].getName());

        urlString = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/HomelandSecurity/operations/FeatureServer/2";
        url = new URL(urlString);
        useAsBasemap = false;
        result = RestServiceReader.readService(url, useAsBasemap);
        assertEquals(1, result.length);
        assertEquals(urlString, result[0].getDatasetPath());
        assertEquals(LayerType.FEATURE_SERVICE, result[0].getLayerType());
        assertFalse(result[0] instanceof BasemapLayerInfo);
        assertEquals("Incident Areas", result[0].getName());

        urlString = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/HomelandSecurity/operations/FeatureServer";
        url = new URL(urlString);
        useAsBasemap = false;
        result = RestServiceReader.readService(url, useAsBasemap);
        assertEquals(3, result.length);
        assertEquals(urlString + "/0", result[0].getDatasetPath());
        assertEquals(LayerType.FEATURE_SERVICE, result[0].getLayerType());
        assertFalse(result[0] instanceof BasemapLayerInfo);
        assertEquals("Incident Points", result[0].getName());        
        assertEquals(urlString + "/1", result[1].getDatasetPath());
        assertEquals(LayerType.FEATURE_SERVICE, result[1].getLayerType());
        assertFalse(result[0] instanceof BasemapLayerInfo);
        assertEquals("Incident Lines", result[1].getName());        
        assertEquals(urlString + "/2", result[2].getDatasetPath());
        assertEquals(LayerType.FEATURE_SERVICE, result[2].getLayerType());
        assertFalse(result[2] instanceof BasemapLayerInfo);
        assertEquals("Incident Areas", result[2].getName());
        
        urlString = "http://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Earthquakes/CaliforniaDEM/ImageServer";
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